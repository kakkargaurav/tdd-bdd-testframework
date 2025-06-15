package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Main Cucumber Test Runner
 * Configures Cucumber execution with TestNG
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepDefinitions", "hooks"},
        tags = "@smoke or @regression",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-html-report.html",
                "json:target/cucumber-json-reports/cucumber.json",
                "junit:target/cucumber-xml-reports/cucumber.xml",
                "timeline:target/cucumber-reports/timeline",
                "usage:target/cucumber-reports/usage.json"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}