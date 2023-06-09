package com.gumi.enjoytrip.security.filter;

import com.gumi.enjoytrip.domain.user.entity.User;
import com.gumi.enjoytrip.security.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromRequest(request);
        if (isPermitAllRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (StringUtils.hasText(token)) {
            // 토큰이 있는 경우
            if (tokenService.verifyToken(token)) {
                User user = tokenService.getUserFromToken(token);
                Authentication authentication = getAuthentication(user);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("인증 성공");
                log.info("이메일 : " + ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
                log.info("권한 : " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                filterChain.doFilter(request, response);
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Authentication getAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())));
    }

    private boolean isPermitAllRequest(HttpServletRequest request) {
        List<RequestMatcher> matchers = new ArrayList<>();

        // HttpSecurity 클래스에서 permitAll()로 허용한 URL 패턴 가져오기
        matchers.add(new AntPathRequestMatcher("/api/v1/users/login"));
        matchers.add(new AntPathRequestMatcher("/api/v1/users/join"));
        matchers.add(new AntPathRequestMatcher("/api/v1/users/refresh-token"));
        matchers.add(new AntPathRequestMatcher("/api/v1/users/images/**"));
        matchers.add(new AntPathRequestMatcher("/api/v1/hot-places/images/**"));
        matchers.add(new AntPathRequestMatcher("/api/v1/tours/**"));
        matchers.add(new AntPathRequestMatcher("/api/v1/posts/home"));
        matchers.add(new AntPathRequestMatcher("/"));

        matchers.add(new AntPathRequestMatcher("/v3/api-docs/**"));
        matchers.add(new AntPathRequestMatcher("/swagger-ui.html"));
        matchers.add(new AntPathRequestMatcher("/swagger-ui/**"));

        matchers.add(new AntPathRequestMatcher("/actuator/**"));

        // 요청 URL이 permitAll()로 허용한 URL 패턴에 해당하는지 확인
        for (RequestMatcher matcher : matchers) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }
}
