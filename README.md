# DreamShops API (Enterprise E-Commerce Backend)

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) 

This project represents the culmination of my comprehensive study in the Spring ecosystem. It is designed to simulate a robust, enterprise-level e-commerce backend, focusing on data integrity, security, and scalable architecture.

The goal is to build an interactive full-stack application applying advanced Spring Boot technologies.

## Key Features & Highlights

### Security & Authentication
- **Robust Role-Based Access Control (RBAC)**: Implements Spring Security with JWT (JSON Web Token).
- **Graceful Error Handling**: Custom exception handling ensures that unauthorized actions return appropriate `401 Unauthorized` or `403 Forbidden` statuses, replacing generic `500 Internal Server Errors`.

### Order Processing System
- **Transactional Integrity**: Uses `@Transactional` to ensure atomic operations.
- **Smart Inventory Management**: Automatically deducts inventory upon order placement.
- **Auto-Clear Cart**: Implements a logic flow where the shopping cart is automatically verified and cleared immediately after a successful order, preventing duplicate processing.

### API Documentation
- **OpenAPI 3.1 Integration**: Fully integrated with **SpringDoc (Swagger)** for auto-generated, interactive API documentation.
- **Developer-Friendly**: Provides detailed schemas for all requests and responses.

---

## Technology Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5
- **Database:** PostgreSQL
- **Security:** Spring Security, JWT (jjwt)
- **ORM:** Spring Data JPA (Hibernate)
- **Docs:** SpringDoc OpenAPI (Swagger UI)
- **Tools:** Maven, Lombok, ModelMapper

---

### Prerequisites (Before You Run)

To run this application locally, ensure you have the following installed:

1.  **Java Development Kit (JDK) 21**: Ensure `JAVA_HOME` is configured.
2.  **Maven 3.8+**: For building the project and managing dependencies.
3.  **PostgreSQL Database**:
    * Ensure the service is running on default port `5432`.
    * Create a database named `dream_shops_db` (or update `application.properties`).
4.  **IDE / Editor**: IntelliJ IDEA (Recommended) or Emacs.

---

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/Shaoen-Lin/dreamshops-api.git
cd dreamshops-api
```
### 2. Configure Database
Open src/main/resources/application.properties and update your PostgreSQL credentials:
```bash
spring.datasource.url=jdbc:postgresql://localhost:5432/dream_shops_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```
### 3. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```
Note on Data Seeding: Upon the first startup, the application includes a Data Seeder that will automatically populate your database with sample products (e.g., Apple, Samsung devices) and default roles. You can start testing immediately without manual data entry.

## API Testing & Documentation
Since this is a backend-focused project without a frontend UI, we provide two professional ways to test the API:

### Option 1: Swagger UI (Browser)
Once the server is running, visit:
 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

* Visualize and test endpoints interactively.
* Download the `swagger.json` definition.

### Option 2: Rest-Lisp
For a hacker-style, keyboard-driven testing workflow, I have developed a custom **Emacs Lisp** tool that integrates seamlessly with this project.

* **Features**: Swagger import, cURL import, auto-completion, and result beautification.
* **Get the Tool**:  **[Rest-Lisp](https://github.com/Shaoen-Lin/rest-lisp)**

### Option 3: Postman
Standard Postman collections are also supported. You can import the OpenAPI spec directly into Postman.
