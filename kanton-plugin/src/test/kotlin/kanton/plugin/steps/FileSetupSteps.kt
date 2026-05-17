package kanton.plugin.steps

import io.cucumber.java.en.Given

class FileSetupSteps {

    // ── CLI file content (lexing and parsing scenarios) ───────────────────────

    @Given("I have a .kt.md file containing only plain text")
    fun fileWithOnlyPlainText() {
        PluginScenarioContext.input = "hello world\n"
    }

    @Given("I have a .kt.md file with a fenced cli block")
    fun fileWithFencedCliBlock() {
        PluginScenarioContext.input = "```cli\ngreet:Say hello\n--name, -n, Name\n\necho(\"hello\")\n```\n"
        PluginScenarioContext.injectionLang = "cli"
    }

    @Given("I have a .kt.md file with a fenced kotlin block")
    fun fileWithFencedKotlinBlock() {
        PluginScenarioContext.input = "```kotlin\necho(\"hello\")\n```\n"
        PluginScenarioContext.injectionLang = "kotlin"
    }

    @Given("I have a .kt.md file with a fenced dependencies block")
    fun fileWithFencedDependenciesBlock() {
        PluginScenarioContext.input = "```dependencies\n\"com.example:lib:1.0\"\n```\n"
        PluginScenarioContext.injectionLang = "dependencies"
    }

    @Given("I have a .kt.md file with plain text before and after a fenced cli block")
    fun fileWithTextAroundCliBlock() {
        PluginScenarioContext.input = "some text\n```cli\ncode\n```\nmore text\n"
    }

    @Given("I have a .kt.md file with a fenced dependencies block followed by a fenced cli block")
    fun fileWithDepsAndCliBlocks() {
        PluginScenarioContext.input = "```dependencies\ndep\n```\n```cli\ncode\n```\n"
    }

    @Given("I have a .kt.md file with a fenced cli block that has no content")
    fun fileWithEmptyFencedCliBlock() {
        PluginScenarioContext.input = "```cli\n```\n"
    }

    @Given("I have a .kt.md file with a fenced cli block that is never closed")
    fun fileWithUnclosedCliBlock() {
        PluginScenarioContext.input = "```cli\nsome code\n"
    }

    @Given("I have an empty .kt.md file")
    fun emptyFile() {
        PluginScenarioContext.input = ""
    }

    @Given("I have a .kt.md file with a single fenced cli block")
    fun fileWithSingleCliBlock() {
        PluginScenarioContext.input = "```cli\nval x = 1\n```\n"
        PluginScenarioContext.injectionLang = "cli"
    }

    @Given("I have a .kt.md file with plain text followed by a fenced cli block")
    fun fileWithTextFollowedByCliBlock() {
        PluginScenarioContext.input = "some text\n```cli\ncode\n```\n"
    }

    @Given("I have a .kt.md file with a single fenced dependencies block")
    fun fileWithSingleDepsBlock() {
        PluginScenarioContext.input = "```dependencies\n\"dep:1.0\"\n```\n"
        PluginScenarioContext.injectionLang = "dependencies"
    }

    // ── CLI block content (injection analysis scenarios) ──────────────────────

    @Given("I have a .kt.md file with a fenced cli block that has a header line, option declarations, and a run body")
    fun blockWithHeaderOptionsAndRunBody() {
        PluginScenarioContext.blockContent = "name:help\n--opt, -o, Option\n\nprintln(\"hi\")\n"
    }

    @Given("I have a .kt.md file with a fenced cli block that has no blank separator line")
    fun blockWithNoBlankSeparator() {
        PluginScenarioContext.blockContent = "name:help\n--opt, -o, Option\n"
    }

    @Given("I have a .kt.md file with a fenced cli block that is empty")
    fun blockThatIsEmpty() {
        PluginScenarioContext.blockContent = ""
    }

    @Given("I have a .kt.md file with a fenced cli block whose run body has no trailing newline")
    fun blockWithRunBodyNoTrailingNewline() {
        PluginScenarioContext.blockContent = "name:help\n\nprintln(\"done\")"
    }

    @Given("I have a .kt.md file with a fenced cli block whose blank separator is the last line")
    fun blockWithBlankSeparatorAtEnd() {
        PluginScenarioContext.blockContent = "name:help\n\n"
    }

    // ── Lib file content (lexing and parsing scenarios) ───────────────────────

    @Given("I have a .kt.md file with a fenced lib block")
    fun fileWithFencedLibBlock() {
        PluginScenarioContext.input = "```lib\ncom.example:mylib:1.0:A lib\n\nfun greet() = println(\"hi\")\n```\n"
        PluginScenarioContext.injectionLang = "lib"
    }

    @Given("I have a .kt.md file with plain text before and after a fenced lib block")
    fun fileWithTextAroundLibBlock() {
        PluginScenarioContext.input = "some text\n```lib\ncode\n```\nmore text\n"
    }

    @Given("I have a .kt.md file with a fenced deps block followed by a fenced lib block")
    fun fileWithDepsAndLibBlocks() {
        PluginScenarioContext.input = "```deps\napi com.example:lib:1.0\n```\n```lib\ncode\n```\n"
    }

    @Given("I have a .kt.md file with a fenced lib block that has no content")
    fun fileWithEmptyFencedLibBlock() {
        PluginScenarioContext.input = "```lib\n```\n"
    }

    @Given("I have a .kt.md file with a fenced lib block that is never closed")
    fun fileWithUnclosedLibBlock() {
        PluginScenarioContext.input = "```lib\nsome code\n"
    }

    @Given("I have a .kt.md file with a single fenced lib block")
    fun fileWithSingleLibBlock() {
        PluginScenarioContext.input = "```lib\ncom.example:mylib:1.0:A lib\n\nval x = 1\n```\n"
        PluginScenarioContext.injectionLang = "lib"
    }

    @Given("I have a .kt.md file with plain text followed by a fenced lib block")
    fun fileWithTextFollowedByLibBlock() {
        PluginScenarioContext.input = "some text\n```lib\ncode\n```\n"
    }

    @Given("I have a .kt.md file with a single fenced deps block")
    fun fileWithSingleFencedDepsBlock() {
        PluginScenarioContext.input = "```deps\napi com.example:lib:1.0\n```\n"
        PluginScenarioContext.injectionLang = "deps"
    }
}
