@regression @user
Feature: User Management API
  As an API consumer
  I want to manage user accounts
  So that I can create, update, retrieve and delete users

  Background:
    Given the API is available
    And I have valid authentication credentials

  @smoke @user-creation
  Scenario: Create a new user successfully
    Given I have valid user data
    When I send a POST request to create a user
    Then the response status code should be 201
    And the response should contain the created user details
    And the user should have a valid ID
    And the response time should be less than 2000 milliseconds

  @smoke @user-retrieval
  Scenario: Retrieve an existing user
    Given a user exists in the system
    When I send a GET request to retrieve the user
    Then the response status code should be 200
    And the response should contain the user details
    And all required fields should be present

  @user-creation @validation
  Scenario: Attempt to create user with invalid data
    Given I have invalid user data
    When I send a POST request to create a user
    Then the response status code should be 400
    And the response should contain validation errors
    And the error message should indicate the specific validation failures

  @user-update
  Scenario: Update an existing user
    Given a user exists in the system
    And I have updated user data
    When I send a PUT request to update the user
    Then the response status code should be 200
    And the response should contain the updated user details
    And the updated fields should reflect the changes

  @user-deletion
  Scenario: Delete an existing user
    Given a user exists in the system
    When I send a DELETE request to remove the user
    Then the response status code should be 204
    And the user should no longer exist in the system

  @user-retrieval @error-handling
  Scenario: Attempt to retrieve non-existent user
    Given I have an ID for a non-existent user
    When I send a GET request to retrieve the user
    Then the response status code should be 404
    And the response should contain an appropriate error message

  @user-management @data-driven
  Scenario Outline: Create users with different data sets
    Given I have user data for "<userType>"
    When I send a POST request to create a user
    Then the response status code should be <expectedStatus>
    And the response should contain "<expectedField>"

    Examples:
      | userType        | expectedStatus | expectedField |
      | validUser       | 201           | id            |
      | adminUser       | 201           | id            |
      | guestUser       | 201           | id            |
      | invalidUser     | 400           | error         |

  @user-authentication @security
  Scenario: Access user data without authentication
    Given I do not have authentication credentials
    When I send a GET request to retrieve user data
    Then the response status code should be 401
    And the response should contain an authentication error

  @user-permissions @security
  Scenario: Access restricted user data with insufficient permissions
    Given I have guest user authentication credentials
    When I send a GET request to retrieve admin user data
    Then the response status code should be 403
    And the response should contain a permissions error

  @user-profile @user-update
  Scenario: Update user profile information
    Given a user exists in the system
    And I have profile update data
    When I send a PATCH request to update user profile
    Then the response status code should be 200
    And the profile information should be updated
    And the timestamp should reflect the recent update

  @user-search @query-parameters
  Scenario: Search for users with query parameters
    Given multiple users exist in the system
    When I send a GET request to search users with filters
      | parameter | value    |
      | role      | admin    |
      | active    | true     |
      | limit     | 10       |
    Then the response status code should be 200
    And the response should contain a list of matching users
    And all returned users should match the search criteria
    And the response should include pagination information

  @user-validation @edge-cases
  Scenario: Handle special characters in user data
    Given I have user data with special characters
    When I send a POST request to create a user
    Then the response status code should be 201
    And the special characters should be properly encoded
    And the user data should be retrievable correctly

  @performance @user-management
  Scenario: Bulk user operations performance
    Given I have data for multiple users
    When I send multiple POST requests to create users
    Then all requests should complete within acceptable time limits
    And all users should be created successfully
    And the system should handle concurrent requests properly