package es.rubenjgarcia.user;

import es.rubenjgarcia.user.controller.response.UserJson;
import es.rubenjgarcia.user.document.User;

public interface UserBuilder {

  static UserJson buildUserJson(final User user) {
    return UserJson.builder()
        .email(user.getEmail())
        .name(user.getName())
        .surname(user.getSurname())
        .build();
  }
}
