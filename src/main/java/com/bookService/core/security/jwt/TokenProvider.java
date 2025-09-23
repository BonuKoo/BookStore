package com.bookService.core.security.jwt;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.security.vo.CustomUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
// JWT 토큰을 생성하고 검증하는 역할을 수행
public class TokenProvider {

//    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // JWT 서명에 사용할 비밀 키 ( 512 비트 이상 추천 )
    private static final String SECRET_KEY = "FlRpX30pMqDbiAkmlfArbrmVkDD4RqISskGZmBFax5oGVxzXXWUzTR5JyskiHMIV9M1Oicegkpi46AdvrcX1E6CmTUBc6IFbTPiD";
    //비밀 키로부터 HMAC SHA 키 객체 생성
    private final Key SIGNING_KEY;
    private final JwtParser jwtParser;

    public TokenProvider(
            @Value("${jwt.secret}")
            String secret, JwtParser jwtParser){
        this.SIGNING_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = jwtParser;
    }

    // 사용자 정보를 기반으로 JWT 토큰 생성
    public String create(AccountEntity userEntity) {
        // 토큰만료 시간을 현재 시각으로부터 1일 뒤로 설정
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        // JWT 생성 및 반환
        return Jwts.builder()
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)    // 서명 알고리즘과 키 설정
                .setSubject(String.valueOf(userEntity.getId()))     // 사용자 ID를 subject로 설정
                .setIssuer("demo app")                              // 토큰 발급자 정보 설정
                .setIssuedAt(new Date())                            // 토큰 발급 시간 설정
                .setExpiration(expiryDate)                          // 만료 시간 설정
                .compact();                                         // 토큰 생성 완료
    }

    // 토큰을 검증하고, 포함된 사용자 ID(subject)를 반환
    public String validateAndGetUserId(String token) {
        // 토큰 파싱 및 검증 ( 서명이 유효한 지 확인 )
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)     //서명 키 설정
                .build()
                .parseClaimsJws(token)// 토큰 파싱
                .getBody();           // Payload(Cliams) 추출

        return claims.getSubject();     //사용자 ID (subject) 반환
    }

    // GC 방지 리팩토링
    public String validateAndGetUserIdVer2(String token) {
        // 토큰 파싱 및 검증 ( 서명이 유효한 지 확인 )
        Claims claims = jwtParser
                .parseClaimsJws(token)// 토큰 파싱
                .getBody();           // Payload(Cliams) 추출

        return claims.getSubject();     //사용자 ID (subject) 반환
    }

    public String create(final Authentication authentication) {
        CustomUser userPrincipal = (CustomUser) authentication.getPrincipal();

        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        return Jwts.builder()
                .setSubject(userPrincipal.getName())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    // 사용자 ID만을 기반으로 토큰 생성 (단순히 userId 하나만 받아서 JWT를 생성)
    // 이미 사용자 ID를 갖고 있고, 그 아이디만으로 토큰을 만들 때 사용
    private String createByUserId(final Long userId) {
        // 만료일 1일 후 설정
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
        // 토큰 생성
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 사용자 ID 설정
                .setIssuedAt(new Date())            // 발급 시각
                .setExpiration(expiryDate)          // 만료 시각
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512) //서명
                .compact();                         // 최종 토큰 문자열 생성
    }


}
