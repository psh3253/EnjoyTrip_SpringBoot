package com.gumi.enjoytrip.domain.user.service;

import com.gumi.enjoytrip.domain.user.dto.UserPasswordUpdateDto;
import com.gumi.enjoytrip.domain.user.dto.UserUpdateDto;
import com.gumi.enjoytrip.domain.user.entity.Role;
import com.gumi.enjoytrip.domain.user.entity.User;
import com.gumi.enjoytrip.domain.user.exception.DuplicateEmailException;
import com.gumi.enjoytrip.domain.user.exception.InvalidPasswordException;
import com.gumi.enjoytrip.domain.user.exception.UserNotFoundException;
import com.gumi.enjoytrip.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User login(String email, String password, PasswordEncoder passwordEncoder) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }

    @Transactional
    public void signup(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            throw new DuplicateEmailException("중복된 이메일입니다.");
        }
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .build());
    }

    public User getLoginUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public void updateUser(UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userUpdateDto.getId()).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        userRepository.save(userUpdateDto.toEntity());
    }

    public void passwordCheck(String email, String password, PasswordEncoder passwordEncoder) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    public void passwordChange(@RequestBody UserPasswordUpdateDto userPasswordUpdateDto, PasswordEncoder passwordEncoder) {
        User user = userRepository.findById(userPasswordUpdateDto.getId()).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(userPasswordUpdateDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.save(userPasswordUpdateDto.toEntity(passwordEncoder));
    }
}
