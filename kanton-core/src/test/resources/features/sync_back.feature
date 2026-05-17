Feature: Syncing changes back to source
  As a developer using kanton,
  I want my edits in the temporary build files to persist in my markdown source
  So that I don't lose work when refactoring.

  Scenario: Run sync on a missing source file
    Given no source file exists
    When I attempt to sync back
    Then the process should fail
    And the error message should indicate the source file was not found

  Scenario: Run sync without a prior scaffold
    Given a valid .kt.md fixture file
    And no scaffold exists for the fixture
    When I attempt to sync back
    Then the process should fail
    And the error message should indicate the project needs to be scaffolded first

  Scenario: Sync an unmodified scaffold produces no changes
    Given a valid .kt.md fixture file
    And the fixture has been scaffolded
    When I sync back
    Then no changes are written to the source file

  Scenario: Sync edits from the build directory back to source
    Given a valid .kt.md fixture file
    And the fixture has been scaffolded
    And the run body in Main.kt has been edited to add "(edited)"
    When I sync back
    Then the source file is updated with the edits
    And the fixture file contains "(edited)"
