# study-modular-sharding

Spring Boot에서 단일 history DB와 사용자 키 기반 샤딩을 같은 애플리케이션 구조로 실습하는 프로젝트입니다.

확인할 수 있는 내용은 다음과 같습니다.

- application DB와 history DB 분리
- `SINGLE`, `SHARDED` 토폴로지 선택과 운영 모드 잠금
- `userId % shardCount` 기반 모듈러 라우팅
- template과 AOP 방식의 샤딩 컨텍스트 관리
- Flyway를 이용한 application DB와 각 history DB 스키마 관리
- Actuator readiness와 JSON 콘솔 로그를 이용한 배포 검증
- 테스트, 이미지 발행, Infra PR 생성을 잇는 GitHub Actions

> 학습용 프로젝트입니다. 기본 비밀번호, 평문 사용자 비밀번호 저장, 인증 없는 API를 그대로 운영 환경에 사용하면 안 됩니다.

## 구조

```text
Controller -> Application Service -> Persistence Port
                                      |
                       +--------------+--------------+
                       |                             |
               application DB                  history DB
                                              SINGLE 또는
                                  LazyConnectionDataSourceProxy
                                               |
                                        DataSourceRouter
                                               |
                              userId % shardCount -> shard
```

## 기술 스택

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Boot Actuator
- Gradle Wrapper

## 빠른 시작

### 1. PostgreSQL 준비

테스트용 Compose는 PostgreSQL 인스턴스 하나에 DB 3개를 만듭니다.

```bash
docker compose -f compose.ci.yaml up -d --wait
```

| DB | 역할 |
|---|---|
| `demo_application` | 사용자 데이터 |
| `demo_shard_1` | SINGLE history DB 또는 shard 0 |
| `demo_shard_2` | shard 1 |

### 2. SINGLE 모드 실행

```bash
./gradlew bootRun
```

`local` 프로필의 기본 모드는 `SINGLE`입니다. `HISTORY_DB_MODE`와 `HISTORY_DB_EXPECTED_MODE`가 다르면 애플리케이션은 기동하지 않습니다.

### 3. API 확인

```bash
curl -i -X POST \
  'http://localhost:8080/user/create?email=user1@example.com&password=demo'

curl -i -X POST \
  'http://localhost:8080/history/save?userId=1&action=LOGIN'

curl -s -X POST \
  'http://localhost:8080/history?userId=1&historyId=1'
```

조회 결과 예시:

```json
{"action":"LOGIN","userId":1,"id":1}
```

배포 상태는 메인 포트에서 확인할 수 있습니다.

```bash
curl -f http://localhost:8080/livez
curl -f http://localhost:8080/readyz
```

콘솔 로그는 Logstash JSON 형식이며 `database_mode`, `shard_index`, `shard_name`과 다음 배포 식별 필드를 포함합니다.

- `service=modular-sharding-api`
- `environment=${APP_ENV:local}`
- `revision=${APP_REVISION:local}`

## SHARDED 모드

```bash
HISTORY_DB_MODE=SHARDED \
HISTORY_DB_EXPECTED_MODE=SHARDED \
./gradlew bootRun
```

현재 설정은 `MODULAR`이며 등록된 shard 수를 나눗수로 사용합니다.

| `userId` | 계산 | DB |
|---:|---:|---|
| 1 | `1 % 2 = 1` | `demo_shard_2` |
| 2 | `2 % 2 = 0` | `demo_shard_1` |
| 3 | `3 % 2 = 1` | `demo_shard_2` |

샤딩 키가 없거나 `RANGE` 규칙에 맞는 shard가 없으면 기본 DB로 우회하지 않고 예외를 발생시킵니다.

### template과 AOP

```bash
SHARDING_MODE=template \
HISTORY_DB_MODE=SHARDED \
HISTORY_DB_EXPECTED_MODE=SHARDED \
./gradlew bootRun
```

```bash
SHARDING_MODE=aop \
HISTORY_DB_MODE=SHARDED \
HISTORY_DB_EXPECTED_MODE=SHARDED \
./gradlew bootRun
```

| 방식 | 특징 |
|---|---|
| `template` | `ShardingTemplate.execute(...)` 호출에서 범위와 키가 명확함 |
| `aop` | `@Sharding` adapter의 첫 번째 `Long` 인자를 샤딩 키로 사용 |

## 모드 변경 주의

`SINGLE`과 `SHARDED`는 실행 가능한 토폴로지를 선택할 뿐 데이터를 자동으로 옮기지 않습니다.

- `SINGLE -> SHARDED`: 기존 데이터는 원래 DB에 남아 있어 다른 shard로 라우팅되는 사용자의 데이터가 보이지 않을 수 있습니다.
- `SHARDED -> SINGLE`: 선택된 단일 DB 외의 shard 데이터는 조회되지 않습니다.
- 실제 전환은 쓰기 중지, 데이터 이관, identity sequence 조정, 건수 검증, 재기동과 롤백 계획을 포함한 별도 작업으로 수행해야 합니다.

운영에서는 `HISTORY_DB_EXPECTED_MODE`를 배포 정책에 고정합니다.

## Flyway

애플리케이션 시작 시 다음 마이그레이션을 실행합니다.

| 대상 | 위치 | 이력 테이블 |
|---|---|---|
| application DB | `db/migration/application` | `flyway_schema_history` |
| SINGLE 또는 각 history shard | `db/migration/history` | `flyway_history_schema_history` |

새로운 스키마 변경은 기존 SQL을 수정하지 않고 다음 버전 파일을 추가합니다.

```text
src/main/resources/db/migration/application/V2__change_users.sql
src/main/resources/db/migration/history/V2__change_history.sql
```

기존 Hibernate DDL로 만든 DB를 도입할 때는 현재 스키마를 검증한 뒤 별도의 Flyway baseline 절차가 필요합니다.

## 운영 프로필

`prod` 프로필에는 DB 주소와 인증 정보의 실사용 기본값이 없습니다. 선택된 토폴로지에 필요한 값이 비어 있으면 시작 검증에서 실패합니다.

```bash
SPRING_PROFILES_ACTIVE=prod \
APP_ENV=production \
APP_REVISION=<git-commit-sha> \
HISTORY_DB_MODE=SHARDED \
HISTORY_DB_EXPECTED_MODE=SHARDED \
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/demo_application \
SPRING_DATASOURCE_USERNAME=<username> \
SPRING_DATASOURCE_PASSWORD=<password> \
HISTORY_SHARD_0_URL=jdbc:postgresql://postgres:5432/demo_shard_1 \
HISTORY_SHARD_0_USERNAME=<username> \
HISTORY_SHARD_0_PASSWORD=<password> \
HISTORY_SHARD_1_URL=jdbc:postgresql://postgres:5432/demo_shard_2 \
HISTORY_SHARD_1_USERNAME=<username> \
HISTORY_SHARD_1_PASSWORD=<password> \
java -jar build/libs/application.jar
```

`HISTORY_DB_EXPECTED_MODE`는 애플리케이션 이미지가 아니라 Infra 저장소에서 고정해야 합니다.

## 주요 설정

| 설정 | 기본값 | 설명 |
|---|---|---|
| `spring.datasource.*` | `demo_application` | 사용자용 DB |
| `datasource.history.mode` | local: `SINGLE`, prod: 필수 | `SINGLE` 또는 `SHARDED` |
| `datasource.history.expected-mode` | local: `SINGLE`, prod: 필수 | 배포 환경에서 허용한 모드 |
| `datasource.history.single` | `demo_shard_1` | SINGLE history DB |
| `datasource.history.shard.strategy` | `MODULAR` | `MODULAR` 또는 `RANGE` |
| `datasource.history.shards` | 2개 | 샤드 번호 순서로 등록할 DB 목록 |
| `sharding.mode` | `template` | `template` 또는 `aop` |

각 DB 연결 정보는 `SPRING_DATASOURCE_*`, `HISTORY_DB_*`, `HISTORY_SHARD_0_*`, `HISTORY_SHARD_1_*` 환경 변수로 변경할 수 있습니다.

## CI/CD

[`application-pipeline.yml`](.github/workflows/application-pipeline.yml)은 하나의 의존성 체인으로 실행됩니다.

```text
Pull Request -> test + Docker build
main push   -> test + Docker build -> GHCR digest push -> Infra PR
```

Infra PR 생성에는 대상 저장소에 설치된 GitHub App과 다음 설정이 필요합니다.

- Repository secret: `GHCR_USERNAME`
- Repository secret: `GHCR_PAT` (GHCR push 권한이 있는 Personal Access Token)
- Repository variable: `INFRA_UPDATE_ENABLED=true`
- Repository variable: `INFRA_APP_CLIENT_ID`
- Repository secret: `INFRA_APP_PRIVATE_KEY`
- Infra 파일: `versions/prod.env`

현재 Infra 저장소가 없거나 접근할 수 없는 동안에는 `INFRA_UPDATE_ENABLED`를 설정하지 않아야 합니다. 애플리케이션 Workflow는 서버에 직접 SSH 접속하거나 배포하지 않습니다.

## 검증

CI와 같은 DB 구성을 실행한 뒤 테스트와 이미지를 검증합니다. 로컬 5432 포트가 사용 중이면 `POSTGRES_PORT`와 JDBC URL을 함께 변경합니다.

```bash
docker compose -f compose.ci.yaml up -d --wait

HISTORY_DB_MODE=SHARDED \
HISTORY_DB_EXPECTED_MODE=SHARDED \
./gradlew test --no-daemon

docker build -t study-modular-sharding:test .
docker compose -f compose.ci.yaml down -v
```

## 디렉터리

```text
src/main/java/.../global/datasource/  샤딩 설정, 컨텍스트, 라우터, 팩토리
src/main/java/.../adapter/            REST API와 JPA adapter
src/main/resources/application*.yaml 공통, local, prod 설정
src/main/resources/db/migration/     application/history Flyway SQL
docker/postgres/init.sql             테스트 DB 초기화
compose.ci.yaml                      CI용 PostgreSQL
Dockerfile                           애플리케이션 이미지
.github/workflows/                   검증, GHCR 발행, Infra PR
```

## 참고 자료

- [Spring Boot 데이터베이스 초기화와 Flyway](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Flyway Java API](https://documentation.red-gate.com/flyway/reference/usage/api-java)
- [DB 분산처리를 위한 sharding](https://techblog.woowahan.com/2687/)
- [마이데이터 플랫폼의 대용량 데이터 처리 개선](https://tech.kakaopay.com/post/mydata-platfrom-improvement/#%EC%9D%B4%EC%8A%88-3-%ED%86%B5%EA%B3%84-%EB%B0%B0%EC%B9%98-%EC%88%98%ED%96%89-%EC%8B%9C%EA%B0%84-%EC%A6%9D%EA%B0%80)
