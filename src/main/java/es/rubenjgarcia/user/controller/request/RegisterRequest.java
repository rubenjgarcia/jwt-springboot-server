package es.rubenjgarcia.user.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class RegisterRequest {

  @NotEmpty
  private String email;

  @NotEmpty
  private String name;

  @NotEmpty
  private String surname;

  @NotEmpty
  private String password;

}
