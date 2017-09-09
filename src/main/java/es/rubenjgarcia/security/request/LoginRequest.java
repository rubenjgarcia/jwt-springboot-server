package es.rubenjgarcia.security.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class LoginRequest {

  @NotEmpty
  private String email;

  @NotEmpty
  private String password;

}
