Feature: Bootstrapping scripts with checksum-based caching
  As a developer using kanton,
  I want changed scripts to be recompiled automatically during bootstrap,
  So that my installed binaries always reflect the latest source.

  Scenario: Skip compilation when the binary is up-to-date
    Given a valid bootstrap fixture file
    And a compiled binary already exists for the fixture
    And the saved checksum matches the current source
    When I bootstrap the fixture
    Then the binary is not recompiled

  Scenario: Recompile when the source has changed since last compilation
    Given a valid bootstrap fixture file
    And a compiled binary already exists for the fixture
    And the saved checksum does not match the current source
    When I bootstrap the fixture
    Then the binary is recompiled
    And the saved checksum is updated to match the current source

  Scenario: First compilation saves a checksum alongside the binary
    Given a valid bootstrap fixture file
    And no compiled binary exists for the fixture
    When I bootstrap the fixture
    Then a compiled binary is produced for the fixture
    And a checksum is saved alongside the binary
