package es.rubenjgarcia.test.steps;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import es.rubenjgarcia.user.document.User;
import es.rubenjgarcia.user.UserRepository;
import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

public class MongoSteps {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private UserRepository userRepository;

  @Before
  public void before() {
    userRepository.deleteAll();
  }

  @Given("^I have an user in database$")
  public void iHaveAUserInDatabase() throws IOException {
    saveFromJson("user/user.json", User.class, userRepository);
  }

  private <T> T saveFromJson(String path, Class<T> modelClazz, MongoRepository<T, ? extends Serializable> repository) throws IOException {
    T entity = getEntityFromJson(path, modelClazz);
    return repository.save(entity);
  }

  private <T> T getEntityFromJson(String path, Class<T> modelClazz) throws IOException {
    String json = IOUtils.toString(MongoSteps.class.getResourceAsStream("/data/" + path), "UTF-8");
    DBObject dbObject = (DBObject) JSON.parse(json);
    return mongoTemplate.getConverter().read(modelClazz, dbObject);
  }
}
