Feature: Removing a scaffolded library
  As a developer authoring a library with kanton,
  I want to clean up the temporary build project when I'm done editing,
  So that I don't accumulate stale project directories on my machine.

  Scenario: Remove a library that has not been scaffolded, by artifact
    Given no library scaffold exists
    When I delete the library scaffold by artifact
    Then the library scaffold should fail
    And the library error message should indicate the project was not found

  Scenario: Remove an existing library scaffold by artifact
    Given a library scaffold exists
    When I delete the library scaffold by artifact
    Then the library project directory no longer exists

  Scenario: Remove an existing library scaffold by source file
    Given a valid .kt.md library fixture file
    And the library fixture has been scaffolded
    When I delete the library scaffold by source file
    Then the library project directory no longer exists
