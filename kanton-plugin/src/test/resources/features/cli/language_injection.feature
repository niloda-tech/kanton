Feature: Kotlin assistance inside fenced cli blocks
  As a developer editing a .kt.md CLI script,
  I want completion, inspections, and navigation to work inside my fenced code blocks,
  So that I can write and refactor Kotlin with full IDE support.

  Scenario: The run body is identified in a well-formed cli block
    Given I have a .kt.md file with a fenced cli block that has a header line, option declarations, and a run body
    When the IDE analyses the block for Kotlin assistance
    Then the run body is identified as the region to assist with
    And the region starts after the blank separator line

  Scenario: No run body is identified when the block has no blank separator
    Given I have a .kt.md file with a fenced cli block that has no blank separator line
    When the IDE analyses the block for Kotlin assistance
    Then no run body is identified

  Scenario: No run body is identified in an empty cli block
    Given I have a .kt.md file with a fenced cli block that is empty
    When the IDE analyses the block for Kotlin assistance
    Then no run body is identified

  Scenario: Run body at the end of a block without a trailing newline is handled correctly
    Given I have a .kt.md file with a fenced cli block whose run body has no trailing newline
    When the IDE analyses the block for Kotlin assistance
    Then the run body is identified
    And the identified region stays within the block boundary

  Scenario: Blank separator at the very end of the block does not overshoot the boundary
    Given I have a .kt.md file with a fenced cli block whose blank separator is the last line
    When the IDE analyses the block for Kotlin assistance
    Then the identified region stays within the block boundary

  Scenario: Kotlin assistance is provided inside a fenced cli block
    Given I have a .kt.md file with a fenced cli block
    When the IDE prepares Kotlin assistance for the block
    Then Kotlin language features are active inside the block
    And standard Clikt command APIs are available without explicit imports

  Scenario: Kotlin assistance is not provided inside a fenced dependencies block
    Given I have a .kt.md file with a fenced dependencies block
    When the IDE prepares assistance for the block
    Then the dependencies block does not receive the Clikt command scaffold
