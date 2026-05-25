package com.backend.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Getter
    @Value("${jwt.expiration}")
    private long expiration;

    @Getter
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    @Getter
    @Value("${jwt.prefix}")
    private String prefix;

    @Getter
    @Value("${jwt.header}")
    private String header;

    @Getter
    @Value("${jwt.token-type}")
    private String tokenType;

    private SecretKey signingKey;

    @PostConstruct
    void initialize() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generateAccessToken(Authentication authentication) {
        return generateAccessToken(authentication, Map.of());
    }

    public String generateAccessToken(Authentication authentication, Map<String, Object> extraClaims) {
        Instant now = Instant.now();

        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(authentication.getName())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .claim("authorities", authorities)
                .claim("type", "ACCESS")
                .claims(extraClaims)
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(authentication.getName())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpiration)))
                .claim("type", "REFRESH")
                .signWith(signingKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            log.warn("JWT parsing failed: {}", ex.getMessage());
            return null;
        }
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public List<String> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return List.of();

        Object raw = claims.get("authorities");
        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .toList();
        }

        return List.of();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return username != null
                && username.equals(expectedUsername)
                && !isTokenExpired(token)
                && isAccessToken(token);
    }

    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        return claims == null || claims.getExpiration().before(Date.from(Instant.now()));
    }

    public long getRemainingExpiration(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return 0;
        long expireAt = claims.getExpiration().getTime();
        long now      = Instant.now().toEpochMilli();
        return Math.max(0, (expireAt - now) / 1000);
    }

    private boolean isAccessToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null && "ACCESS".equals(claims.get("type"));
    }
}