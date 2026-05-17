Feature: Scaffolding a library for IDE editing
  As a developer authoring a library with kanton,
  I want to open my library as a real Kotlin project in my IDE,
  So that I can edit it with code completion and type checking.

  Scenario: Scaffold a library from a missing source file
    Given no library source file exists
    When I scaffold the library
    Then the library scaffold should fail
    And the library error message should indicate the source file was not found

  Scenario: Scaffold creates a ready-to-edit library project
    Given a valid .kt.md library fixture file
    When I scaffold the library
    Then an editable library project is created
    And the library project contains a build file
    And the library project contains a library source file

  Scenario: Scaffold generates a class named after the artifact
    Given a valid .kt.md library fixture file
    When I scaffold the library
    Then the library source file contains a class named after the artifact
