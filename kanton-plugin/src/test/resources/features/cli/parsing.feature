Feature: Building an editable document structure from a .kt.md CLI script
  As a developer editing a .kt.md CLI script,
  I want the IDE to model the file as a structured document,
  So that refactoring, navigation, and inspections work correctly across the whole file.

  Scenario: An empty file produces an empty document
    Given I have an empty .kt.md file
    When the IDE builds the document structure
    Then the document contains no elements

  Scenario: A file with only a cli block produces a single code element
    Given I have a .kt.md file with a single fenced cli block
    When the IDE builds the document structure
    Then the document contains one element
    And that element is a code block
    And the code block is identified as a cli block

  Scenario: A file with text followed by a code block produces two elements
    Given I have a .kt.md file with plain text followed by a fenced cli block
    When the IDE builds the document structure
    Then the document contains two elements
    And the first element is a text region
    And the second element is a code block

  Scenario: A file with a dependencies block is correctly structured
    Given I have a .kt.md file with a single fenced dependencies block
    When the IDE builds the document structure
    Then the document contains one element
    And the code block is identified as a dependencies block

  Scenario: A file with multiple blocks produces one element per block
    Given I have a .kt.md file with a fenced dependencies block followed by a fenced cli block
    When the IDE builds the document structure
    Then the document contains two elements
    And the first element is a dependencies block
    And the second element is a cli block

  Scenario: A code block can host injected language features
    Given I have a .kt.md file with a fenced cli block
    When the IDE builds the document structure
    Then the cli block is eligible to host language assistance
