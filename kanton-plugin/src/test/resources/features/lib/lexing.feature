Feature: Recognising structure in a .kt.md library file
  As a developer editing a .kt.md library file,
  I want the IDE to understand which parts are plain text and which are fenced code blocks,
  So that syntax highlighting and editing assistance apply to the right regions.

  Scenario: A file with only plain text is recognised
    Given I have a .kt.md file containing only plain text
    When the editor parses the file
    Then the entire content is treated as descriptive text
    And no code block is detected

  Scenario: A fenced lib block is detected
    Given I have a .kt.md file with a fenced lib block
    When the editor parses the file
    Then a fenced code block is detected
    And the block is identified as a lib block

  Scenario: A fenced deps block is detected
    Given I have a .kt.md file with a single fenced deps block
    When the editor parses the file
    Then a fenced code block is detected
    And the block is identified as a deps block

  Scenario: Text before and after a code block is preserved
    Given I have a .kt.md file with plain text before and after a fenced lib block
    When the editor parses the file
    Then the text regions before and after the block are treated as descriptive text
    And the fenced block is identified as a lib block

  Scenario: Multiple fenced blocks in one file are all detected
    Given I have a .kt.md file with a fenced deps block followed by a fenced lib block
    When the editor parses the file
    Then two code blocks are detected
    And the first block is identified as a deps block
    And the second block is identified as a lib block

  Scenario: An empty fenced block is handled gracefully
    Given I have a .kt.md file with a fenced lib block that has no content
    When the editor parses the file
    Then a fenced code block is detected
    And the block has no code content

  Scenario: An unclosed fenced block does not crash the editor
    Given I have a .kt.md file with a fenced lib block that is never closed
    When the editor parses the file
    Then the open block is treated as having code content up to the end of the file
    And no error prevents the file from being displayed
