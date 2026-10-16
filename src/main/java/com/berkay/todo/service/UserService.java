package com.berkay.todo.service;

import com.berkay.todo.dto.request.LoginRequest;
import com.berkay.todo.dto.request.RegisterRequest;
import com.berkay.todo.dto.response.LoginResponse;
import com.berkay.todo.dto.response.UserResponse;

import com.berkay.todo.exception.AlreadyExistsException;
import com.berkay.todo.mapper.UserMapper;
import com.berkay.todo.repository.UserRepository;
import com.berkay.todo.entity.User;
import com.berkay.todo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new AlreadyExistsException("Bu email adresi zaten kayıtlı");
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = userMapper.toEntity(registerRequest);

        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }


    public LoginResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                        loginRequest.getPassword())

        );
        String token =jwtService.generateToken(loginRequest.getEmail());
        return new LoginResponse(token);



    }


}
