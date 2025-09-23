package com.bookService.core.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** 요청 당 한 번만 실행되는 커스텀 필터 클래스
 *  시큐리티에서 소셜 로그인을 처리할 때, 클라이언트가 요청한 리다이렉션 URL을 쿠키에 저장하는 용도로 사용
 *  소셜 로그인 요청 시, 클라이언트가 보낸 redirect_url 파라미터를 쿠키로 저장해 놓고
 *  로그인 완료 후 해당 URL로 리다이렉트 하기 위한 기반을 마련 */
@Slf4j
@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {
    /**
    /== OncePerRequestFilter ==/
        스프링 시큐리티에서 제공하는 필터 클래스 : 요청 당 한번만 실행된다.
     */
    public static final String REDIRECT_URI_PARAM = "redirect_url"; // 요청 파라미터 및 쿠키 이름

    private static final int MAX_AGE = 180; // 쿠키 유효 시간 : 180초

    // 필터 로직을 구현하는 메서드
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // oauth2/authorization 으로 시작하는 요청이 들어오면 처리
        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            try {
                log.info("request uri {} ", request.getRequestURI()); // 요청 URI 로그 출력
                String redirectUrl = request.getParameter(REDIRECT_URI_PARAM); // redirect_ul 파라미터 가져오기

                // 쿠키 생성 및 설정
                Cookie cookie = new Cookie(REDIRECT_URI_PARAM, redirectUrl); // 쿠키 이름과 값 설정
                cookie.setPath("/");                                         // 전체 경로에서 쿠키 사용 가능하도록 설정
                cookie.setHttpOnly(true);                                    // 자바 스크립트에서 접근 불가능하도록 설정 ( 보안 강화 )
                cookie.setMaxAge(MAX_AGE);                                   // 쿠키 만료 시간 설정 (180초)
                response.addCookie(cookie);

            } catch (Exception ex) {
                log.error("Could not set user authentication in security context", ex);
                log.info("Unauthorized request");
            }

        }
        filterChain.doFilter(request, response);
    }
}
