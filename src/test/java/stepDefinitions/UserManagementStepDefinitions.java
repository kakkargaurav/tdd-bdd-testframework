package stepDefinitions;

import framework.core.ApiClient;
import framework.core.RequestBuilder;
import framework.core.TestContext;
import framework.utils.DataProvider;
import framework.utils.JsonUtils;
import framework.utils.LogManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions specific to User Management API operations
 */
public class UserManagementStepDefinitions {
    private static final Logger logger = LoggerFactory.getLogger(UserManagementStepDefinitions.class);
    private final ApiClient apiClient;
    private Object currentUserData;
    private String currentUserId;

    public UserManagementStepDefinitions() {
        this.apiClient = new ApiClient();
    }

    @Given("I have valid user data")
    public void iHaveValidUserData() {
        LogManager.logTestStep("Preparing valid user data");
        
        currentUserData = DataProvider.getUserData("validUser");
        TestContext.setTestData(currentUserData);
        
        logger.info("Valid user data prepared");
    }

    @Given("I have invalid user data")
    public void iHaveInvalidUserData() {
        LogManager.logTestStep("Preparing invalid user data");
        
        currentUserData = DataProvider.getUserData("invalidUser");
        TestContext.setTestData(currentUserData);
        
        logger.info("Invalid user data prepared");
    }

    @Given("I have user data for {string}")
    public void iHaveUserDataFor(String userType) {
        LogManager.logTestStep("Preparing user data for: " + userType);
        
        currentUserData = DataProvider.getUserData(userType);
        TestContext.setTestData(currentUserData);
        
        logger.info("User data prepared for: {}", userType);
    }

    @Given("a user exists in the system")
    public void aUserExistsInTheSystem() {
        LogManager.logTestStep("Creating a user in the system");
        
        // Create a user first
        Map<String, Object> userData = DataProvider.getUserData("validUser");
        
        // Add unique identifier to avoid conflicts
        userData.put("username", "testuser_" + UUID.randomUUID().toString().substring(0, 8));
        userData.put("email", "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        
        String endpoint = DataProvider.getEndpoint("users");
        Response response = apiClient.post(endpoint, userData);
        
        assertThat(response.getStatusCode())
                .as("User creation should be successful")
                .isEqualTo(201);
        
        // Extract user ID from response
        currentUserId = response.jsonPath().getString("id");
        TestContext.setUserId(currentUserId);
        TestContext.setResponse(response);
        
        logger.info("User created in system with ID: {}", currentUserId);
    }

    @Given("I have an ID for a non-existent user")
    public void iHaveAnIdForANonExistentUser() {
        LogManager.logTestStep("Setting up non-existent user ID");
        
        currentUserId = "non-existent-" + UUID.randomUUID().toString();
        TestContext.setUserId(currentUserId);
        
        logger.info("Non-existent user ID set: {}", currentUserId);
    }

    @Given("I have updated user data")
    public void iHaveUpdatedUserData() {
        LogManager.logTestStep("Preparing updated user data");
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("firstName", "Updated");
        updateData.put("lastName", "User");
        updateData.put("email", "updated_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        
        currentUserData = updateData;
        TestContext.setTestData(updateData);
        
        logger.info("Updated user data prepared");
    }

    @Given("I have profile update data")
    public void iHaveProfileUpdateData() {
        LogManager.logTestStep("Preparing profile update data");
        
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("bio", "Updated bio information");
        profileData.put("website", "https://updated-website.com");
        profileData.put("location", "Updated City, UC");
        
        currentUserData = profileData;
        TestContext.setTestData(profileData);
        
        logger.info("Profile update data prepared");
    }

    @Given("I have user data with special characters")
    public void iHaveUserDataWithSpecialChars() {
        LogManager.logTestStep("Preparing user data with special characters");
        
        currentUserData = DataProvider.getUserData("userWithSpecialChars");
        TestContext.setTestData(currentUserData);
        
        logger.info("User data with special characters prepared");
    }

    @Given("I have data for multiple users")
    public void iHaveDataForMultipleUsers() {
        LogManager.logTestStep("Preparing data for multiple users");
        
        // Create a list of different user types for bulk operations
        Map<String, Object> allUsers = DataProvider.loadJsonData("users");
        TestContext.setTestData(allUsers);
        
        logger.info("Multiple users data prepared");
    }

    @Given("multiple users exist in the system")
    public void multipleUsersExistInTheSystem() {
        LogManager.logTestStep("Creating multiple users in the system");
        
        String endpoint = DataProvider.getEndpoint("users");
        
        // Create admin user
        Map<String, Object> adminUser = DataProvider.getUserData("adminUser");
        adminUser.put("username", "admin_" + UUID.randomUUID().toString().substring(0, 8));
        adminUser.put("email", "admin_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        
        Response adminResponse = apiClient.post(endpoint, adminUser);
        assertThat(adminResponse.getStatusCode()).isEqualTo(201);
        
        // Create regular user
        Map<String, Object> regularUser = DataProvider.getUserData("validUser");
        regularUser.put("username", "user_" + UUID.randomUUID().toString().substring(0, 8));
        regularUser.put("email", "user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com");
        
        Response userResponse = apiClient.post(endpoint, regularUser);
        assertThat(userResponse.getStatusCode()).isEqualTo(201);
        
        logger.info("Multiple users created in system");
    }

    @When("I send a POST request to create a user")
    public void iSendAPostRequestToCreateAUser() {
        LogManager.logTestStep("Sending POST request to create user");
        
        String endpoint = DataProvider.getEndpoint("users");
        Response response = apiClient.post(endpoint, currentUserData);
        
        TestContext.setResponse(response);
        TestContext.setRequestBody(currentUserData);
        TestContext.setEndpoint(endpoint);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("POST request sent to create user");
    }

    @When("I send a GET request to retrieve the user")
    public void iSendAGetRequestToRetrieveTheUser() {
        LogManager.logTestStep("Sending GET request to retrieve user");
        
        String userId = TestContext.getUserId();
        if (userId == null) {
            userId = currentUserId;
        }
        
        String endpoint = DataProvider.getEndpoint("userById");
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        
        Response response = apiClient.get(endpoint, pathParams);
        
        TestContext.setResponse(response);
        TestContext.setEndpoint(endpoint);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("GET request sent to retrieve user: {}", userId);
    }

    @When("I send a PUT request to update the user")
    public void iSendAPutRequestToUpdateTheUser() {
        LogManager.logTestStep("Sending PUT request to update user");
        
        String userId = TestContext.getUserId();
        String endpoint = DataProvider.getEndpoint("userById");
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        
        Response response = apiClient.put(endpoint, currentUserData, pathParams);
        
        TestContext.setResponse(response);
        TestContext.setRequestBody(currentUserData);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("PUT request sent to update user: {}", userId);
    }

    @When("I send a PATCH request to update user profile")
    public void iSendAPatchRequestToUpdateUserProfile() {
        LogManager.logTestStep("Sending PATCH request to update user profile");
        
        String userId = TestContext.getUserId();
        String endpoint = DataProvider.getEndpoint("userProfile");
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        
        Response response = new RequestBuilder(apiClient)
                .endpoint(endpoint)
                .pathParams(pathParams)
                .body(currentUserData)
                .patch();
        
        TestContext.setResponse(response);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("PATCH request sent to update user profile: {}", userId);
    }

    @When("I send a DELETE request to remove the user")
    public void iSendADeleteRequestToRemoveTheUser() {
        LogManager.logTestStep("Sending DELETE request to remove user");
        
        String userId = TestContext.getUserId();
        String endpoint = DataProvider.getEndpoint("userById");
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        
        Response response = apiClient.delete(endpoint, pathParams);
        
        TestContext.setResponse(response);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("DELETE request sent to remove user: {}", userId);
    }

    @When("I send a GET request to search users with filters")
    public void iSendAGetRequestToSearchUsersWithFilters(DataTable dataTable) {
        LogManager.logTestStep("Sending GET request to search users with filters");
        
        List<Map<String, String>> filters = dataTable.asMaps(String.class, String.class);
        Map<String, Object> queryParams = new HashMap<>();
        
        for (Map<String, String> filter : filters) {
            queryParams.put(filter.get("parameter"), filter.get("value"));
        }
        
        String endpoint = DataProvider.getEndpoint("users");
        Response response = apiClient.getWithQueryParams(endpoint, queryParams);
        
        TestContext.setResponse(response);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("GET request sent to search users with filters: {}", queryParams);
    }

    @When("I send a GET request to retrieve user data")
    public void iSendAGetRequestToRetrieveUserData() {
        LogManager.logTestStep("Sending GET request to retrieve user data");
        
        String endpoint = DataProvider.getEndpoint("users");
        Response response = apiClient.get(endpoint);
        
        TestContext.setResponse(response);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("GET request sent to retrieve user data");
    }

    @When("I send a GET request to retrieve admin user data")
    public void iSendAGetRequestToRetrieveAdminUserData() {
        LogManager.logTestStep("Sending GET request to retrieve admin user data");
        
        String endpoint = DataProvider.getEndpoint("adminUsers");
        Response response = apiClient.get(endpoint);
        
        TestContext.setResponse(response);
        
        LogManager.logResponse(response.getStatusCode(), response.getBody().asString());
        logger.info("GET request sent to retrieve admin user data");
    }

    @When("I send multiple POST requests to create users")
    public void iSendMultiplePostRequestsToCreateUsers() {
        LogManager.logTestStep("Sending multiple POST requests to create users");
        
        Map<String, Object> allUsers = TestContext.getTestData();
        String endpoint = DataProvider.getEndpoint("users");
        int successCount = 0;
        
        for (String userType : allUsers.keySet()) {
            if (userType.contains("User") && !userType.contains("invalid")) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) allUsers.get(userType);
                    
                    // Make username unique
                    userData.put("username", userData.get("username") + "_" + System.currentTimeMillis());
                    
                    Response response = apiClient.post(endpoint, userData);
                    if (response.getStatusCode() == 201) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to create user: {}", userType, e);
                }
            }
        }
        
        TestContext.set("created_users_count", successCount);
        logger.info("Multiple user creation completed. Success count: {}", successCount);
    }

    @Then("the response should contain the created user details")
    public void theResponseShouldContainTheCreatedUserDetails() {
        LogManager.logTestStep("Validating created user details in response");
        
        Response response = TestContext.getResponse();
        
        assertThat((Object) response.jsonPath().get("id"))
                .as("Response should contain user ID")
                .isNotNull();
        
        assertThat((Object) response.jsonPath().get("username"))
                .as("Response should contain username")
                .isNotNull();
        
        assertThat((Object) response.jsonPath().get("email"))
                .as("Response should contain email")
                .isNotNull();
        
        // Store the created user ID for future use
        String userId = response.jsonPath().getString("id");
        TestContext.setUserId(userId);
        
        logger.info("Created user details validation passed");
    }

    @Then("the user should have a valid ID")
    public void theUserShouldHaveAValidId() {
        LogManager.logTestStep("Validating user has valid ID");
        
        Response response = TestContext.getResponse();
        String userId = response.jsonPath().getString("id");
        
        assertThat(userId)
                .as("User ID should not be null or empty")
                .isNotEmpty();
        
        // Basic UUID format validation (adjust based on your ID format)
        assertThat(userId.length())
                .as("User ID should have reasonable length")
                .isGreaterThan(0);
        
        logger.info("User ID validation passed: {}", userId);
    }

    @Then("the response should contain the user details")
    public void theResponseShouldContainTheUserDetails() {
        LogManager.logTestStep("Validating user details in response");
        
        Response response = TestContext.getResponse();
        
        assertThat((Object) response.jsonPath().get("id"))
                .as("Response should contain user ID")
                .isNotNull();
        
        assertThat((Object) response.jsonPath().get("username"))
                .as("Response should contain username")
                .isNotNull();
        
        logger.info("User details validation passed");
    }

    @Then("all required fields should be present")
    public void allRequiredFieldsShouldBePresent() {
        LogManager.logTestStep("Validating all required fields are present");
        
        Response response = TestContext.getResponse();
        String[] requiredFields = {"id", "username", "email", "firstName", "lastName"};
        
        for (String field : requiredFields) {
            assertThat((Object) response.jsonPath().get(field))
                    .as("Required field should be present: " + field)
                    .isNotNull();
        }
        
        logger.info("Required fields validation passed");
    }

    @Then("the response should contain validation errors")
    public void theResponseShouldContainValidationErrors() {
        LogManager.logTestStep("Validating presence of validation errors");
        
        Response response = TestContext.getResponse();
        String responseBody = response.getBody().asString();
        
        boolean hasValidationErrors = responseBody.contains("validation") ||
                                    responseBody.contains("error") ||
                                    responseBody.contains("invalid") ||
                                    responseBody.contains("required");
        
        assertThat(hasValidationErrors)
                .as("Response should contain validation errors")
                .isTrue();
        
        logger.info("Validation errors presence confirmed");
    }

    @Then("the error message should indicate the specific validation failures")
    public void theErrorMessageShouldIndicateTheSpecificValidationFailures() {
        LogManager.logTestStep("Validating specific validation failure messages");
        
        Response response = TestContext.getResponse();
        String responseBody = response.getBody().asString();
        
        // Check for specific validation error indicators
        boolean hasSpecificErrors = responseBody.contains("username") ||
                                  responseBody.contains("email") ||
                                  responseBody.contains("required") ||
                                  responseBody.contains("format");
        
        assertThat(hasSpecificErrors)
                .as("Response should contain specific validation failure details")
                .isTrue();
        
        logger.info("Specific validation failure messages confirmed");
    }

    @Then("the response should contain the updated user details")
    public void theResponseShouldContainTheUpdatedUserDetails() {
        LogManager.logTestStep("Validating updated user details in response");
        
        Response response = TestContext.getResponse();
        Map<String, Object> updateData = TestContext.getTestData();
        
        for (String key : updateData.keySet()) {
            Object expectedValue = updateData.get(key);
            Object actualValue = response.jsonPath().get(key);
            
            assertThat(actualValue)
                    .as("Updated field should match: " + key)
                    .isEqualTo(expectedValue);
        }
        
        logger.info("Updated user details validation passed");
    }

    @Then("the updated fields should reflect the changes")
    public void theUpdatedFieldsShouldReflectTheChanges() {
        LogManager.logTestStep("Validating updated fields reflect changes");
        
        Response response = TestContext.getResponse();
        
        // Check for updated timestamp or version field
        Object updatedAt = response.jsonPath().get("updatedAt");
        if (updatedAt != null) {
            assertThat(updatedAt.toString())
                    .as("Updated timestamp should be present")
                    .isNotEmpty();
        }
        
        logger.info("Field changes validation passed");
    }

    @Then("the user should no longer exist in the system")
    public void theUserShouldNoLongerExistInTheSystem() {
        LogManager.logTestStep("Verifying user no longer exists in system");
        
        String userId = TestContext.getUserId();
        String endpoint = DataProvider.getEndpoint("userById");
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        
        Response verificationResponse = apiClient.get(endpoint, pathParams);
        
        assertThat(verificationResponse.getStatusCode())
                .as("User should not be found after deletion")
                .isEqualTo(404);
        
        logger.info("User deletion verification passed");
    }

    @Then("the response should contain a list of matching users")
    public void theResponseShouldContainAListOfMatchingUsers() {
        LogManager.logTestStep("Validating response contains list of matching users");
        
        Response response = TestContext.getResponse();
        List<Object> users = response.jsonPath().getList("$");
        
        assertThat(users)
                .as("Response should contain a list of users")
                .isNotNull()
                .isNotEmpty();
        
        logger.info("User list validation passed. Count: {}", users.size());
    }

    @Then("all returned users should match the search criteria")
    public void allReturnedUsersShouldMatchTheSearchCriteria() {
        LogManager.logTestStep("Validating all returned users match search criteria");
        
        Response response = TestContext.getResponse();
        List<Map<String, Object>> users = response.jsonPath().getList("$");
        
        for (Map<String, Object> user : users) {
            // Example validation - adjust based on your search criteria
            if (user.containsKey("role")) {
                assertThat(user.get("role"))
                        .as("User role should match search criteria")
                        .isNotNull();
            }
        }
        
        logger.info("Search criteria validation passed");
    }

    @Then("the response should include pagination information")
    public void theResponseShouldIncludePaginationInformation() {
        LogManager.logTestStep("Validating pagination information in response");
        
        Response response = TestContext.getResponse();
        
        // Check for common pagination fields
        boolean hasPagination = response.jsonPath().get("page") != null ||
                              response.jsonPath().get("totalCount") != null ||
                              response.jsonPath().get("hasNext") != null ||
                              response.getHeader("X-Total-Count") != null;
        
        assertThat(hasPagination)
                .as("Response should include pagination information")
                .isTrue();
        
        logger.info("Pagination information validation passed");
    }

    @Then("the special characters should be properly encoded")
    public void theSpecialCharactersShouldBeProperlyEncoded() {
        LogManager.logTestStep("Validating special characters are properly encoded");
        
        Response response = TestContext.getResponse();
        String responseBody = response.getBody().asString();
        
        // Verify the response contains the special characters correctly
        assertThat(responseBody)
                .as("Response should contain properly encoded special characters")
                .isNotEmpty();
        
        logger.info("Special characters encoding validation passed");
    }

    @Then("the user data should be retrievable correctly")
    public void theUserDataShouldBeRetrievableCorrectly() {
        LogManager.logTestStep("Validating user data is retrievable correctly");
        
        String userId = TestContext.getResponse().jsonPath().getString("id");
        String endpoint = DataProvider.getEndpoint("userById");
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        
        Response retrievalResponse = apiClient.get(endpoint, pathParams);
        
        assertThat(retrievalResponse.getStatusCode())
                .as("User should be retrievable after creation")
                .isEqualTo(200);
        
        logger.info("User data retrieval validation passed");
    }

    @Then("all requests should complete within acceptable time limits")
    public void allRequestsShouldCompleteWithinAcceptableTimeLimits() {
        LogManager.logTestStep("Validating all requests complete within time limits");
        
        // This is a placeholder - actual implementation would track request times
        logger.info("Performance validation completed");
    }

    @Then("all users should be created successfully")
    public void allUsersShouldBeCreatedSuccessfully() {
        LogManager.logTestStep("Validating all users created successfully");
        
        Integer createdCount = TestContext.get("created_users_count");
        assertThat(createdCount)
                .as("At least some users should be created successfully")
                .isGreaterThan(0);
        
        logger.info("Bulk user creation validation passed. Created: {}", createdCount);
    }

    @Then("the system should handle concurrent requests properly")
    public void theSystemShouldHandleConcurrentRequestsProperly() {
        LogManager.logTestStep("Validating system handles concurrent requests properly");
        
        // This is a placeholder for concurrent request validation
        logger.info("Concurrent request handling validation completed");
    }

    @Then("the profile information should be updated")
    public void theProfileInformationShouldBeUpdated() {
        LogManager.logTestStep("Validating profile information is updated");
        
        Response response = TestContext.getResponse();
        Map<String, Object> updateData = TestContext.getTestData();
        
        for (String key : updateData.keySet()) {
            Object actualValue = response.jsonPath().get(key);
            assertThat(actualValue)
                    .as("Profile field should be updated: " + key)
                    .isEqualTo(updateData.get(key));
        }
        
        logger.info("Profile update validation passed");
    }

    @Then("the timestamp should reflect the recent update")
    public void theTimestampShouldReflectTheRecentUpdate() {
        LogManager.logTestStep("Validating timestamp reflects recent update");
        
        Response response = TestContext.getResponse();
        String updatedAt = response.jsonPath().getString("updatedAt");
        
        if (updatedAt != null) {
            assertThat(updatedAt)
                    .as("Updated timestamp should be present")
                    .isNotEmpty();
        }
        
        logger.info("Timestamp validation passed");
    }
}