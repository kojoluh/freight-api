# freight Cargo Application

## Overview
The Freight API application is a Spring Boot-based API project designed to manage cargo operations. It provides APIs for handling package updates, delivery tracking, and exception handling for scenarios like package not found or already existing. The application is built using Java and Maven, ensuring scalability and maintainability.

## Setup and Instructions

### Prerequisites
- Java 21 or higher
- Maven 3.9.4 or higher
- Docker 19.03.0 or higher
- ** these are minimum requirements, higher versions are recommended for better performance and security. This was tested on "mac os x", version: "15.4.1", arch: "aarch64"

### Steps to Run Locally
1. Unzip/Extract the zipped file `fkluh-freight-cargo-api.zip` to your desired directory.
2. Open a terminal and navigate to the project directory:
   ```bash
   cd path/to/freight-cargo-api
   ```
3. Ensure the build.sh script has execution permissions:  
   ```bash
   chmod +x build.sh ```
4. Run the build.sh script to build the project and create a Docker image:  
   ```./build.sh```
   
5. Run the Docker container:  
   ```bash
   docker run -p 8080:8080 freight-cargo-api:0.0.1-SNAPSHOT

6. Access the application API Documentation:
   The API documentation is available at http://localhost:8080/swagger-ui.html.


## Architecture
The application follows a layered architecture:
- Controller Layer: Handles HTTP requests and responses.
- Service Layer: Contains business logic.
- Repository Layer: Manages data persistence.
- DTOs: Used for transferring data between layers.
- Exception Handling: Custom exceptions like PackageNotFoundException and PackageAlreadyExistsException ensure robust error handling.

### Key Components
- Spring Boot: Framework for building the application.
- Lombok: Reduces boilerplate code for DTOs and models.
- Maven: Dependency management and build tool.


## Future Improvements
- Enhance request and response DTOs for better API usability.
- Add support for asynchronous processing of package updates.
- Add comprehensive unit and integration tests.
- Increase coverage of tests to ensure reliability.
- Implement caching for frequently accessed data.
- Introduce API versioning to manage changes in the API.
- Introduce Authentication and Authorization mechanisms, with role-based access control (RBAC) to secure the API endpoints.
- Add support for internationalization (i18n).
- Improve logging and monitoring with tools like ELK stack or Prometheus.
- Add API rate limiting to prevent abuse.
- Introduce comprehensive CI/CD pipeline for automated testing and deployment.
- Implement a more robust exception handling mechanism with global exception handlers.
- Consider using a message broker (like RabbitMQ or Kafka) for handling asynchronous events.
- Introduce performance testing to identify bottlenecks and optimize performance.
- abstracted track if conditions into track strategy for better flow isolation and separation of concerns
- abstracted /filter if conditions into filter strategy for better flow
- fixed some clarity issues with the /filter
- improved on the cacheing by using additional keys
- improved exception handler to use instance of class names instead of checking classname strings
- add mapstruct to better handle mapping of entity to dto and vice versa
- add apache string utils to better handle string checks in validation
-


## Acknowledgements
Special thanks to the open-source community for providing libraries and tools that made this project possible.

---

## Demo Deployment

### Local Demo with Docker Compose

1. Build and run the app:
   ```sh
   PORT=10000 APP_JWT_SECRET=Freight-256-bit-secret-Cargo-256-bit-secret docker-compose up --build
   ```
2. Access Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

**Demo Credentials:**
- Username: `admin`
- Password: `Mine@admin-975`

### Online Demo Hosting (Render.com Example)

1. Sign up at [Render.com](https://render.com/)
2. Create a new Web Service:
   - Connect your GitHub repo
   - Choose Docker as the environment
   - Set the port to `8080` but on Render its 10000
   - Set environment variables:
     - `app.jwt.secret=Freight-256-bit-secret-Cargo-256-bit-secret`
     - `app.cors.allowed-origins=*`
3. Deploy and wait for the build to finish
4. Visit your public URL (e.g., `https://your-app.onrender.com/swagger-ui.html`)

**Usage:**
- Use `/api/v1/auth/login` to get a JWT token
- Click "Authorize" in Swagger UI (usually at the top right of the page) and paste the token
- Try any secured endpoint

> **Note:** This is a public demo. Do not use for production or sensitive data.

---
