package com.ex.prac.service;

import com.ex.prac.dto.LoginRequestDto;
import com.ex.prac.dto.SignupRequestDto;
import com.ex.prac.dto.UserDto;
import com.ex.prac.entity.User;
import com.ex.prac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j // ← 추가
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto signup(SignupRequestDto dto) {
        log.info("회원가입 요청 - username: {}, email: {}", dto.getUsername(), dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("중복 이메일: {}", dto.getEmail());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("중복 유저네임: {}", dto.getUsername());
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);

        UserDto result = userRepository.save(user).toDto();

        log.info("회원가입 성공 - userId: {}", user.getId());
        return result;
    }

    public UserDto login(LoginRequestDto dto) {
        log.info("로그인 요청 - email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("이메일 없음: {}", dto.getEmail());
                    return new IllegalArgumentException("존재하지 않는 이메일입니다.");
                });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("비밀번호 불일치 - email: {}", dto.getEmail());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.info("로그인 성공 - userId: {}", user.getId());
        return user.toDto();
    }
}
