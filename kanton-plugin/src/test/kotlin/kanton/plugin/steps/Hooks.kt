package kanton.plugin.steps

import io.cucumber.java.After

class Hooks {

    @After
    fun resetScenarioContext() {
        PluginScenarioContext.reset()
    }
}
