package com.ex.prac.service;

import com.ex.prac.dto.LoginRequestDto;
import com.ex.prac.dto.SignupRequestDto;
import com.ex.prac.dto.UserDto;
import com.ex.prac.entity.User;
import com.ex.prac.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void signup_성공() {
        // given
        SignupRequestDto dto = SignupRequestDto.builder()
                .username("tester")
                .email("tester@example.com")
                .password("rawPassword")
                .build();

        when(userRepository.existsByEmail("tester@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("tester")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        User user = User.builder()
                .username("tester")
                .email("tester@example.com")
                .password("encodedPassword")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        UserDto result = userService.signup(dto);

        // then
        assertEquals("tester", result.getUsername());
        assertEquals("tester@example.com", result.getEmail());
    }

    @Test
    void signup_중복_이메일() {
        // given
        SignupRequestDto dto = SignupRequestDto.builder()
                .username("tester")
                .email("tester@example.com")
                .password("pass")
                .build();

        when(userRepository.existsByEmail("tester@example.com")).thenReturn(true);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.signup(dto));
        assertEquals("이미 사용 중인 이메일입니다.", ex.getMessage());
    }

    @Test
    void login_성공() {
        // given
        LoginRequestDto dto = LoginRequestDto.builder()
                .email("tester@example.com")
                .password("rawPassword")
                .build();

        User user = User.builder()
                .username("tester")
                .email("tester@example.com")
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // when
        UserDto result = userService.login(dto);

        // then
        assertEquals("tester", result.getUsername());
        assertEquals("tester@example.com", result.getEmail());
    }

    @Test
    void login_이메일_없음() {
        // given
        LoginRequestDto dto = LoginRequestDto.builder()
                .email("notfound@example.com")
                .password("pass")
                .build();

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.login(dto));
        assertEquals("존재하지 않는 이메일입니다.", ex.getMessage());
    }

    @Test
    void login_비밀번호_불일치() {
        // given
        LoginRequestDto dto = LoginRequestDto.builder()
                .email("tester@example.com")
                .password("wrongPassword")
                .build();

        User user = User.builder()
                .username("tester")
                .email("tester@example.com")
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.login(dto));
        assertEquals("비밀번호가 일치하지 않습니다.", ex.getMessage());
    }
}