package es.rubenjgarcia.user.controller;

import es.rubenjgarcia.controller.NotFoundException;
import es.rubenjgarcia.user.UserService;
import es.rubenjgarcia.user.controller.request.RegisterRequest;
import es.rubenjgarcia.user.controller.response.UserJson;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegistrerController {

  @Autowired
  private UserService userService;

  @RequestMapping(method = RequestMethod.POST)
  public UserJson registerUser(@RequestBody @Valid final RegisterRequest registerRequest) throws NotFoundException {
    return userService.registerUser(registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getName(), registerRequest.getSurname());
  }

}
