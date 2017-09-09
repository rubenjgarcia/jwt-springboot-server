package es.rubenjgarcia.user.document;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.match.annotation.Patterns;
import io.vavr.match.annotation.Unapply;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document
@Patterns
public class User {

  @Id
  private String email;
  private String name;
  private String surname;
  private String hash;

  @Unapply
  static Tuple2<String, String> User(final User user) {
    return Tuple.of(user.getEmail(), user.getHash());
  }
}
