Feature: Syncing library changes back to source
  As a developer authoring a library with kanton,
  I want my edits in the temporary build files to persist in my markdown source,
  So that I don't lose work when refactoring.

  Scenario: Sync a library with a missing source file
    Given no library source file exists
    When I sync the library back
    Then the library sync should report a failure
    And the library failure reason should indicate the source file was not found

  Scenario: Sync a library without a prior scaffold
    Given a valid .kt.md library fixture file
    And no library scaffold exists for the fixture
    When I sync the library back
    Then the library sync should report a failure
    And the library failure reason should indicate the project needs to be scaffolded first

  Scenario: Sync an unmodified library scaffold produces no changes
    Given a valid .kt.md library fixture file
    And the library fixture has been scaffolded
    When I sync the library back
    Then no library changes are written to the source file

  Scenario: Sync edits from the library scaffold back to source
    Given a valid .kt.md library fixture file
    And the library fixture has been scaffolded
    And the library body has been edited to add "(edited)"
    When I sync the library back
    Then the library source file is updated with the edits
    And the library source file contains "(edited)"
