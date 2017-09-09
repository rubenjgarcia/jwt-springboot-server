package es.rubenjgarcia.user;

import static es.rubenjgarcia.user.document.UserPatterns.$User;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import es.rubenjgarcia.controller.NotFoundException;
import es.rubenjgarcia.security.PasswordUtils;
import es.rubenjgarcia.user.controller.response.UserJson;
import es.rubenjgarcia.user.document.User;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public boolean existUser(final String email, String password) {
    final User user = userRepository.findOne(email);
    return Match(user).of(
        Case($User($(), $(h -> PasswordUtils.verifyPassword(password, h))), (e, h) -> true),
        Case($(), false)
    );
  }

  public UserJson getUserByEmail(final String email) throws NotFoundException {
    final User user = userRepository.findOne(email);
    return Option.of(user)
        .map(UserBuilder::buildUserJson)
        .getOrElseThrow(NotFoundException::new);
  }

  public UserJson registerUser(final String email, final String password, final String name, final String surname) {
    final User user = User.builder()
        .email(email)
        .name(name)
        .surname(surname)
        .hash(PasswordUtils.createHash(password))
        .build();
    final User savedUser = userRepository.save(user);
    return UserBuilder.buildUserJson(savedUser);
  }
}
