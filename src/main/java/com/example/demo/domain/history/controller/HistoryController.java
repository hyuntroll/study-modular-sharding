package com.example.demo.domain.history.controller;

import com.example.demo.domain.history.domain.HistoryEntity;
import com.example.demo.domain.history.server.HistoryService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
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
