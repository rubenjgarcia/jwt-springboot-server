package es.rubenjgarcia.user.controller;

import es.rubenjgarcia.controller.NotFoundException;
import es.rubenjgarcia.user.UserService;
import es.rubenjgarcia.user.controller.response.UserJson;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

  @Autowired
  private UserService userService;

  @RequestMapping(method = RequestMethod.GET)
  public UserJson getUser(@RequestAttribute("claims") final Claims claims) throws NotFoundException {
    return userService.getUserByEmail(claims.getSubject());
  }

}
