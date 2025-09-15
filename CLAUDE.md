# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot e-commerce application called "SanTech Market" - a marketplace system with the following key features:
- User management with role-based access (CLIENT, ADMIN, MANAGER)
- Product catalog with categories
- Shopping cart functionality
- Order management system
- Notifications system
- Promotions and discounts

## Technology Stack

- **Framework**: Spring Boot 3.5.5 with Java 21
- **Database**: PostgreSQL with Liquibase migrations
- **Security**: Spring Security with JWT authentication
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Documentation**: SpringDoc OpenAPI (Swagger)

## Development Commands

### Build and Run
```bash
# Build the application
./mvnw clean compile

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Package as JAR
./mvnw clean package
```

### Database Management
- Database migrations are managed by Liquibase
- Migration files are in `src/main/resources/db/changelog/`
- Liquibase runs automatically on application startup
- Database URL: `jdbc:postgresql://localhost:5432/santech_market`

## Project Structure

### Entities and Business Logic
- **Base Entity**: `BaseEntity` provides common fields (id, createdAt, updatedAt) for all entities
- **Core Entities**: User, Product, Category, Cart, CartItem, Order, OrderItem, Notification, Promotion
- **Enums**: UserRole, OrderStatus, NotificationType
- **Package**: `kg.santechmarket.entity`

### Security Architecture
- Users implement Spring Security's `UserDetails` interface
- JWT-based authentication with 24-hour token expiration
- Role-based authorization (CLIENT, ADMIN, MANAGER)
- No self-registration - users are created by managers through admin interface

### Database Schema
- Uses Liquibase for version-controlled database migrations
- Structured migrations in versioned directories (`v1.0.0/`)
- Includes indexes and initial data setup
- All tables follow consistent naming with snake_case

### Configuration
- Main config in `application.yml`
- Server runs on port 8080 with context path `/api/v1`
- Actuator endpoints available at `/actuator`
- Swagger UI available at `/swagger-ui.html`
- Database logging enabled for development

## Development Notes

### Current Architecture State
This appears to be a foundational setup with:
- Complete entity model defined
- Database schema fully migrated
- Spring Security configuration in place
- No controllers, services, or repositories implemented yet

### Key Business Rules
- Phone numbers must follow Kyrgyzstan format: +996XXXXXXXXX
- Users have unique usernames, emails, and phone numbers
- Cart is one-to-one with User
- Orders have one-to-many relationship with Users
- Products belong to Categories
- Promotions can be applied to products and categories

### Security Considerations
- JWT secret key is set for development (should be changed in production)
- Passwords are validated with minimum 6 characters
- Email format validation included
- toString excludes password field for security