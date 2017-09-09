Feature: Register features

  Scenario: I can register with valid data
    Given I call POST "/register" with data:
    """
      name: "Foo"
      surname: "Bar"
      email: "foo@bar.com"
      password: "1234"
    """
    Then The response status should be 200
    And The response entity should contains "name" with value "Foo"
    And The response entity should contains "surname" with value "Bar"
    And The response entity should contains "email" with value "foo@bar.com"

  Scenario Outline: I can't register with invalid data
    Given I call POST "/register" with data:
    """
      name: "<name>"
      surname: "<surname>"
      email: "<email>"
      password: "<password>"
    """
    Then The response status should be 400
    And The response entity should contains "error" with value "There are validation errors"
    And The response entity "errors" should contains "<field>" with value "may not be empty"
    Examples:
      | name | surname | email       | password | field    |
      |      | Bar     | foo@bar.com | 1234     | name     |
      | Foo  |         | foo@bar.com | 1234     | surname  |
      | Foo  | Bar     |             | 1234     | email    |
      | Foo  | Bar     | foo@bar.com |          | password |