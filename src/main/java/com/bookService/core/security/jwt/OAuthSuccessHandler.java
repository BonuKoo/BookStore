package com.bookService.core.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.bookService.core.security.jwt.RedirectUrlCookieFilter.REDIRECT_URI_PARAM;

@Slf4j
@AllArgsConstructor
@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String LOCAL_REDIRECT_URL = "http://localhost:3000";

    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String token = tokenProvider.create(authentication);

        log.info("token {}", token);

        Optional<Cookie> oCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                .findFirst();
        Optional<String> redirectUri = oCookie.map(Cookie::getValue);

        log.info("redirectUri {}", redirectUri);

        String targetUrl = redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL) + "/sociallogin?token=" + token;

        log.info("targetUrl {}", targetUrl);

        response.sendRedirect(targetUrl);
    }

    /**
     * @apiNote 기존 방식 -> TokenProvider를 메서드 내에서 생성
     * 메서드가 호출될 때마다, 새로운 TokenProvider 객체 생성
     * 객체 내부에서 다시 JwtParser와 같은 객체를 반복적으로 생성하게 되어서
     * GC 오버헤드와 CPU 낭비가 심해져서 성능 병목
     * -> 수정
     * TokenProvider를 싱글톤 빈으로 단 한번 생성이후 메서드에 주입
     * -> 불필요한 객체 생성과 GC 부하가 사라진다.
     */
    /*
    public void onAuthenticationSuccess2(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        TokenProvider tokenProvider = new TokenProvider();
        String token = tokenProvider.create(authentication);

        log.info("token {}", token);

        Optional<Cookie> oCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(REDIRECT_URI_PARAM))
                .findFirst();
        Optional<String> redirectUri = oCookie.map(Cookie::getValue);

        log.info("redirectUri {}", redirectUri);

        String targetUrl = redirectUri.orElseGet(() -> LOCAL_REDIRECT_URL) + "/sociallogin?token=" + token;

        log.info("targetUrl {}", targetUrl);

        response.sendRedirect(targetUrl);
    }
    */


}
