package es.rubenjgarcia.test.tests;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {BaseTest.TestConfig.class})
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public abstract class BaseTest {

  @Autowired
  protected TestRestTemplate restTemplate;

  @TestConfiguration
  public static class TestConfig extends AbstractMongoConfiguration {

    private static final Fongo FONGO = new Fongo("mongo-test");

    @Override
    public String getDatabaseName() {
      return "testdb";
    }

    @Bean
    @Override
    public Mongo mongo() {
      return FONGO.getMongo();
    }

  }
}
