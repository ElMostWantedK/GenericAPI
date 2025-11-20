# GenericAPI-REST

A modern REST API built with Spring Boot 3.5.6 featuring JWT authentication, Google OAuth integration, and SQLite database.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Security](#security)
- [Contributing](#contributing)

## Features

- **JWT Authentication**: Secure token-based authentication system
- **Google OAuth**: Third-party authentication integration
- **RESTful API**: Clean and intuitive REST endpoints
- **Database Migrations**: Liquibase for version-controlled schema changes
- **Spring Security**: Comprehensive security configuration
- **Input Validation**: Jakarta Bean Validation for data integrity
- **Layered Architecture**: Clear separation of concerns (Controller, Service, Repository)
- **SQLite Database**: Lightweight database for local development

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.6
- **Build Tool**: Maven
- **Database**: SQLite
- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: Liquibase
- **Authentication**: JWT (jjwt 0.11.5) + Google OAuth
- **Security**: Spring Security
- **Validation**: Jakarta Validation API
- **Testing**: Spring Boot Test, Spring Security Test

## Prerequisites

- **Java Development Kit (JDK)**: 21 or higher
- **Maven**: 3.6+ (or use the included Maven wrapper)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (optional)

## Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd GenericAPI-REST
   ```

2. **Build the project**:
   ```bash
   mvn clean compile
   ```

3. **Run tests** (optional):
   ```bash
   mvn test
   ```

4. **Package the application**:
   ```bash
   mvn clean package
   ```

## Configuration

The main configuration file is located at `src/main/resources/application.properties`.

### Key Configuration Properties

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:sqlite:src/main/resources/db/local.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=none

# JWT Configuration
app.security.jwt.secret=your-secret-key-here
app.security.jwt.expiration-minutes=60

# Google OAuth Configuration
app.security.oauth.google.client-id=your-google-client-id
app.security.oauth.google.client-secret=your-google-client-secret

# Liquibase Configuration
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

### Environment-Specific Configuration

**Important**: Before deploying to production:
1. Update JWT secret to a strong random value
2. Configure Google OAuth credentials
3. Update CORS settings to restrict allowed origins
4. Consider using PostgreSQL or MySQL instead of SQLite

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using the JAR

```bash
java -jar target/main-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080` by default.

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

#### Google OAuth Login
```http
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "google-id-token-here"
}
```

### Protected Endpoints

Include the JWT token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

### Postman Collection

Import the `GenericAPI-REST.postman_collection.json` file into Postman for a complete collection of API endpoints.

## Project Structure

```
src/main/java/com/generic/rest/main/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── JwtConfig.java
├── controller/          # REST controllers
│   ├── AuthController.java
│   └── ...
├── service/             # Business logic
│   ├── AuthService.java
│   └── ...
├── repository/          # Data access layer
│   ├── UserRepository.java
│   └── ...
├── model/               # JPA entities
│   ├── User.java
│   └── ...
├── dto/                 # Data Transfer Objects
│   ├── LoginRequestDTO.java
│   └── ...
├── security/            # Security components
│   ├── JwtFilter.java
│   ├── JwtUtil.java
│   └── ...
└── util/                # Utility classes

src/main/resources/
├── db/
│   ├── changelog/       # Liquibase migrations
│   │   └── db.changelog-master.yaml
│   └── local.db         # SQLite database
└── application.properties
```

## Database Migrations

This project uses **Liquibase** for database version control.

### Creating a New Migration

1. Open `src/main/resources/db/changelog/db.changelog-master.yaml`
2. Add a new changeset:

```yaml
- changeSet:
    id: your-changeset-id
    author: your-name
    changes:
      - createTable:
          tableName: your_table
          columns:
            - column:
                name: id
                type: BIGINT
                autoIncrement: true
                constraints:
                  primaryKey: true
                  nullable: false
```

3. Run the application to apply migrations automatically

### Important Rules

- **NEVER** modify `spring.jpa.hibernate.ddl-auto` - it must remain `none`
- All schema changes MUST be done via Liquibase
- Never modify existing changesets
- Use YAML format for consistency

## Testing

The project includes comprehensive tests with a target coverage of 80%.

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=AuthControllerTest
```

### Test Structure

- **Integration Tests**: End-to-end testing of request-response flow
- **Controller Tests**: Using `@WebMvcTest` for isolated controller testing
- **Security Tests**: Using Spring Security Test utilities

## Security

### JWT Authentication

- **Token Expiration**: 60 minutes (configurable)
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Format**: Bearer token in Authorization header

### Password Security

- Passwords are hashed using BCrypt
- Never store plain-text passwords

### CORS Configuration

Currently configured to allow all origins for development. Update in production:

```java
@Configuration
public class CorsConfig {
    // Configure specific origins for production
    allowedOrigins("https://your-domain.com")
}
```

### Google OAuth

- Validates ID tokens using Google API Client Library
- Extracts user information from verified tokens
- Creates or updates user accounts automatically

## Development Best Practices

1. **Always read files before editing**: Use proper file access patterns
2. **Follow transaction boundaries**: Use `@Transactional` appropriately
3. **Validate input early**: Check preconditions before processing
4. **Use DTOs**: Never expose entities directly in API responses
5. **Handle relationships carefully**: Update both sides of bidirectional relationships
6. **Write tests**: Maintain 80% code coverage
7. **Use meaningful names**: Code should be self-documenting
8. **Document complex logic**: Add comments for non-obvious code

## Common Commands

```bash
# Clean and build
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Run application
mvn spring-boot:run

# Run with timeout (10 seconds)
timeout 10 mvn spring-boot:run
```

## Troubleshooting

### SQLite Connection Issues

- Verify `src/main/resources/db/local.db` exists
- Check file permissions
- Ensure single connection pool setting is configured

### Liquibase Errors

- Review changelog YAML syntax
- Enable debug logging: `logging.level.liquibase=DEBUG`
- Check database state vs expected changesets

### Lazy Loading Issues

- Ensure `@Transactional` is used properly
- Fetch required relationships in queries
- Use DTOs to avoid lazy loading in controllers

## Contributing

1. Create a feature branch from `master`
2. Follow the existing code style and architecture
3. Write tests for new functionality
4. Ensure all tests pass: `mvn test`
5. Create a pull request with a clear description

## License

[Add your license information here]

## Contact

[Add contact information or links here]

---

**Last Updated**: 2025-11-20
