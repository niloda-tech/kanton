Feature: Removing a scaffolded project
  As a developer using kanton,
  I want to clean up the temporary build project when I'm done editing,
  So that I don't accumulate stale project directories on my machine.

  Scenario: Remove a project that has not been scaffolded, by name
    Given no scaffolded project exists
    When I delete the scaffold by name
    Then the process should fail
    And the error message should indicate the project was not found

  Scenario: Remove an existing scaffolded project by name
    Given a scaffolded project exists
    When I delete the scaffold by name
    Then the project directory no longer exists

  Scenario: Remove a project using a missing source file
    Given no source file exists
    When I delete the scaffold by source file
    Then the process should fail
    And the error message should indicate the source file was not found

  Scenario: Remove an existing scaffolded project by source file
    Given a valid .kt.md fixture file
    And a scaffolded project exists
    When I delete the scaffold by source file
    Then the project directory no longer exists
