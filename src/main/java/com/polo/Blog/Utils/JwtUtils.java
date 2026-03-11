package com.polo.Blog.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    private static final int MIN_SECRET_LENGTH = 32;
    private static SecretKey key;
    private static long expiration = 1000L * 60 * 60 * 24;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-millis:86400000}")
    private long configuredExpiration;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret 未配置，请通过环境变量或外部配置文件提供 JWT_SECRET");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("jwt.secret 长度至少需要 32 个字符");
        }
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        expiration = configuredExpiration;
    }

    /**
     * 生成 Token (修改：增加 roleKey 参数)
     * @param id 用户id
     * @param roleKey 角色标识 (如 admin)
     */
    public static String generateToken(Long id, String roleKey) {
        ensureInitialized();
        // 你可以把 role 放在 map 里，也可以直接用 .claim
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roleKey);

        return Jwts.builder()
                .subject(id.toString()) // 标准字段：存用户名
                .claims(claims)    // 存入自定义权限字段
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token
     * @return 如果解析成功返回 Claims，失败返回 null
     */
    public static Claims parseToken(String token) {
        ensureInitialized();
        return Jwts.parser()
                .verifyWith(key) // 设置验签的 Key (新版写法)
                .build()
                .parseSignedClaims(token) // 解析
                .getPayload(); // 获取载荷 (旧版叫 getBody，新版叫 getPayload)
    }

    private static void ensureInitialized() {
        if (key == null) {
            throw new IllegalStateException("JwtUtils 尚未初始化，请检查 jwt.secret 配置");
        }
    }
}
