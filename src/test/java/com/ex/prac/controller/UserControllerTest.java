//package com.ex.prac.controller;
//
//import com.ex.prac.dto.LoginRequestDto;
//import com.ex.prac.dto.SignupRequestDto;
//import com.ex.prac.dto.UserDto;
//import com.ex.prac.repository.UserRepository;
//import com.ex.prac.service.UserService;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@SpringBootTest
//@Transactional // 테스트 후 자동 롤백
//class UserControllerTest {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    void 회원가입_로그인_정상작동() {
//        // given
//        SignupRequestDto signupDto = SignupRequestDto.builder()
//                .username("통합테스트유저")
//                .email("integration@example.com")
//                .password("1234")
//                .build();
//
//        // when: 회원가입
//        UserDto signupResult = userService.signup(signupDto);
//
//        // then: DB에 저장되었는지 확인
//        assertThat(userRepository.findByEmail("integration@example.com")).isPresent();
//        assertThat(signupResult.getUsername()).isEqualTo("통합테스트유저");
//
//        // when: 로그인
//        LoginRequestDto loginDto = LoginRequestDto.builder()
//                .email("integration@example.com")
//                .password("1234")
//                .build();
//
//        UserDto loginResult = userService.login(loginDto);
//
//        // then: 로그인 성공 여부 확인
//        assertThat(loginResult.getUsername()).isEqualTo("통합테스트유저");
//    }
//
//    @Test
//    void 로그인_실패_비밀번호틀림() {
//        // given
//        SignupRequestDto signupDto = SignupRequestDto.builder()
//                .username("통합테스트")
//                .email("wrongpass@example.com")
//                .password("correctpass")
//                .build();
//
//        userService.signup(signupDto);
//
//        // when
//        LoginRequestDto loginDto = LoginRequestDto.builder()
//                .email("wrongpass@example.com")
//                .password("wrongpass")
//                .build();
//
//        // then
//        assertThatThrownBy(() -> userService.login(loginDto))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("비밀번호가 일치하지 않습니다.");
//    }
//}
