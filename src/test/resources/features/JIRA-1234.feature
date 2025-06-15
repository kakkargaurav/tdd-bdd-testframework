@JIRA-1234
Feature: Australian Bank Payment Initiation API Testing
  As an API consumer
  I want to test all payment initiation scenarios
  So that I can ensure the API works correctly for all Australian payment types

  Background:
    Given the payment initiation API is available
    And I have valid API authentication

  @JIRA-1234 @smoke @health
  Scenario: Verify API health status
    When I send a GET request to health endpoint
    Then the response status code should be 200
    And the response should contain health status information
    And the response time should be less than 2000 milliseconds

  @JIRA-1234 @smoke @service-info
  Scenario: Retrieve service information
    When I send a GET request to service info endpoint
    Then the response status code should be 200
    And the response should contain BIAN service domain information
    And the response should contain service instance details

  @JIRA-1234 @npp @positive
  Scenario: Initiate successful NPP instant payment
    Given I have valid NPP instant payment data
    When I send a POST request to initiate NPP payment
    Then the response status code should be 201
    And the response should contain payment instruction reference
    And the payment should be initiated successfully
    And the response time should be less than 5000 milliseconds

  @JIRA-1234 @npp @positive
  Scenario: Initiate successful NPP PayID payment
    Given I have valid NPP PayID payment data
    When I send a POST request to initiate NPP payment
    Then the response status code should be 201
    And the response should contain payment instruction reference
    And the PayID payment should be processed correctly

  @JIRA-1234 @npp @negative
  Scenario: NPP payment with insufficient funds
    Given I have NPP payment data with insufficient funds scenario
    When I send a POST request to initiate NPP payment
    Then the response status code should be 400
    And the response should contain insufficient funds error
    And the error should indicate specific validation failure

  @JIRA-1234 @npp @negative
  Scenario: NPP payment with invalid account
    Given I have NPP payment data with invalid account scenario
    When I send a POST request to initiate NPP payment
    Then the response status code should be 400
    And the response should contain invalid account error

  @JIRA-1234 @npp @approval
  Scenario: NPP payment requiring approval for high amount
    Given I have NPP payment data requiring approval scenario
    When I send a POST request to initiate NPP payment
    Then the response status code should be 201
    And the payment should be in pending authorization status
    And the payment should require manual approval

  @JIRA-1234 @npp @crud
  Scenario: Update NPP payment before processing
    Given I have initiated an NPP payment
    When I send a PUT request to update the NPP payment
    Then the response status code should be 200
    And the payment should be updated successfully
    And the updated details should be reflected in response

  @JIRA-1234 @npp @crud
  Scenario: Submit NPP payment for processing
    Given I have initiated an NPP payment
    When I send a POST request to submit the payment for processing
    Then the response status code should be 200
    And the payment should be submitted for processing

  @JIRA-1234 @npp @crud
  Scenario: Retrieve NPP payment details
    Given I have initiated an NPP payment
    When I send a GET request to retrieve the payment details
    Then the response status code should be 200
    And the response should contain complete payment information
    And the payment status should be available

  @JIRA-1234 @npp @negative
  Scenario: Retrieve non-existent NPP payment
    Given I have a non-existent payment ID
    When I send a GET request to retrieve the payment details
    Then the response status code should be 404
    And the response should contain payment not found error

  @JIRA-1234 @npp @control
  Scenario: Cancel NPP payment
    Given I have initiated an NPP payment
    When I send a PUT request to cancel the payment
    Then the response status code should be 200
    And the payment should be cancelled successfully

  @JIRA-1234 @npp @control
  Scenario: Suspend and resume NPP payment
    Given I have initiated an NPP payment
    When I send a PUT request to suspend the payment
    Then the response status code should be 200
    And the payment should be suspended
    When I send a PUT request to resume the payment
    Then the response status code should be 200
    And the payment should be resumed

  @JIRA-1234 @npp @query
  Scenario: Query NPP payments with filters
    Given multiple NPP payments exist in the system
    When I send a GET request to query payments with status filter
    Then the response status code should be 200
    And the response should contain filtered payment list
    And the pagination information should be included

  @JIRA-1234 @becs @positive
  Scenario: Initiate successful BECS direct entry payment
    Given I have valid BECS payment data
    When I send a POST request to initiate BECS payment
    Then the response status code should be 201
    And the response should contain BECS payment instruction reference
    And the BECS payment should be initiated successfully

  @JIRA-1234 @bpay @positive
  Scenario: Initiate successful BPAY bill payment
    Given I have valid BPAY payment data
    When I send a POST request to initiate BPAY payment
    Then the response status code should be 201
    And the response should contain BPAY payment instruction reference
    And the BPAY payment should be validated correctly

  @JIRA-1234 @direct-debit @positive
  Scenario: Initiate successful Direct Debit mandate
    Given I have valid Direct Debit payment data
    When I send a POST request to initiate Direct Debit payment
    Then the response status code should be 201
    And the response should contain Direct Debit instruction reference
    And the mandate should be created successfully

  @JIRA-1234 @domestic-wire @positive
  Scenario: Initiate successful Domestic Wire Transfer
    Given I have valid Domestic Wire payment data
    When I send a POST request to initiate Domestic Wire payment
    Then the response status code should be 201
    And the response should contain wire transfer instruction reference
    And the high-value payment should be initiated successfully

  @JIRA-1234 @domestic-wire @crud
  Scenario: Complete Domestic Wire Transfer lifecycle
    Given I have initiated a Domestic Wire Transfer
    When I send a PUT request to update the wire transfer
    Then the response status code should be 200
    When I send a POST request to submit the wire transfer for processing
    Then the response status code should be 200
    When I send a GET request to retrieve the wire transfer details
    Then the response status code should be 200
    And the complete wire transfer information should be available

  @JIRA-1234 @domestic-wire @control
  Scenario: Control Domestic Wire Transfer operations
    Given I have initiated a Domestic Wire Transfer
    When I send a PUT request to suspend the wire transfer
    Then the response status code should be 200
    When I send a PUT request to resume the wire transfer
    Then the response status code should be 200
    When I send a PUT request to cancel the wire transfer
    Then the response status code should be 200

  @JIRA-1234 @domestic-wire @exchange
  Scenario: Handle Domestic Wire Transfer status updates
    Given I have initiated a Domestic Wire Transfer
    When I send a POST request to exchange wire transfer status
    Then the response status code should be 200
    And the status update should be processed correctly

  @JIRA-1234 @international-wire @positive
  Scenario: Initiate successful International Wire Transfer
    Given I have valid International Wire payment data
    When I send a POST request to initiate International Wire payment
    Then the response status code should be 201
    And the response should contain international wire instruction reference
    And the SWIFT payment should be initiated successfully

  @JIRA-1234 @international-wire @negative
  Scenario: International Wire Transfer compliance failure
    Given I have International Wire payment data with compliance issues
    When I send a POST request to initiate International Wire payment
    Then the response status code should be 400
    And the response should contain compliance failure error
    And the blocked country scenario should be handled

  @JIRA-1234 @international-wire @crud
  Scenario: Complete International Wire Transfer lifecycle
    Given I have initiated an International Wire Transfer
    When I send a PUT request to update the international wire transfer
    Then the response status code should be 200
    When I send a POST request to submit the international wire for processing
    Then the response status code should be 200
    When I send a GET request to retrieve the international wire details
    Then the response status code should be 200

  @JIRA-1234 @international-wire @control
  Scenario: Control International Wire Transfer operations
    Given I have initiated an International Wire Transfer
    When I send a PUT request to control the international wire transfer
    Then the response status code should be 200
    And the control action should be executed successfully

  @JIRA-1234 @international-wire @exchange
  Scenario: Handle International Wire Transfer notifications
    Given I have initiated an International Wire Transfer
    When I send a POST request to exchange international wire status
    Then the response status code should be 200
    And the international status update should be processed

  @JIRA-1234 @testing @scenarios
  Scenario Outline: Retrieve test scenarios for different payment types
    When I send a GET request to get "<paymentType>" test scenarios
    Then the response status code should be 200
    And the response should contain available test scenarios
    And the scenarios should include description and expected results

    Examples:
      | paymentType        |
      | NPP                |
      | BECS               |
      | BPAY               |
      | Direct Debit       |
      | Domestic Wire      |
      | International Wire |

  @JIRA-1234 @testing @data-generation
  Scenario: Generate test data for specific scenario
    Given I have a test scenario index
    When I send a POST request to generate test data for the scenario
    Then the response status code should be 200
    And the response should contain generated test payment data
    And the data should be valid for the specified scenario

  @JIRA-1234 @validation @schema
  Scenario Outline: Validate request schema for different payment types
    Given I have invalid "<paymentType>" payment data with missing required fields
    When I send a POST request to initiate "<paymentType>" payment
    Then the response status code should be 400
    And the response should contain schema validation errors
    And the error should specify missing required fields

    Examples:
      | paymentType        |
      | NPP                |
      | BECS               |
      | BPAY               |
      | Direct Debit       |
      | Domestic Wire      |
      | International Wire |

  @JIRA-1234 @validation @amount
  Scenario Outline: Validate monetary amount formats
    Given I have "<paymentType>" payment data with invalid amount format
    When I send a POST request to initiate "<paymentType>" payment
    Then the response status code should be 400
    And the response should contain amount format validation error

    Examples:
      | paymentType        |
      | NPP                |
      | BECS               |
      | Domestic Wire      |

  @JIRA-1234 @validation @account
  Scenario Outline: Validate account reference formats
    Given I have "<paymentType>" payment data with invalid account format
    When I send a POST request to initiate "<paymentType>" payment
    Then the response status code should be 400
    And the response should contain account validation error

    Examples:
      | paymentType        |
      | NPP                |
      | BECS               |
      | BPAY               |

  @JIRA-1234 @performance @load
  Scenario: Test API performance under concurrent load
    Given I have multiple valid payment requests
    When I send multiple concurrent requests to initiate payments
    Then all requests should complete within acceptable time limits
    And the system should handle concurrent requests properly
    And no requests should fail due to concurrency issues

  @JIRA-1234 @security @authentication
  Scenario: Test API authentication requirements
    Given I do not have valid API authentication
    When I send a POST request to initiate any payment
    Then the response status code should be 401
    And the response should contain authentication error

  @JIRA-1234 @error-handling @server-errors
  Scenario Outline: Handle server error responses
    Given the API server returns a "<errorCode>" error
    When I send a request to any payment endpoint
    Then the response status code should be <errorCode>
    And the response should contain appropriate error information
    And the error response should follow standard format

    Examples:
      | errorCode |
      | 500       |
      | 503       |

  @JIRA-1234 @integration @end-to-end
  Scenario: Complete payment integration flow
    Given I have valid payment data for multiple payment types
    When I initiate payments of different types
    And I update the payments with new information
    And I submit the payments for processing
    And I retrieve the payment statuses
    Then all payments should progress through correct lifecycle stages
    And the payment statuses should be accurately tracked
    And the integration should work seamlessly across payment types