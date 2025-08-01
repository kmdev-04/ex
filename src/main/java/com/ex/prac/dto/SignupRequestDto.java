package com.ex.prac.dto;

import com.ex.prac.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto {
    private String username;
    private String password;
    private String email;

    public User toEntity(String encodedPassword) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .build();
    }
}
