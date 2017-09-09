package es.rubenjgarcia.test.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import es.rubenjgarcia.test.tests.BaseTest;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.yaml.snakeyaml.Yaml;

public class RestSteps extends BaseTest {

  private static final String HTTP_METHODS = "get|post|put|head|delete|options|patch|trace|GET|POST|PUT|HEAD|DELETE|OPTIONS|PATCH|TRACE";
  private static final String COUNT_COMPARISON = "(?: (less than|more than|at least|at most))?";
  private final StandardEvaluationContext spelCtx;

  private ResponseEntity<Object> response;
  private SpelExpressionParser parser;
  private HttpHeaders headers;

  public RestSteps() {
    parser = new SpelExpressionParser();
    spelCtx = new StandardEvaluationContext();
    spelCtx.addPropertyAccessor(new MapAsPropertyAccessor());
  }

  @Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\"$")
  public void iCall(String httpMethodString, String path) throws Throwable {
    call(httpMethodString, path);
  }

  @Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with data(?:[:])?$")
  public void iCallWithData(String httpMethodString, String path, String data) throws Throwable {
    Yaml yaml = new Yaml();
    Object obj = yaml.load(data);
    call(httpMethodString, path, obj);
  }

  @And("^I set headers to:$")
  public void iSetHeadersTo(DataTable headersTable) throws Throwable {
    headers = new HttpHeaders();
    headersTable.getGherkinRows()
        .forEach(r -> {
          String header = r.getCells().get(1);
          Object headerValue;

          Pattern pattern = Pattern.compile("\\$\\{(.*)}");
          Matcher matcher = pattern.matcher(header);
          if (matcher.find()) {
            String value = matcher.group(1);
            headerValue = matcher.replaceFirst(spelCtx.lookupVariable(value).toString());
          } else {
            headerValue = header;
          }
          headers.set(r.getCells().get(0), headerValue.toString());
        });
  }

  private void call(String httpMethodString, String path) {
    call(httpMethodString, path, null);
  }

  private void call(String httpMethodString, String path, Object requestObj) {
    HttpMethod httpMethod = HttpMethod.valueOf(StringUtils.upperCase(httpMethodString));
    if (httpMethod.equals(HttpMethod.GET) && requestObj != null) {
      throw new IllegalArgumentException("You can't pass data in a GET call");
    }

    HttpEntity<?> request = new HttpEntity<>(requestObj, this.headers);

    Pattern pattern = Pattern.compile("\\$\\{response.(.*)}");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      String value = matcher.group(1);
      path = matcher.replaceFirst((String) parser.parseRaw(value).getValue(spelCtx, response.getBody()));
    }

    response = restTemplate.exchange(path, httpMethod, request, getResponseType());
  }

  private Class getResponseType() {
    Class responseType = Object.class;
    if (this.headers != null && this.headers.getAccept() != null) {
      if (this.headers.getAccept().contains(MediaType.TEXT_PLAIN)) {
        responseType = String.class;
      } else if (this.headers.getAccept().contains(MediaType.APPLICATION_OCTET_STREAM)) {
        responseType = byte[].class;
      }
    }
    return responseType;
  }

  @Then("^The response status should be (\\d+)$")
  public void theResponseStatusShouldBe(int status) throws Throwable {
    assertEquals(status, response.getStatusCodeValue());
  }

  @And("^The response should contains empty array$")
  public void theResponseShouldContainsEmptyArray() {
    assertResponseListSize(0);
  }

  @And("^The response size is (\\d+)$")
  public void theResponseSizeIs(int size) throws Throwable {
    assertResponseListSize(size);
  }

  private List assertResponseListSize(int size) {
    Object body = response.getBody();
    assertNotNull(body);
    assertTrue(List.class.isAssignableFrom(body.getClass()));
    List listBody = (List) body;
    assertEquals(size, listBody.size());
    return listBody;
  }

  @And("^The response entity should contains \"([^\"]*)\"$")
  public void theResponseEntityShouldContains(String key) throws Throwable {
    assertResponseContainsKey(key);
  }

  @And("^The response entity should not contains \"([^\"]*)\"$")
  public void theResponseEntityShouldNotContains(String key) throws Throwable {
    assertResponseNotContainsKey(key);
  }

  @And("^The response entity should contains \"([^\"]*)\" with value \"([^\"]*)\"$")
  public void theResponseEntityShouldContainsWithValue(String key, String value) throws Throwable {
    Map bodyMap = assertResponseContainsKey(key);
    assertEquals(value, bodyMap.get(key));
  }

  @And("^The response entity should contains \"([^\"]*)\" with value ([^\"]*)$")
  public void theResponseEntityShouldContainsWithValue(String key, Integer value) throws Throwable {
    Map bodyMap = assertResponseContainsKey(key);
    assertEquals(value, bodyMap.get(key));
  }

  @And("^The response entity \"([^\"]*)\" should contain(?:s)? " + COUNT_COMPARISON + "(\\d+) entit(?:ies|y)$")
  public Object theResponseEntityContainEntities(String entityName, String comparisonAction, int childCount) {
    Map bodyMap = assertResponseIsMap();
    Object entity = parser.parseRaw(entityName).getValue(spelCtx, bodyMap);
    assertNotNull(entity);
    assertTrue(entity instanceof Collection);
    compareCounts(comparisonAction, childCount, ((Collection) entity).size());
    return entity;
  }

  @And("^The response entity \"([^\"]*)\" should contain(?:s)? \"([^\"]*)\"$")
  public Object theResponseEntityContain(String entity, String key) {
    Map<String, Object> value = getResponseEntityAsMap(entity);
    assertNotNull(value.get(key));
    return value;
  }

  @Then("^The response entity \"([^\"]*)\" should not contains \"([^\"]*)\"$")
  public void theResponseEntityShouldNotContains(String entity, String key) throws Throwable {
    Map<String, Object> value = getResponseEntityAsMap(entity);
    assertFalse(value.containsKey(key));
  }

  private Map<String, Object> getResponseEntityAsMap(String entity) {
    Map bodyMap = assertResponseIsMap();
    Object object = parser.parseRaw(entity).getValue(spelCtx, bodyMap);
    assertTrue(object instanceof Map);
    return (Map<String, Object>) object;
  }

  @And("^The response entity \"([^\"]*)\" should contain(?:s)? \"([^\"]*)\" with value \"([^\"]*)\"$")
  public void theResponseEntityContainKeyWithValueString(String entity, String key, String value) {
    Map<String, Object> entityValue = (Map<String, Object>) theResponseEntityContain(entity, key);
    assertEquals(value, entityValue.get(key));
  }

  @And("^The response entity \"([^\"]*)\" should contain(?:s)? \"([^\"]*)\" with value ([^\"]*)$")
  public void theResponseEntityContainKeyWithValueInt(String entity, String key, Integer value) {
    Map<String, Object> entityValue = (Map<String, Object>) theResponseEntityContain(entity, key);
    assertEquals(value, entityValue.get(key));
  }

  @And("^The response entity \"([^\"]*)\" should have value \"([^\"]*)\"$")
  public void theResponseRntityShouldHaveValue(String entity, String value) throws Throwable {
    Map bodyMap = assertResponseIsMap();
    Object object = parser.parseRaw(entity).getValue(spelCtx, bodyMap);
    assertEquals(value, object);
  }

  @And("^The response entity \"([^\"]*)\" should contains array(?:[:])?$")
  public void theResponseEntityShouldContainsArray(String entity, String data) throws Throwable {
    Map bodyMap = assertResponseIsMap();
    Object entityObj = parser.parseRaw(entity).getValue(spelCtx, bodyMap);
    assertTrue(List.class.isAssignableFrom(entityObj.getClass()));
    Yaml yaml = new Yaml();
    Object dataObj = yaml.load(data);
    assertTrue(List.class.isAssignableFrom(dataObj.getClass()));
    assertTrue(((List) entityObj).containsAll((List) dataObj));
    assertTrue(((List) dataObj).containsAll((List) entityObj));
  }

  @And("^The response entity should contains array(?:[:])?$")
  public void theResponseShouldContainsArray(String data) throws Throwable {
    Yaml yaml = new Yaml();
    Object dataObj = yaml.load(data);
    assertTrue(List.class.isAssignableFrom(dataObj.getClass()));
    List listData = (List) dataObj;
    List listBody = assertResponseListSize(listData.size());
    assertTrue(listBody.containsAll(listData));
  }

  private void compareCounts(String comparison, int expected, int actual) {
    if (StringUtils.equals("at least", comparison)) {
      assertTrue(actual >= expected);
    } else if (StringUtils.equals("at most", comparison)) {
      assertTrue(actual <= expected);
    } else if (StringUtils.equals("more than", comparison)) {
      assertTrue(actual > expected);
    } else if (StringUtils.equals("less than", comparison)) {
      assertTrue(actual < expected);
    } else {
      assertEquals(expected, actual);
    }
  }

  private Map assertResponseIsMap() {
    Object body = response.getBody();
    assertNotNull(body);
    assertTrue(Map.class.isAssignableFrom(body.getClass()));
    return (Map) response.getBody();
  }

  private Map assertResponseContainsKey(String key) {
    return assertResponseContainsOrNotKey(key, true);
  }

  private Map assertResponseNotContainsKey(String key) {
    return assertResponseContainsOrNotKey(key, false);
  }

  private Map assertResponseContainsOrNotKey(String key, boolean shouldContains) {
    Map bodyMap = assertResponseIsMap();
    boolean containsKey = bodyMap.containsKey(key);
    assertEquals(containsKey, shouldContains);
    return bodyMap;
  }

  @And("^The response is empty$")
  public void theResponseIsEmpty() throws Throwable {
    assertNull(response.getBody());
  }

  private Tuple2<String, ? extends AbstractResource> getFileTuple(Tuple2<String, String> t) {
    switch (t._2) {
      case "null":
        return Tuple.of(t._1, null);
      case "empty":
        return Tuple.of(t._1, new InputStreamResource(new ByteArrayInputStream(new byte[]{})));
      default:
        FileSystemResource fileSystemResource = new FileSystemResource(new File(RestSteps.class.getResource("/").getPath() + t._2));
        assertTrue(fileSystemResource.exists());
        return Tuple.of(t._1, fileSystemResource);
    }
  }

  private void callWithFiles(String httpMethodString, String path, List<Tuple2<String, ? extends AbstractResource>> files) {
    HttpMethod httpMethod = HttpMethod.valueOf(StringUtils.upperCase(httpMethodString));
    HttpHeaders headers = Optional.ofNullable(this.headers).orElse(new HttpHeaders());
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    files.forEach(f -> map.add(f._1, f._2));

    HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
    response = restTemplate.exchange(path, httpMethod, request, getResponseType());
  }

  @Given("^I call (" + HTTP_METHODS + ") \"([^\"]*)\" with (null|empty) file \"([^\"]*)\"$")
  public void iCallWithFileNull(String httpMethodString, String path, String type, String file) throws Throwable {
    callWithFiles(httpMethodString, path, Collections.singletonList(getFileTuple(Tuple.of(file, type))));
  }

  @And("^The response body is \"([^\"]*)\"$")
  public void theResponseBodyIs(String body) {
    assertTrue(String.class.isAssignableFrom(this.response.getBody().getClass()));
    assertTrue(body.equals(this.response.getBody()));
  }

  @And("^The response body starts with \"([^\"]*)\"$")
  public void theResponseBodyStartsWith(String body) {
    assertTrue(String.class.isAssignableFrom(this.response.getBody().getClass()));
    assertTrue(this.response.getBody().toString().startsWith(body));
  }

  @And("^The response body (|doesn't\\s)contains \"([^\"]*)\"$")
  public void theResponseBodyContainsOrNot(String hasToContain, String find) throws Throwable {
    assertTrue(String.class.isAssignableFrom(this.response.getBody().getClass()));
    boolean contains = this.response.getBody().toString().contains(find);
    assertTrue(hasToContain.equals("") ? contains : !contains);
  }

  @And("^I set variable \"([^\"]*)\" with value \"([^\"]*)\"$")
  public void iSetVariableWithValue(String name, String value) {
    Object varValue;

    if (value.contains("${response.")) {
      Pattern pattern = Pattern.compile("\\$\\{response.(.*)}");
      Matcher matcher = pattern.matcher(value);
      if (matcher.find()) {
        String varLookup = matcher.group(1);
        varValue = matcher.replaceFirst((String) parser.parseRaw(varLookup).getValue(spelCtx, response.getBody()));
      } else {
        throw new IllegalArgumentException("Wrong expression: " + value);
      }
    } else if (value.equals("${response}")) {
      varValue = response.getBody();
    } else {
      varValue = value;
    }

    spelCtx.setVariable(name, varValue);
  }

  public static class MapAsPropertyAccessor implements PropertyAccessor {

    @Override
    public Class<?>[] getSpecificTargetClasses() {
      return null;
    }

    @Override
    public boolean canRead(EvaluationContext evaluationContext, Object o, String s) throws AccessException {
      return o instanceof Map && ((Map) o).containsKey(s);
    }

    @Override
    public TypedValue read(EvaluationContext evaluationContext, Object o, String s) throws AccessException {
      return new TypedValue(((Map) o).get(s));
    }

    @Override
    public boolean canWrite(EvaluationContext evaluationContext, Object o, String s) throws AccessException {
      return false;
    }

    @Override
    public void write(EvaluationContext evaluationContext, Object o, String s, Object o2) throws AccessException {

    }
  }
}
