Feature: Compiling a script to a binary
  As a developer using kanton,
  I want to distribute my script as a self-contained executable,
  So that others can run it without needing Kotlin or kanton installed.

  Scenario: Compile a script from a missing source file
    Given no source file exists
    When I compile the script
    Then the process should fail
    And the error message should indicate the source file was not found

  Scenario: Compile a file that contains no valid script
    Given a .kt.md file with no run body
    When I compile the script
    Then the process should fail
    And the error message should indicate the script could not be parsed

  Scenario: Compile a script for the first time without a prior scaffold
    Given a valid .kt.md fixture file
    And no scaffold exists for the fixture
    When I compile the script
    Then a self-contained executable is produced
