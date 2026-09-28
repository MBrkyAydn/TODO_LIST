package com.berkay.todo.mapper;

import com.berkay.todo.dto.request.RegisterRequest;
import com.berkay.todo.dto.response.UserResponse;
import com.berkay.todo.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        return userResponse;
    }

    public User toEntity(RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        return user;


    }
}
