# CLAUDE.md - GenericAPI-REST

This file provides guidance to Claude Code when working with this Spring Boot REST API project.

## Project Overview

**GenericAPI-REST** is a Spring Boot 3.5.6 REST API with JWT authentication, Google OAuth integration, and SQLite database.

## Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.6
- **Build Tool**: Maven
- **Database**: SQLite (local development)
- **ORM**: Spring Data JPA with Hibernate
- **Database Migrations**: Liquibase
- **Authentication**: JWT (JSON Web Tokens) + Google OAuth
- **Security**: Spring Security
- **Validation**: Jakarta Validation (Bean Validation)

## Project Structure

```
src/main/java/com/generic/rest/main/
├── config/          # Configuration classes (Security, JWT, CORS)
├── controller/      # REST controllers (endpoints)
├── service/         # Business logic layer
├── repository/      # JPA repositories (data access)
├── model/           # JPA entities
├── dto/             # Data Transfer Objects
├── security/        # Security components (filters, providers)
└── util/            # Utility classes

src/main/resources/
├── db/
│   ├── changelog/   # Liquibase migration files
│   └── local.db     # SQLite database file
└── application.properties
```

## Development Commands

### Build the Project
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
# or
mvn clean test
```

### Run the Application
```bash
mvn spring-boot:run
# or with timeout (stops after 10 seconds)
timeout 10 mvn spring-boot:run
```

### Package the Application
```bash
mvn clean package
```

### Run the JAR
```bash
java -jar target/main-0.0.1-SNAPSHOT.jar
```

## Database & Migrations

- **Database**: SQLite stored at `src/main/resources/db/local.db`
- **Migrations**: Managed by Liquibase
- **Changelog**: `src/main/resources/db/changelog/db.changelog-master.yaml`

### Important Notes:
- **Never modify `spring.jpa.hibernate.ddl-auto`** - It must remain `none`
- All schema changes MUST be done via Liquibase migrations
- SQLite uses single connection pool (`maximum-pool-size=1`)

### Creating Database Migrations

When creating new migrations:
1. Add a new changeset to the Liquibase changelog
2. Use YAML format for consistency
3. Test migrations before committing
4. Never modify existing changesets

## Architecture & Code Conventions

### Layered Architecture

Follow this strict separation:

1. **Controller Layer** (`controller/`)
   - Handle HTTP requests/responses
   - Map DTOs to/from services
   - Validate request data (use `@Valid`)
   - Return appropriate HTTP status codes

2. **Service Layer** (`service/`)
   - Contain business logic
   - Use `@Transactional` for data operations
   - Throw meaningful exceptions
   - Work with entities and DTOs

3. **Repository Layer** (`repository/`)
   - Extend `JpaRepository<Entity, ID>`
   - Define custom queries when needed
   - Keep queries database-agnostic

4. **Model Layer** (`model/`)
   - JPA entities with proper annotations
   - Bidirectional relationships properly managed
   - Use `@Entity`, `@Table`, proper column definitions

5. **DTO Layer** (`dto/`)
   - Data Transfer Objects for API contracts
   - Separate from entities
   - Include validation annotations

### Naming Conventions

- **Entities**: Singular names (e.g., `Product`, `Collection`, `User`)
- **Tables**: Plural names (e.g., `products`, `collections`, `users`)
- **Join Tables**: `entity1_entity2` (e.g., `collection_products`)
- **DTOs**: `EntityDTO` (e.g., `ProductDTO`, `CollectionDTO`)
- **Services**: `EntityService` (e.g., `ProductService`, `CollectionService`)
- **Controllers**: `EntityController` (e.g., `ProductController`)
- **Repositories**: `EntityRepository` (e.g., `ProductRepository`)

### Transaction Management

- Always use `@Transactional` for methods that modify data
- Use `@Transactional(readOnly = true)` for read-only operations
- Handle entity relationships carefully to avoid lazy loading issues

### Relationship Management

When working with bidirectional relationships:
- Always update both sides of the relationship
- Use helper methods in entities (e.g., `addProduct()`, `removeProduct()`)
- Be careful with collections to avoid `ConcurrentModificationException`
- Create defensive copies when iterating and modifying: `new ArrayList<>(collection)`

### Business Rules for Collections

**Important**: Collections follow a hierarchical structure:
- **Maximum depth**: 2 levels (root collection → subcollection)
- **Product placement**: Products can only exist in:
  - Root collections WITHOUT subcollections
  - Subcollections (leaf nodes)
- **When adding subcollections**: All products from parent collection are automatically removed
- **Validation**: Prevent products from being in both parent and subcollection

## Authentication & Security

### JWT Authentication
- **Secret**: Configured in `application.properties` (`app.security.jwt.secret`)
- **Expiration**: 60 minutes (configurable via `app.security.jwt.expiration-minutes`)
- **Format**: Bearer token in `Authorization` header

### Google OAuth
- Client ID and Secret configured in `application.properties`
- Use for third-party authentication

### CORS Configuration
- Allowed origins: `*` (development only)
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Allowed headers: `*`

**Security Note**: Update CORS and OAuth credentials for production deployment

## Testing

- Use Spring Boot Test framework
- Include `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Use Spring Security Test utilities for auth testing
- Everytime there is a new implementation of a controller, make a new test class for it
- The coverage of the project must be at least 80%
- The testing is a testing end-to-end testing the request-response flow 

## Error Handling

- Services throw `RuntimeException` with descriptive messages
- Consider implementing custom exception classes and `@ControllerAdvice`
- Always validate input data

## Best Practices

1. **Always read files before editing** - Use Read tool before Edit/Write
2. **Follow transaction boundaries** - Don't mix data access and business logic
3. **Validate early** - Check preconditions before processing
4. **Use DTOs** - Never expose entities directly in API responses
5. **Handle relationships carefully** - Update both sides of bidirectional relationships
6. **Write defensive code** - Check for null, validate sizes, etc.
7. **Use meaningful names** - Code should be self-documenting
8. **Keep services focused** - Single responsibility per service method
9. **Test business logic** - Write tests for critical functionality
10. **Document complex logic** - Add comments for non-obvious code

## Common Patterns

### Creating Entities with Relationships

```java
// Service method
@Transactional
public EntityDTO createEntity(EntityDTO request) {
    Entity entity = new Entity(request.getName());

    if (request.getRelatedId() != null) {
        RelatedEntity related = relatedRepository.findById(request.getRelatedId())
            .orElseThrow(() -> new RuntimeException("Related entity not found"));

        entity.setRelated(related);
    }

    Entity saved = entityRepository.save(entity);
    return mapToDTO(saved);
}
```

### Updating Collections Safely

```java
// Create defensive copy before modifying
List<Item> items = new ArrayList<>(collection.getItems());
for (Item item : items) {
    collection.removeItem(item);
}
```

### Mapping Entities to DTOs

```java
private EntityDTO mapToDTO(Entity entity) {
    return new EntityDTO(
        entity.getId(),
        entity.getName(),
        entity.getRelatedIds(),
        // ... other fields
    );
}
```

## Important Files

- `application.properties` - Main configuration
- `pom.xml` - Maven dependencies
- `db.changelog-master.yaml` - Database migrations
- `SecurityConfig.java` - Security configuration
- `JwtUtil.java` / `JwtFilter.java` - JWT handling

## Development Workflow

1. **Start work**: Understand the existing code structure
2. **Plan changes**: Consider impact on existing functionality
3. **Create migrations**: If database changes needed
4. **Implement logic**: Follow layered architecture
5. **Test**: Verify with unit/integration tests
6. **Build**: Run `mvn clean compile` to check for errors
7. **Run**: Test with `mvn spring-boot:run`

## Troubleshooting

### SQLite Connection Issues
- Check `local.db` file exists
- Verify single connection pool setting
- Check file permissions

### Liquibase Errors
- Review changelog syntax
- Check database state vs. expected changesets
- Use `logging.level.liquibase=DEBUG`

### Lazy Loading Issues
- Ensure `@Transactional` is used properly
- Fetch required relationships in queries
- Use DTOs to avoid lazy loading in controllers

---

**Last Updated**: 2025-10-30
