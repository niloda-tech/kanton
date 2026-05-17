Feature: Extracting the script name from a .kt.md CLI file
  As a developer using kanton actions,
  I want the IDE to extract the script name from my source file,
  So that action tools can identify the correct scaffold directory and binary name.

  Scenario: Script name is extracted from a well-formed cli block
    Given I have a .kt.md file with a cli header line "greet:Say hello"
    When the IDE extracts the script name
    Then the script name is "greet"

  Scenario: Hyphenated script name is extracted correctly
    Given I have a .kt.md file with a cli header line "my-tool:A useful tool"
    When the IDE extracts the script name
    Then the script name is "my-tool"

  Scenario: No script name is found in a file with no cli block
    Given I have a .kt.md file with no cli block
    When the IDE extracts the script name
    Then no script name is found

  Scenario: No script name is found in an empty file
    Given I have an empty .kt.md file
    When the IDE extracts the script name
    Then no script name is found
