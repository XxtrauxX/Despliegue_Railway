package com.projectspringboot.a.proyecspringboot.security;





import com.projectspringboot.a.proyecspringboot.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Clave secreta fija. En producción, esto debería cargarse desde un archivo de propiedades.
    private static final String SECRET_KEY_STRING = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    
    private final Key signingKey;

    public JwtService() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY_STRING);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().getNombre());
        claims.put("nombreUsuario", usuario.getNombreUsuario());

        return buildToken(claims, usuario.getNombreUsuario());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    

    private String buildToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(this.signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}