Feature: Injection context for fenced cli blocks
  As a developer editing a .kt.md CLI script,
  I want cli blocks to be wrapped in a CliktCommand scaffold,
  So that I get accurate IDE assistance for Clikt-based scripts.

  Scenario: A cli block is wrapped in a CliktCommand scaffold
    Given I have a .kt.md file with a fenced cli block
    When the IDE prepares the injection context for a cli block
    Then the injected code is wrapped in a CliktCommand class
    And the standard Clikt imports are included

  Scenario: A dependencies block has no injection wrapping
    Given I have a .kt.md file with a fenced dependencies block
    When the IDE prepares the injection context for a dependencies block
    Then the injected code has no prefix or suffix wrapping

  Scenario: A run block includes dependency function delegates
    Given I have a run block with dependency functions
    When the IDE builds the run block prefix
    Then the prefix includes the dependency function delegates
    And the prefix opens a run method

  Scenario: A run block includes option variable declarations
    Given I have a run block with option declarations
    When the IDE builds the run block prefix
    Then the prefix includes the option declarations
    And the prefix opens a run method

  Scenario: A run block with no dependencies or options still opens a CliktCommand
    Given I have a run block with no dependencies or options
    When the IDE builds the run block prefix
    Then the prefix opens a CliktCommand class
    And the prefix opens a run method
