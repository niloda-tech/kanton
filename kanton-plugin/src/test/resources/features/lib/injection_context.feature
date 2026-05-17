Feature: Injection context for fenced lib blocks
  As a developer editing a .kt.md library file,
  I want the IDE to prepend dependency annotations to injected Kotlin,
  So that I get accurate IDE assistance for library code with external dependencies.

  Scenario: A dependencies block has no injection wrapping
    Given I have a .kt.md file with a fenced dependencies block
    When the IDE prepares the injection context for a dependencies block
    Then the injected code has no prefix or suffix wrapping

  Scenario: Dependencies produce DependsOn annotations
    Given I have a lib block with dependencies declared
    When the IDE builds the dependency injection prefix
    Then the prefix includes DependsOn annotations for each dependency
    And each annotation is on its own line

  Scenario: No dependencies produce an empty prefix
    Given I have a lib block with no dependencies
    When the IDE builds the dependency injection prefix
    Then the prefix is empty
