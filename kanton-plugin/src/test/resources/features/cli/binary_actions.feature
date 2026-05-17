Feature: Locating and invoking the kanton binary for CLI actions
  As a developer using kanton,
  I want the IDE to find and run the kanton tool from my project
  So that I can scaffold, sync-back, compile, and delete without leaving the editor.

  Scenario: Binary found when it exists in an ancestor directory
    Given I have a source file nested below a directory that contains the kanton binary
    When the IDE searches for the kanton binary
    Then the binary is found

  Scenario: Binary not found when absent from all ancestors and the system path
    Given I have a source file in a directory with no kanton binary nearby
    When the IDE searches for the kanton binary
    Then no binary is found

  Scenario: Invoking the binary succeeds
    Given I have a kanton binary that reports success
    And I have a source file to process
    When the IDE invokes the binary
    Then the invocation succeeds

  Scenario: Invoking the binary captures failure output
    Given I have a kanton binary that exits with an error
    And I have a source file to process
    When the IDE invokes the binary
    Then the invocation indicates failure
    And the error output from the binary is captured
