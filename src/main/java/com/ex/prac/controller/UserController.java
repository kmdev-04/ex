package com.ex.prac.controller;

import com.ex.prac.dto.LoginRequestDto;
import com.ex.prac.dto.SignupRequestDto;
import com.ex.prac.dto.UserDto;
import com.ex.prac.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequestDto dto) {
        UserDto savedUser = userService.signup(dto);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto dto) {
        UserDto loggedInUser = userService.login(dto);
        return ResponseEntity.ok(loggedInUser);
    }
}