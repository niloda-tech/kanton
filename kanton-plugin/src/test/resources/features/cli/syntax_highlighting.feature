Feature: Syntax highlighting of .kt.md CLI scripts
  As a developer editing a .kt.md CLI script,
  I want fence markers, language tags, code content, and plain text to be visually distinct,
  So that I can quickly tell which parts of the file are code and which are descriptive text.

  Scenario: Fence markers are highlighted as keywords
    Given I have a .kt.md file with a fenced cli block
    When the editor applies syntax highlighting
    Then the fence markers are highlighted as keywords

  Scenario: The language tag is highlighted as metadata
    Given I have a .kt.md file with a fenced cli block
    When the editor applies syntax highlighting
    Then the language tag is highlighted as metadata

  Scenario: Code content is highlighted as template language
    Given I have a .kt.md file with a fenced cli block
    When the editor applies syntax highlighting
    Then the code content is highlighted as template language

  Scenario: Plain text is highlighted as documentation
    Given I have a .kt.md file containing only plain text
    When the editor applies syntax highlighting
    Then the plain text is highlighted as documentation

  Scenario: A file with mixed content has distinct highlighting for each region
    Given I have a .kt.md file with plain text before and after a fenced cli block
    When the editor applies syntax highlighting
    Then the fence markers are highlighted as keywords
    And the language tag is highlighted as metadata
    And the code content is highlighted as template language
    And the plain text is highlighted as documentation
