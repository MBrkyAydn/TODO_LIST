package com.berkay.todo.controller;

import com.berkay.todo.dto.request.RegisterRequest;
import com.berkay.todo.dto.response.UserResponse;
import com.berkay.todo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
      return    userService.register(registerRequest);
    }

}
