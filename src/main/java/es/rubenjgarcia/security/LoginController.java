package es.rubenjgarcia.security;

import es.rubenjgarcia.security.request.LoginRequest;
import es.rubenjgarcia.user.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.servlet.ServletException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

  @Value("${jwt.secret}")
  private String secret;

  @Autowired
  private UserService userService;

  @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> login(@RequestBody @Valid final LoginRequest login) throws ServletException {
    final boolean existUser = userService.existUser(login.getEmail(), login.getPassword());
    if (!existUser) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    final Instant now = Instant.now();

    final String jwt = Jwts.builder()
        .setSubject(login.getEmail())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(1, ChronoUnit.DAYS)))
        .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode(secret))
        .compact();
    return new ResponseEntity<>(jwt, HttpStatus.OK);
  }

}
