Feature: Me features

  Scenario: I can see my user information
    Given I have an user in database
    When I set headers to:
      | accept | text/plain |
    And I call POST "/login" with data:
    """
      email: "foo@bar.com"
      password: "1234"
    """
    Then The response status should be 200
    And I set variable "token" with value "${response}"

    Given I set headers to:
      | Authorization | Bearer ${token} |
    When I call GET "/api/me"
    Then The response status should be 200
    And The response entity should contains "name" with value "Foo"
    And The response entity should contains "surname" with value "Bar"
    And The response entity should contains "email" with value "foo@bar.com"

  Scenario: I want see my user information with wrong token
    Given I set headers to:
      | Authorization | Bearer token |
    When I call GET "/api/me"
    Then The response status should be 401

  Scenario: I want see my user information without token
    When I call GET "/api/me"
    Then The response status should be 401