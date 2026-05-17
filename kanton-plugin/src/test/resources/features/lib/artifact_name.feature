Feature: Extracting the artifact name from a .kt.md library file
  As a developer using kanton actions,
  I want the IDE to extract the artifact name from my library file,
  So that action tools can identify the correct scaffold directory and publish coordinates.

  Scenario: Artifact name is extracted from a well-formed lib block
    Given I have a .kt.md file with a lib header line "com.example:mylib:0.1.0:A utility library"
    When the IDE extracts the artifact name
    Then the artifact name is "mylib"

  Scenario: Hyphenated artifact name is extracted correctly
    Given I have a .kt.md file with a lib header line "com.example:my-utils:1.0.0:Utility functions"
    When the IDE extracts the artifact name
    Then the artifact name is "my-utils"

  Scenario: No artifact name is found in a file with no lib block
    Given I have a .kt.md file with no lib block
    When the IDE extracts the artifact name
    Then no artifact name is found

  Scenario: No artifact name is found in an empty file
    Given I have an empty .kt.md file
    When the IDE extracts the artifact name
    Then no artifact name is found
