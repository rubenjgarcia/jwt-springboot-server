package es.rubenjgarcia.controller;

import es.rubenjgarcia.controller.response.InvalidResponse;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorController {

  @Autowired
  private MessageSource messageSource;

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleDocumentNotFoundException(final NotFoundException e) {

  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public InvalidResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
    final Map<String, String> errors = List.ofAll(e.getBindingResult().getFieldErrors())
        .map(f -> Tuple.of(f.getField(), f.getDefaultMessage()))
        .foldLeft(HashMap.empty(), HashMap::put);

    return InvalidResponse.builder()
        .error(messageSource.getMessage("error.validationErrors", null, LocaleContextHolder.getLocale()))
        .errors(errors)
        .build();
  }
}
