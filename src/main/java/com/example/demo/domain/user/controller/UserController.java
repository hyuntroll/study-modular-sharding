package com.example.demo.domain.user.controller;


import com.example.demo.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/user/create")
    public void create(Request request) {
        userService.create(request.getEmail(), request.getPassword());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        private String password;
        private String email;
    }
}
