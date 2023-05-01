package com.gumi.enjoytrip.domain.user.controller;

import com.gumi.enjoytrip.domain.user.entity.User;
import com.gumi.enjoytrip.domain.user.service.UserService;
import com.gumi.enjoytrip.security.dto.Token;
import com.gumi.enjoytrip.security.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserController {

    private final TokenService tokenService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "로그인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "401", description = "비밀번호가 일치하지 않습니다.")
    })
    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestParam String email, @RequestParam String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = userService.login(email, hashedPassword);
        return ResponseEntity.ok(tokenService.generateToken(user.getEmail(), user.getRole()));
    }

    @Operation(summary = "회원가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "중복된 이메일입니다.")
    })
    @PostMapping("/singup")
    public ResponseEntity<Void> signup(@RequestParam String email, @RequestParam String password, @RequestParam String nickname) {
        String hashedPassword = passwordEncoder.encode(password);
        userService.signup(email, hashedPassword, nickname);
        return ResponseEntity.created(null).build();
    }
}
