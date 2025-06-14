# API Test Automation Framework

A comprehensive BDD/TDD API testing framework built with Maven, Java, and Cucumber, designed for REST API testing with JSON responses, authentication support, parallel execution, and dual reporting capabilities.

## 🏗️ Framework Architecture

- **BDD Support**: Cucumber with Gherkin features for business-readable scenarios
- **TDD Support**: Unit and integration testing capabilities
- **Authentication**: Bearer Token and Basic Auth support
- **Parallel Execution**: Thread-safe concurrent test execution
- **Data-Driven Testing**: JSON/YAML test data management
- **Dual Reporting**: Extent Reports + Cucumber HTML Reports
- **Environment Management**: Multi-environment configuration support

## 🛠️ Technology Stack

- **Java 11+**: Programming language
- **Maven**: Build and dependency management
- **Cucumber 7.x**: BDD framework
- **RestAssured 5.x**: API testing library
- **TestNG**: Test execution framework
- **Extent Reports 5.x**: Rich HTML reporting
- **Jackson**: JSON/YAML processing
- **SLF4J + Logback**: Logging framework

## 📁 Project Structure

```
api-test-framework/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/framework/
│   │   ├── auth/                    # Authentication management
│   │   ├── config/                  # Configuration management
│   │   ├── core/                    # Core API client and utilities
│   │   ├── reporting/               # Extent Reports management
│   │   └── utils/                   # Utility classes
│   └── test/
│       ├── java/
│       │   ├── hooks/               # Cucumber hooks
│       │   ├── runners/             # Test runners
│       │   └── stepDefinitions/     # Step definitions
│       └── resources/
│           ├── config/              # Environment configurations
│           ├── features/            # Cucumber feature files
│           └── testData/            # Test data files
└── target/
    └── reports/                     # Generated reports
```

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.8 or higher
- IDE with Cucumber plugin support (IntelliJ IDEA/Eclipse)

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd api-test-framework
```

2. Install dependencies:
```bash
mvn clean install
```

3. Verify setup:
```bash
mvn test -Dtest=TestRunner
```

## 🎯 Running Tests

### Basic Commands

```bash
# Run all tests
mvn clean test

# Run with specific environment
mvn clean test -Denvironment=staging

# Run specific tags
mvn clean test -Dcucumber.filter.tags="@smoke"

# Run with parallel execution
mvn clean test -P parallel

# Run regression tests
mvn clean test -P regression
```

### Environment Profiles

```bash
# Development environment
mvn clean test -P dev

# Staging environment
mvn clean test -P staging

# Production environment
mvn clean test -P prod
```

### Tag-based Execution

```bash
# Smoke tests
mvn clean test -P smoke

# Regression tests
mvn clean test -P regression

# User management tests
mvn clean test -Dcucumber.filter.tags="@user-management"

# API validation tests
mvn clean test -Dcucumber.filter.tags="@validation"
```

## 📊 Reports

The framework generates multiple types of reports:

### Extent Reports
- Location: `target/extent-reports/`
- Rich HTML reports with detailed test execution information
- Screenshots and attachments for failed tests
- Environment and configuration details

### Cucumber Reports
- Location: `target/cucumber-reports/`
- Native BDD reports with scenario details
- JSON/XML reports for CI/CD integration

### Logs
- Location: `target/logs/`
- Detailed execution logs with different log levels per environment

## 🔧 Configuration

### Environment Configuration

Edit environment-specific properties in `src/test/resources/config/`:

- `dev.properties`: Development environment settings
- `staging.properties`: Staging environment settings
- `prod.properties`: Production environment settings

### Test Data Management

Test data is managed in `src/test/resources/testData/`:

- `users.json`: User test data
- `endpoints.yaml`: API endpoint definitions
- Custom data files as needed

### Authentication Configuration

Configure authentication in environment properties:

```properties
# Basic Authentication
auth.type=basic
auth.username=testuser
auth.password=testpass

# Bearer Token Authentication
auth.type=bearer
auth.token.endpoint=/api/auth/token
```

## 🧪 Writing Tests

### Feature Files

Create feature files in `src/test/resources/features/`:

```gherkin
@regression
Feature: User Management API
  
  @smoke
  Scenario: Create a new user
    Given I have valid user data
    When I send a POST request to create a user
    Then the response status code should be 201
    And the response should contain the created user details
```

### Step Definitions

Implement step definitions in `src/test/java/stepDefinitions/`:

```java
@Given("I have valid user data")
public void iHaveValidUserData() {
    currentUserData = DataProvider.getUserData("validUser");
    TestContext.setTestData(currentUserData);
}
```

### Test Data

Define test data in JSON/YAML format:

```json
{
  "validUser": {
    "username": "testuser",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }
}
```

## 🔀 Parallel Execution

The framework supports parallel execution at the scenario level:

```bash
# Enable parallel execution
mvn clean test -P parallel

# Configure thread count in pom.xml
<threadCount>10</threadCount>
```

## 🛡️ Authentication

### Basic Authentication

```java
apiClient.authenticate("username", "password");
```

### Bearer Token Authentication

```java
// Using username/password to obtain token
apiClient.authenticate("username", "password");

// Using pre-obtained token
apiClient.authenticateWithToken("your-token-here");
```

## 🔍 API Testing Features

### Request Building

```java
Response response = new RequestBuilder(apiClient)
    .endpoint("/api/users")
    .body(userData)
    .header("Content-Type", "application/json")
    .post();
```

### Response Validation

```java
new ResponseValidator(response)
    .statusCode(201)
    .responseTime(2000)
    .jsonPathEquals("username", expectedUsername)
    .jsonSchema("schemas/userSchema.json");
```

### Test Context Management

```java
// Store data for use across steps
TestContext.setUserId("12345");
TestContext.setResponse(response);

// Retrieve data in other steps
String userId = TestContext.getUserId();
Response lastResponse = TestContext.getResponse();
```

## 🐛 Debugging

### Enable Debug Logging

```bash
mvn clean test -Dlog.level=DEBUG
```

### View Detailed Reports

1. Open `target/extent-reports/API-Test-Report-[timestamp].html`
2. Check `target/logs/[environment].log` for detailed execution logs
3. Review `target/cucumber-reports/` for BDD-specific reports

## 🤝 Contributing

1. Follow existing code conventions
2. Write comprehensive tests for new features
3. Update documentation for any API changes
4. Ensure all tests pass before submitting changes

## 📝 Best Practices

### Test Organization

- Group related scenarios in feature files
- Use meaningful scenario and step names
- Implement reusable step definitions
- Maintain clean test data

### Data Management

- Use external data files for test data
- Implement data providers for complex scenarios
- Clean up test data after execution
- Use unique identifiers to avoid conflicts

### Error Handling

- Implement comprehensive error scenarios
- Use appropriate assertion messages
- Log detailed failure information
- Provide clear debugging information

### Performance

- Set appropriate timeouts
- Monitor response times
- Use parallel execution for large test suites
- Optimize test data creation

## 🎨 Customization

### Adding New Endpoints

1. Update `endpoints.yaml` with new endpoint definitions
2. Create corresponding step definitions
3. Add test scenarios in feature files

### Custom Assertions

Extend `ResponseValidator` class:

```java
public ResponseValidator customValidation(CustomValidator validator) {
    validator.validate(response);
    return this;
}
```

### Additional Reporting

Extend `ExtentReportManager` for custom reporting needs:

```java
public static void logCustomMetric(String metricName, Object value) {
    // Custom implementation
}
```

## 🚨 Troubleshooting

### Common Issues

1. **Authentication Failures**: Check credentials and endpoint configuration
2. **Connection Timeouts**: Verify base URL and network connectivity
3. **JSON Parsing Errors**: Validate response format and schema
4. **Parallel Execution Issues**: Ensure thread-safe data management

### Support

For issues and questions:
1. Check existing documentation
2. Review logs and reports
3. Verify configuration settings
4. Test with minimal scenarios

---

**Framework Version**: 1.0.0  
**Last Updated**: December 2024  
**Maintained By**: Test Automation Team