package es.rubenjgarcia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
    final StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
    stringHttpMessageConverter.setWriteAcceptCharset(false);
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    converters.add(stringHttpMessageConverter);
    converters.add(new ByteArrayHttpMessageConverter());
  }
}