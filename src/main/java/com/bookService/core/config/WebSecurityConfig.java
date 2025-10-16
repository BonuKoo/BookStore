package com.bookService.core.config;

import com.bookService.core.security.jwt.JwtAuthenticationFilter;
import com.bookService.core.security.jwt.OAuthSuccessHandler;
import com.bookService.core.security.jwt.RedirectUrlCookieFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
//@EnableWebSecurity
public class WebSecurityConfig {

    //JWT 인증 필터 의존성 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final RedirectUrlCookieFilter redirectUrlFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                             OAuthSuccessHandler oAuthSuccessHandler,
                             RedirectUrlCookieFilter redirectUrlFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.oAuthSuccessHandler = oAuthSuccessHandler;
        this.redirectUrlFilter = redirectUrlFilter;
    }
    // 보안 필터 체인 정의 : 인증, 인가, 세션, 예외 처리, JWT 필터 설정
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //cors 설정 활성화 : FE과 BE가 서로 다른 도메인에 있을 때, 교차 출처 요청(CORS) 할 수 있게 된다.
                .cors(cors -> {})
                // CSRF 보호 비활성화 -> 보통 세션, 쿠키 등을 사용하지 않기 때문
                .csrf(csrf -> csrf.disable())
                // 기본 인증 방식 비활성화 -> id와 비밀번호를 매 요청마다 헤더에 보내는 것을, JWT 를 사용하기 때문에 할 필요 없다.
                .httpBasic(httpBasic -> httpBasic.disable())
                // 세션 사용X -> JWT 기반 인증
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )// 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/**").permitAll() // root 및 auth, 경로는 인증 없이 허용
                        .requestMatchers("/api/naver/**").permitAll()  //  네이버 API는 로그인 없이도 접근 가능
                        .requestMatchers("/v1/toss/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .anyRequest().authenticated()
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 이후에 실행되도록 추가
                 /** 문제 부분 */
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 필터
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuthSuccessHandler)
                )
                // 인증 실패 시 403 Forbidden 반환
                /** 미인증 요청이 컨트롤러까지 흘러가지 않도록, 403이 아닌 401로 수정*/
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                )
                .addFilterBefore(redirectUrlFilter, OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build(); // 필터 체인 객체 반환
    }
    // CORS 설정을 정의하는 Bean
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);    // 자격 증명 포함 허용 ( 예: 쿠키, Authorization 헤더 )
        configuration.setAllowedOrigins(List.of("http://localhost:3000","http://localhost:5173")); // 허용할 프론트엔드 도메인
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용 메서드
        configuration.setAllowedHeaders(List.of("*")); // 모든 요청 헤더 허용
        configuration.setExposedHeaders(List.of("*")); // 응답 헤더 노출

        // 위의 CORS 설정을 모든 경로에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 요청에 대해 설정 적용
        return source;
    }

}
