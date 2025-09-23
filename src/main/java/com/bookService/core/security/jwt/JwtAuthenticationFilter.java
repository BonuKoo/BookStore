package com.bookService.core.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /* OncePerRequestFilter : HTTP 요청에서 JWT 토큰을 추출하고 인증 정보를 설정하는 필터 클래스
        이걸 상속하면 HTTP 요청당 한 번만 실행되는 필터를 만들 수 있다. */

    // 토큰 검증 및 사용자 ID 추출을 위한 TokenProvider
    private final TokenProvider tokenProvider;

    // OPTIONS 요청은 필터를 건너뛰도록 설정 - CORS 사전 요청 등을 무시
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (request.getMethod().equals("OPTIONS")) {
            return true;    //OPTIONS 메서드는 필터 대상이 아님
        }

        return false;
    }

    // 요청에서 JWT 파싱 -> 검증 -> 인증 객체 생성 -> SecurityContext 설정
    // JWT 토큰을 꺼내서 검증하고, 정상적인 토큰이면 해당 사용자를 인증된 상태로 만들어주는 역할
    // 1. 요청 헤더에서 JWT 토큰 추출
    // 2. 추출한 토큰이 유효한지 확인
    // 3. 유효한 경우 토큰에 담긴 사용자 ID를 바탕으로 시큐리티 인증 객체 생성
    // 4. 인증 정보를 시큐리티 필터에 설정
    // 5. 다음 필터로 요청을 넘긴다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Authorization 헤더에서 JWT 토큰 파싱, 토큰 값에서 Bearer 부분을 제외하고 토큰 값만 꺼낸다.
            String token = parseBearerToken(request);

//            log.info("doFilterInternal");

            // 토큰이 존재하고, "null" 이 아닌 경우
            if (token != null && !token.equalsIgnoreCase("null")) {
                String userId = tokenProvider.validateAndGetUserIdVer2(token); // 토큰 검증 및 사용자 ID 추출
//                log.info("Authenticated user ID : " + userId);

                // 사용자 ID를 기반으로 인증 객체 생성 (권한 없음)
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null,
                       AuthorityUtils.NO_AUTHORITIES
                       // AuthorityUtils.createAuthorityList("ROLE_USER") //임시로 최소 권한 부여
                );

                // 요청 정보 추가 (IP, 세션 등등)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 새로운 Security Context 생성 및 인증 객체 설정
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                // 현재 Thread에 SecurityContext 등록
                SecurityContextHolder.setContext(securityContext);
            }
        } catch (Exception ex) {
            //예외 발생 시 로그 출력
            log.error("Could not set user authentication in security context", ex);
        }
        // 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
    // Authorization 헤더에서 Bearer 토큰만 추출하는 메서드
    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");    // Authorization 헤더 가져오기
        
        // 헤더 값이 있고 "Bearer"로 시작하면 토큰만 추출하여 반환
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer" 이후 문자열 추출
        }

        return null;
    }

}

/**
 * 사용자가 로그인에 성공하면 서버는 JWT를 발급해서 클라이언트에 전달
 * 이후 클라이언트는 요청을 보낼 때마다, Authorization : Bearer <Token> 형식으로 헤더에 토큰을 담아 전송
 * 요청이 들어오면, JwtAuthenticationFilter가 작동해서, 헤더에 담긴 토큰을 꺼내고, 유효한 토큰인지 검증
 * 토큰이 유효하다면, 토큰 안에 들어 있는 사용자 ID를 추출해서
 * 스프링 시큐리티에 SecurityContext에 인증 정보를 등록한다.
 * 인증이 완료되면, 이후 컨트롤러나 서비스에선 @AuthenticaiionPrincipal 또는
 * SecurityContextHolder를 통해 현재 로그인한 사용자 정보에 접근할 수 있게 된다.
 *
 * 즉 JWT 기반 인증은 한번 로그인으로 토큰을 발급받고, 그 토큰만 있으면 매번 로그인 없이도
 * 안전하게 인증된 사용자임을 확인할 수 있는 구조이다.
 * */