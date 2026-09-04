# study-modular-sharding

Spring Boot에서 단일 history DB와 사용자 키 기반 샤딩을 같은 애플리케이션 구조로 실습하는 프로젝트입니다.

확인할 수 있는 내용은 다음과 같습니다.

- application DB와 history DB 분리
- `SINGLE`, `SHARDED` 토폴로지 선택과 운영 모드 잠금
- `userId % shardCount` 기반 모듈러 라우팅
- template과 AOP 방식의 샤딩 컨텍스트 관리
- Flyway를 이용한 application DB와 각 history DB 스키마 관리

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
- Gradle Wrapper

## 빠른 시작

### 1. PostgreSQL 준비

로컬 PostgreSQL을 사용하거나 실습용 컨테이너와 DB 3개를 만듭니다.

```bash
docker run -d \
  --name modular-sharding-postgres \
  -e POSTGRES_USER=history_shard_1 \
  -e POSTGRES_PASSWORD=demo1234 \
  -e POSTGRES_DB=demo_application \
  -p 5432:5432 \
  postgres:16

until docker exec modular-sharding-postgres \
  pg_isready -U history_shard_1 -d demo_application
do
  sleep 1
done

docker exec modular-sharding-postgres \
  createdb -U history_shard_1 demo_shard_1

docker exec modular-sharding-postgres \
  createdb -U history_shard_1 demo_shard_2
```

| DB | 역할 |
|---|---|
| `demo_application` | 사용자 데이터 |
| `demo_shard_1` | SINGLE history DB 또는 shard 0 |
| `demo_shard_2` | shard 1 |

### 2. SINGLE 모드 실행

```bash
HISTORY_DB_MODE=SINGLE \
HISTORY_DB_EXPECTED_MODE=SINGLE \
./gradlew bootRun
```

`HISTORY_DB_MODE`와 `HISTORY_DB_EXPECTED_MODE`가 다르면 애플리케이션은 기동하지 않습니다.

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

## 주요 설정

| 설정 | 기본값 | 설명 |
|---|---|---|
| `spring.datasource.*` | `demo_application` | 사용자용 DB |
| `datasource.history.mode` | 필수 | `SINGLE` 또는 `SHARDED` |
| `datasource.history.expected-mode` | 필수 | 배포 환경에서 허용한 모드 |
| `datasource.history.single` | `demo_shard_1` | SINGLE history DB |
| `datasource.history.shard.strategy` | `MODULAR` | `MODULAR` 또는 `RANGE` |
| `datasource.history.shards` | 2개 | 샤드 번호 순서로 등록할 DB 목록 |
| `sharding.mode` | `template` | `template` 또는 `aop` |

각 DB 연결 정보는 `SPRING_DATASOURCE_*`, `HISTORY_DB_*`, `HISTORY_SHARD_0_*`, `HISTORY_SHARD_1_*` 환경 변수로 변경할 수 있습니다.

## 검증

PostgreSQL을 실행한 상태에서 테스트합니다.

```bash
HISTORY_DB_MODE=SINGLE \
HISTORY_DB_EXPECTED_MODE=SINGLE \
./gradlew test --no-daemon
```

## 디렉터리

```text
src/main/java/.../global/datasource/  샤딩 설정, 컨텍스트, 라우터, 팩토리
src/main/java/.../adapter/            REST API와 JPA adapter
src/main/resources/application.yaml  DB, 샤딩, Flyway 설정
src/main/resources/db/migration/     application/history Flyway SQL
```

## 참고 자료

- [Spring Boot 데이터베이스 초기화와 Flyway](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Flyway Java API](https://documentation.red-gate.com/flyway/reference/usage/api-java)
- [DB 분산처리를 위한 sharding](https://techblog.woowahan.com/2687/)
- [마이데이터 플랫폼의 대용량 데이터 처리 개선](https://tech.kakaopay.com/post/mydata-platfrom-improvement/#%EC%9D%B4%EC%8A%88-3-%ED%86%B5%EA%B3%84-%EB%B0%B0%EC%B9%98-%EC%88%98%ED%96%89-%EC%8B%9C%EA%B0%84-%EC%A6%9D%EA%B0%80)
