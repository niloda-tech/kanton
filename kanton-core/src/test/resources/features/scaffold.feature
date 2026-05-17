Feature: Scaffolding a script for IDE editing
  As a developer using kanton,
  I want to open my script as a real Kotlin project in my IDE,
  So that I can edit it with code completion and type checking.

  Scenario: Scaffold a script from a missing source file
    Given no source file exists
    When I scaffold the script
    Then the process should fail
    And the error message should indicate the source file was not found

  Scenario: Scaffold creates a ready-to-edit project
    Given a valid .kt.md fixture file
    When I scaffold the script
    Then an editable project is created for the script
    And the project contains a build file
    And the project contains CLI stubs

  Scenario: Scaffold generates a class named after the script
    Given a valid .kt.md fixture file
    When I scaffold the script
    Then the generated class is named after the script
    And the generated class has a run method

  Scenario: Scaffold includes declared options in the generated class
    Given a valid .kt.md fixture file
    When I scaffold the script
    Then the generated class includes the declared options
