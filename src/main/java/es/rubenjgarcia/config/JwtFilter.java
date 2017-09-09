package es.rubenjgarcia.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.TextCodec;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

@WebFilter(urlPatterns = "/api/*")
public class JwtFilter implements Filter {

  @Value("${jwt.secret}")
  private String secret;

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;
    final String authHeader = request.getHeader("authorization");

    if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK);
      chain.doFilter(req, res);
    } else {

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      final String token = authHeader.substring(7);

      try {
        final Claims claims = Jwts.parser()
            .setSigningKey(TextCodec.BASE64.encode(secret))
            .parseClaimsJws(token)
            .getBody();
        request.setAttribute("claims", claims);
      } catch (final JwtException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      chain.doFilter(req, res);
    }
  }

  @Override
  public void destroy() {

  }
}