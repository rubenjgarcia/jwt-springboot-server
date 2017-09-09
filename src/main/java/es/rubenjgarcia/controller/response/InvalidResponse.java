package es.rubenjgarcia.controller.response;

import io.vavr.collection.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvalidResponse {

  private String error;
  private Map<String, String> errors;
}
