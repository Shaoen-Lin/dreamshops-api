# DreamShops API (Enterprise E-Commerce Backend)

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue) 

A production-ready RESTful e-commerce backend built with **Spring Boot 3**, featuring JWT-based authentication, role-based access control, and a full shopping workflow from product browsing to order placement.

> This project was developed based on [dailycodework/dream-shops](https://github.com/dailycodework/dream-shops) as a learning foundation. I identified and resolved multiple bugs in the original codebase, and extended the implementation with additional fixes and improvements.

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.2 |
| Security | Spring Security + JWT (jjwt) |
| Persistence | Spring Data JPA + Hibernate |
| Database | Postgre |
| Mapping | ModelMapper |
| Build Tool | Maven |
| Utilities | Lombok |

---

## ✨ Features

### 🔐 Authentication & Authorization
- User registration and login via `POST /api/v1/auth/login`
- JWT token generation with embedded user ID and roles (`ROLE_ADMIN`, `ROLE_USER`)
- Stateless authentication via `AuthTokenFilter` — validates Bearer tokens on every request
- `JwtAuthEntryPoint` returns structured JSON `401 Unauthorized` responses for unauthenticated access
- Method-level security using `@PreAuthorize("hasRole('ROLE_ADMIN')")` on sensitive operations (add / update / delete products)

### 📦 Product Management
- Full CRUD operations for products
- Rich query support: filter by name, brand, category, brand + name, category + brand
- DTO projection via ModelMapper — `ProductDto` includes nested `ImageDto` list
- Duplicate product guard: throws `AlreadyExistsException` if the same brand + name combination already exists
- Admin-only write operations enforced at the service layer

### 🗂️ Category Management
- Create, read, update, and delete product categories
- Conflict detection for duplicate category names
- Auto-create category during product creation if it does not exist

### 🛒 Cart & Cart Item Management
- Cart is lazily initialized per user — created on first item addition
- Add, remove, and update item quantities in cart
- Total price recalculated on every update using stream aggregation to avoid stale state
- JWT-aware cart operations: `CartItemController` resolves the authenticated user via `SecurityContextHolder` before cart lookup
- `@Transactional` on cart mutation operations to ensure data consistency

### 📋 Order Management
- Place orders from an existing cart
- Retrieve single order by ID or all orders by user ID
- Orders returned as `OrderDto` for clean API responses

### 🖼️ Image Management
- Upload multiple product images as `MultipartFile` (stored as SQL `Blob`)
- Download images by ID with correct MIME type headers (`Content-Disposition`, `Content-Type`)
- Update and delete images by ID
- `@Transactional` on download endpoint to correctly handle `Blob` streaming

### 👤 User Management
- Create, read, update, and delete users
- Passwords encoded with `BCryptPasswordEncoder`
- User responses projected as `UserDto` to avoid exposing sensitive fields

---

## 🐛 Bugs Fixed

The original codebase contained several issues that I identified and resolved:

### 1. Cart Total Amount Stale State Bug
**Location:** `CartItemService.updateItemQuantity()`

The original code called `cart.getTotalAmount()` after mutating item quantities, which returned the **pre-update cached value** instead of the recalculated total.

```java
// Before — returns stale total
BigDecimal totalAmount = cart.getTotalAmount();

// After — recalculates from current items
BigDecimal totalAmount = cart.getItems().stream()
    .map(CartItem::getTotalPrice)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

### 2. Cart Clear — Orphan CartItems Not Deleted
**Location:** `CartService.clearCart()`

The original implementation called `CartItemRepo.deleteAllByCartId(id)` directly, which bypassed JPA's relationship management and left orphaned records or caused constraint violations.

```java
// Before — bypasses JPA cascade
CartItemRepo.deleteAllByCartId(id);

// After — clears via managed collection, triggers orphanRemoval
cart.getItems().clear();
cart.setTotalAmount(BigDecimal.ZERO);
CartRepo.save(cart);
```

### 3. Image Download — Null Blob Not Guarded
**Location:** `ImageController.downloadImages()`

The original code attempted to read `image.getImage().getBytes(...)` without checking if the `Blob` field was `null`, causing a `NullPointerException` at runtime for images with missing data.

```java
// Added null guard before Blob access
if (image.getImage() == null) {
    throw new ResourceNotFoundException("Image data is empty for id=" + imageID);
}
```

### 4. JWT Validation — Specific Exceptions Swallowed
**Location:** `JwtUtils.validateToken()` and `AuthTokenFilter`

The original catch block silently swallowed specific JWT exceptions (`ExpiredJwtException`, `MalformedJwtException`, `SignatureException`, etc.) without re-throwing, causing the filter chain to proceed as if authentication succeeded.

```java
// After — catches all JWT-specific exceptions and re-throws as JwtException
} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException
       | SignatureException | IllegalArgumentException e) {
    throw new JwtException(e.getMessage());
}
```

The `AuthTokenFilter` now catches `JwtException` separately and returns HTTP `401` with a descriptive message before the filter chain continues.

### 5. Cart Initialization — Race Condition Safety
**Location:** `CartService`

Added `AtomicLong` for cart ID generation to prevent race conditions in concurrent cart creation scenarios, ensuring thread-safe ID allocation.

---

## 🗂️ Project Structure

```
src/main/java/com/Shawn/dream_shops/
├── controller/
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CartItemController.java
│   ├── CategoryController.java
│   ├── ImageController.java
│   ├── OrderController.java
│   ├── ProductController.java
│   └── UserController.java
├── service/
│   ├── cart/         (CartService, CartItemService + interfaces)
│   ├── product/      (ProductService + interface)
│   ├── category/     (CategoryService + interface)
│   ├── image/        (ImageService + interface)
│   ├── order/        (OrderService + interface)
│   └── user/         (UserService + interface)
├── model/            (JPA Entities: User, Product, Category, Cart, CartItem, Order, Image)
├── dto/              (ProductDto, ImageDto, OrderDto, UserDto)
├── repository/       (Spring Data JPA Repositories)
├── request/          (AddProductRequest, ProductUpdateRequest, LoginRequest, ...)
├── reponse/          (ApiResponse, JwtResponse, CreateUserRequest, ...)
├── exceptions/       (ResourceNotFoundException, AlreadyExistsException, ProductNotFoundException)
└── security/
    ├── jwt/          (JwtUtils, AuthTokenFilter, JwtAuthEntryPoint)
    ├── user/         (ShopUserDetails)
    └── service/      (ShopUserDetailsService)
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.8+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Shaoen-Lin/dreamshops-api.git
   cd dreamshops-api
   ```

2. **Configure the database** — edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/dreamshops
   spring.datasource.username=YOUR_DB_USER
   spring.datasource.password=YOUR_DB_PASSWORD

   api.prefix=/api/v1

   auth.token.jwtSecret=YOUR_BASE64_ENCODED_SECRET
   auth.token.expirationInMils=3600000
   ```

3. **Build and run**
   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080/api/v1`.

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/login` | Login and receive JWT | Public |

### Products
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/products/all` | Get all products | Public |
| GET | `/api/v1/products/product/{id}/product` | Get product by ID | Public |
| POST | `/api/v1/products/add` | Add product | Admin |
| PUT | `/api/v1/products/product/{id}/update` | Update product | Admin |
| DELETE | `/api/v1/products/product/{id}/delete` | Delete product | Admin |
| GET | `/api/v1/products/products/by/brand-and-name` | Filter by brand & name | Public |
| GET | `/api/v1/products/products/by/category-and-brand` | Filter by category & brand | Public |
| GET | `/api/v1/products/product/{name}/products` | Filter by name | Public |
| GET | `/api/v1/products/product/by-brand` | Filter by brand | Public |
| GET | `/api/v1/products/product/{category}/all/products` | Filter by category | Public |

### Categories
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/categories/all` | Get all categories | Public |
| POST | `/api/v1/categories/add` | Add category | Public |
| GET | `/api/v1/categories/category/{id}/category` | Get by ID | Public |
| PUT | `/api/v1/categories/category/{id}/update` | Update category | Public |
| DELETE | `/api/v1/categories/category/{id}/delete` | Delete category | Public |

### Cart
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/carts/{cartId}/my-cart` | Get cart | User |
| DELETE | `/api/v1/carts/{cartId}/clear` | Clear cart | User |
| GET | `/api/v1/carts/{cartId}/cart/total-price` | Get total price | User |
| POST | `/api/v1/cartItems/item/add` | Add item to cart | User |
| DELETE | `/api/v1/cartItems/cart/{cartId}/item/{productId}/remove` | Remove item | User |
| PUT | `/api/v1/cartItems/cart/{cartId}/item/{productId}/update` | Update quantity | User |

### Orders
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/orders/order` | Place order | User |
| GET | `/api/v1/orders/{orderId}/order` | Get order by ID | User |
| GET | `/api/v1/orders/{userId}/orders` | Get user orders | User |

### Images
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/images/upload` | Upload images | User |
| GET | `/api/v1/images/image/download/{imageID}` | Download image | Public |
| POST | `/api/v1/images/image/{imageID}/update` | Update image | User |
| DELETE | `/api/v1/images/image/{imageID}/update` | Delete image | User |

### Users
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/users/{id}/user` | Get user by ID | User |
| POST | `/api/v1/users/add` | Create user | Public |
| PUT | `/api/v1/users/{userId}/update` | Update user | User |
| DELETE | `/api/v1/users/{userId}/delete` | Delete user | User |

---

## 🔑 Authentication Flow

```
Client                             Server
  |                                  |
  |  POST /auth/login                |
  |  { email, password }  ─────────► |
  |                                  | AuthenticationManager.authenticate()
  |                                  | → BCrypt password verification
  |                                  | → JwtUtils.generateTokenForUser()
  |                                  |   (embeds userId + roles in JWT payload)
  |  ◄── 200 { token, userId }       |
  |                                  |
  |  GET /products (Bearer <token>)  |
  |  ──────────────────────────────► |
  |                                  | AuthTokenFilter.doFilterInternal()
  |                                  | → JwtUtils.validateToken()
  |                                  | → Load UserDetails from DB
  |                                  | → Set SecurityContextHolder
  |  ◄── 200 { data }               |
```

---

## 🧠 Design Decisions

**Interface-first service layer** — all services implement an interface (`IProductService`, `ICartService`, etc.), enabling loose coupling, easy mocking in tests, and adherence to the Dependency Inversion Principle.

**DTO pattern** — entities are never directly exposed in API responses. ModelMapper converts them to DTOs, preventing circular serialization issues (e.g. `Product → Image → Product`) and avoiding unintended field exposure.

**Stateless JWT security** — no server-side session state. Each request is independently authenticated via the `AuthTokenFilter`, making the API horizontally scalable.

**Lazy cart initialization** — a user's cart is only created when they first add an item, reducing unnecessary DB rows for users who browse without purchasing.

**`@Transactional` on Blob streaming** — MySQL's JDBC `Blob` object requires an active database connection to stream bytes. Without `@Transactional`, the connection is closed before the bytes are read, resulting in a runtime error.
