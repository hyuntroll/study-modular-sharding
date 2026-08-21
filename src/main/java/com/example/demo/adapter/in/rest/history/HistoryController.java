package com.example.demo.adapter.in.rest.history;

import com.example.demo.adapter.out.persistence.history.HistoryEntity;
import com.example.demo.application.history.HistoryService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HistoryController {
    private final HistoryService historyService;

    @PostMapping("/history")
    public HistoryEntity load(Request request) {
        return historyService.getHistory(request.getUserId(), request.getHistoryId());
    }

    @PostMapping("/history/save")
    public void save(Request1 request) {
        historyService.save(request.getUserId(), request.getAction());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        private Long userId;
        private Long historyId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request1 {
        private Long userId;
        private String action;
    }
}
