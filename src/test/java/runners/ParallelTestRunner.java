package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Parallel Test Runner for Cucumber tests
 * Enables parallel execution at scenario level
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"stepDefinitions", "hooks"},
        tags = "@smoke or @regression",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/parallel-cucumber-html-report.html",
                "json:target/cucumber-json-reports/parallel-cucumber.json",
                "junit:target/cucumber-xml-reports/parallel-cucumber.xml",
                "timeline:target/cucumber-reports/parallel-timeline",
                "usage:target/cucumber-reports/parallel-usage.json"
        },
        monochrome = true,
        publish = false
)
public class ParallelTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}