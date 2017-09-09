package es.rubenjgarcia.test.tests;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "html:target/site/cucumber"},
        glue = {"es.rubenjgarcia.test.tests", "es.rubenjgarcia.test.steps"},
        features = "classpath:features")
public class CucumberTestCase {

}