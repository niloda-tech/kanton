package kanton.plugin

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["classpath:features"],
    glue = ["kanton.plugin.steps"],
    plugin = ["pretty"]
)
class CucumberSuite
