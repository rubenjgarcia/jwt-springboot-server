Feature: Secuirty features

  Scenario: I can login with a right pasword
    Given I have an user in database
    When I set headers to:
      | accept | text/plain |
    And I call POST "/login" with data:
    """
      email: "foo@bar.com"
      password: "1234"
    """
    Then The response status should be 200
    And The response body starts with "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi"

  Scenario: I can't login with a wrong password
    Given I have an user in database
    When I set headers to:
      | accept | text/plain |
    And I call POST "/login" with data:
    """
      email: "foo@bar.com"
      password: "1235"
    """
    Then The response status should be 401
