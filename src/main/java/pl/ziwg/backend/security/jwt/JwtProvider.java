package pl.ziwg.backend.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.exception.TokenDoesNotExistsException;
import pl.ziwg.backend.property.JwtProperties;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;

import java.util.Date;

@Component
public class JwtProvider {
   private JwtProperties jwtProperties;


    @Autowired
    public JwtProvider(JwtProperties jwtProperties){
        this.jwtProperties = jwtProperties;
    }
    public String generateJwtToken(Authentication authentication) {
        UserPrinciple userPrincipal = (UserPrinciple) authentication.getPrincipal();

        return  Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtProperties.getExpiration() * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    public boolean validateJwtToken(String authToken) {
        Jwts.parser().setSigningKey(jwtProperties.getSecret()).parseClaimsJws(authToken);
        return true;
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody().getSubject();
    }
}
