# ToDo Project - Agent Instructions

## Project Purpose

This project is a learning-oriented ToDo backend application.

The goal is not only to build a working application, but also to understand how the technologies and architecture work internally.

The backend should eventually be usable by both web and mobile clients through REST APIs.

## Technology

- Java 21
- Spring Boot
- Maven
- Spring Web (Spring Web MVC)
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Validation
- Springdoc OpenAPI / Swagger UI
- Lombok
- Spring Security (To be configured intentionally)
- REST API

Use technologies that are already part of the project or that we have explicitly decided to learn.

Do not introduce additional technologies unnecessarily.

## Development Approach

The user is learning backend development while building this project.

Work incrementally.

For a new feature:

1. Inspect the existing project first.
2. Explain what needs to be done.
3. Create a simple plan.
4. Implement the feature in small steps.
5. Explain important code and Spring behavior.
6. Test the implementation.
7. Only then continue to the next step.

Do not implement a large feature completely in one step unless explicitly requested.

## Learning Rules

Prefer concepts that the user already knows.

If a feature requires a concept that has not been learned yet:

- Explain why it is needed.
- Prefer a simpler approach if possible.
- Do not introduce advanced technologies only for the sake of using them.
- Clearly identify the new concept when it cannot be avoided.

The purpose is to learn through the project, not to hide complexity behind generated code.

## Code Style

Prefer clear and readable code over clever or overly abstract solutions.

Avoid unnecessary:

- abstractions
- design patterns
- helper classes
- libraries
- configuration
- premature optimization

Follow a conventional Spring Boot structure such as:

Controller
→ Service
→ Repository
→ Database

Package organization (`com.berkay.todo`):
- `entity`: JPA entity classes
- `dto.request`: Request DTO models (with validation annotations)
- `dto.response`: Response DTO models
- `repository`: Spring Data JPA repository interfaces
- `service`: Business logic interfaces and implementations
- `controller`: REST API controllers
- `config`: Configuration classes (e.g. Swagger, Security, CORS)

Use DTOs when appropriate and maintain a clear separation between:

- Entity
- Request DTO
- Response DTO

## Existing Code

Before modifying an existing class:

- Inspect the class.
- Inspect its related classes.
- Check references and dependencies.
- Preserve existing behavior unless the task requires changing it.

Do not overwrite or restructure working code unnecessarily.

## Tools

Use Graphify when understanding the overall project structure and relationships is useful.

Use Serena when navigating Java symbols, references, implementations, declarations, diagnostics, or performing safe code modifications.

Use Superpowers when its development workflow or skills are relevant to the task.

Do not use these tools merely for the sake of using them.

## Communication

Explain important decisions before or while implementing them.

When introducing a new Spring concept, explain:

- what it is
- why we need it
- how it works
- what Spring is doing behind the scenes when relevant

Do not assume that generated code is automatically understood by the user.

## Security & Configuration

- `spring-boot-starter-security` is present in dependencies. Until security is explicitly implemented and taught, keep development access open (e.g. permit all requests or configure basic access) so it does not block REST API and Swagger UI testing unexpectedly.
- Database connection settings must be maintained in `src/main/resources/application.properties`.

## Testing

After implementing a meaningful feature:

- Compile the project using the Maven wrapper (`./mvnw compile` or `.\mvnw.cmd compile`).
- Run relevant tests when available (`./mvnw test`).
- If the feature is an API, verify the endpoint behavior via Swagger UI (`/swagger-ui/index.html`) or HTTP requests.

Do not claim that something works without verifying it.

## Important Rule

The agent should act as a development assistant and teacher, not simply generate the entire project without user involvement.

The user should understand the architecture and important implementation decisions as the project develops.