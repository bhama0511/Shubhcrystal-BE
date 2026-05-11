# ShubhCrystals — Backend

Spring Boot REST API for the ShubhCrystals crystal bracelet e-commerce site.

---

## ⚠️ MOBILE-FIRST API MANDATE

The frontend is used by 90% mobile users. API design must support this:

1. **Lean responses** — never send fields the client doesn't need. Add a `ProductResponse` DTO before the catalog grows; raw entities expose internals and bloat payloads.
2. **Fast cold-start** — Neon (serverless Postgres) has cold-start latency. Keep queries simple. Avoid N+1 queries — use `@EntityGraph` or `JOIN FETCH` when loading related data.
3. **Pagination on list endpoints** — product list will grow. Add `?page=0&size=12` before deploying to production.
4. **Small image payloads** — image URLs point to Cloudinary CDN. Never serve binary image data from this API.
5. **Proper HTTP status codes** — mobile clients rely on status codes, not error message text. `400` for bad input, `401` for unauthenticated, `403` for unauthorized, `404` for not found.
6. **No stack traces in responses** — Spring Boot default error format leaks internals. Ensure `ErrorController` or `@ControllerAdvice` returns clean `{ "error": "..." }` JSON.

---

## Tech Stack

- **Java 17**, **Spring Boot 3.2.3**
- **Spring Data JPA** + **Hibernate 6**
- **Spring Security 6** + **JJWT 0.12.6** (JWT auth)
- **PostgreSQL** on [Neon](https://neon.tech) (hosted serverless Postgres)
- **Cloudinary** (image upload, Java SDK `cloudinary-http44 1.39.0`)
- **Maven** (use `mvnw.cmd` on Windows if `mvn` is not on PATH)

## How to Run

```bash
# Requires Java 17+ and Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Windows without Maven on PATH:
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Runs on **port 8081**. Frontend Vite dev proxy expects this port.

## Environment / Profiles

| Profile | Config file | Use for |
|---|---|---|
| `local` | `application-local.properties` | Local dev — gitignored, has real credentials |
| (default) | `application.properties` | CI / production — reads env vars |

### Required env vars for production

```
DB_URL                   jdbc:postgresql://<neon-host>/neondb?sslmode=require
DB_USERNAME              neondb_owner
DB_PASSWORD              <secret>
JWT_SECRET               <random string, min 32 chars>
CLOUDINARY_CLOUD_NAME    dsbo7fly3
CLOUDINARY_API_KEY       <key>
CLOUDINARY_API_SECRET    <secret>
CORS_ORIGINS             https://your-frontend-domain.com

# Email (Brevo SMTP in production, Mailtrap sandbox locally)
SMTP_HOST                smtp-relay.brevo.com
SMTP_PORT                587
SMTP_USERNAME            <brevo SMTP login>
SMTP_PASSWORD            <brevo SMTP key>
EMAIL_FROM_ADDRESS       noreply@shubhcrystals.com
EMAIL_FROM_NAME          ShubhCrystals
EMAIL_ADMIN_RECIPIENTS   admin@shubhcrystals.com
FRONTEND_BASE_URL        https://shubhcrystals.com
THYMELEAF_CACHE          true
```

`application-local.properties` is gitignored — **never commit it**. It holds Mailtrap creds for dev so emails land in a sandbox instead of real inboxes.

## Folder Structure

```
src/main/java/com/shubhcrystals/
├── ShubhcrystalsApplication.java     # Entry point + seed (6 products + admin user)
├── config/
│   ├── AsyncConfig.java              # @EnableAsync + emailTaskExecutor (ThreadPoolTaskExecutor)
│   ├── CloudinaryConfig.java         # Cloudinary bean (cloud-name, api-key, api-secret)
│   ├── EmailProperties.java          # @ConfigurationProperties(prefix="app") — fromAddress, fromName, adminRecipients, frontendBaseUrl
│   ├── JwtAuthFilter.java            # OncePerRequestFilter — validates Bearer token
│   └── SecurityConfig.java           # HTTP security rules, CORS, PasswordEncoder bean
├── controller/
│   ├── AdminController.java          # GET /api/admin/stats|users|products (ADMIN only)
│   ├── AdminOrderController.java     # GET /api/admin/orders, PUT /api/admin/orders/{id}/status (ADMIN only)
│   ├── AuthController.java           # POST /api/auth/login|register (public)
│   ├── CorsConfig.java               # CorsConfigurationSource bean
│   ├── ImageUploadController.java    # POST /api/upload (ADMIN only)
│   ├── OrderController.java          # POST /api/orders, GET /api/orders/my (authenticated)
│   └── ProductController.java        # GET/POST/PUT/DELETE /api/products
├── dto/                              # Request/response shapes — keep separate from entities
│   ├── AuthResponse.java             # record { token, email, name, role }
│   ├── ForgotPasswordRequest.java    # { email } with @Valid
│   ├── LoginRequest.java             # { email, password } with @Valid
│   ├── OrderItemRequest.java         # { productId, quantity }
│   ├── OrderItemResponse.java        # record — snapshot fields per line item
│   ├── OrderResponse.java            # record — full order incl. customer + items + totals
│   ├── PlaceOrderRequest.java        # items + shipping address fields with @Valid
│   ├── RegisterRequest.java          # { name, email, password } with @Valid
│   ├── ResetPasswordRequest.java     # { token, password } with @Valid (min 8 chars)
│   ├── UpdateOrderStatusRequest.java # { status }
│   └── UserResponse.java             # record { id, name, email, role }
├── model/
│   ├── Order.java                    # orders table — userId, items, totals, status, shipping, timestamps
│   ├── OrderItem.java                # order_items — snapshot of productId/name/imageUrl/emoji/price/qty
│   ├── OrderStatus.java              # Enum: PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
│   ├── PasswordResetToken.java       # password_reset_tokens — userId, token, expiresAt, used (30-min TTL)
│   ├── Product.java                  # products table — id, name, stone, price, imageUrl, emoji, etc.
│   ├── Role.java                     # Enum: USER | ADMIN
│   └── User.java                     # app_users table — implements UserDetails
├── repository/
│   ├── OrderRepository.java          # findByUserIdOrderByCreatedAtDesc(), findAllByOrderByCreatedAtDesc()
│   ├── PasswordResetTokenRepository  # findByToken(), deleteByUserId()
│   ├── ProductRepository.java        # findByAvailableTrue(), findByStoneIgnoreCaseAndAvailableTrue()
│   └── UserRepository.java           # findByEmail(), existsByEmail()
└── service/
    ├── AuthService.java              # register() + login() + requestPasswordReset() + resetPassword()
    ├── EmailService.java             # @Async sendWelcome / sendPasswordReset / sendOrderPlaced / sendOrderStatusUpdate / sendAdminNewOrder
    ├── JwtService.java               # generateToken(), isTokenValid(), extractEmail()
    ├── OrderService.java             # placeOrder (snapshots prices server-side), getMyOrders, getAllOrders, updateStatus
    ├── ProductService.java           # CRUD — update() copies ALL fields including imageUrl/emoji/benefits
    └── UserDetailsServiceImpl.java   # Loads User by email for Spring Security

resources/templates/email/           # Thymeleaf HTML email templates (inline-styled, mobile-friendly)
├── admin-new-order.html              # Admin alert with customer info, items, totals
├── order-placed.html                 # Customer confirmation with itemised receipt + shipping address
├── order-status.html                 # Status-aware copy (CONFIRMED/SHIPPED/DELIVERED/CANCELLED)
├── password-reset.html               # Reset link with 30-min expiry note
└── welcome.html                      # New-user greeting with shop CTA
```

## API Endpoints

Base path: `/api`

### Auth (public)

| Method | Path | Body | Response |
|---|---|---|---|
| `POST` | `/api/auth/register` | `{ name, email, password }` | `{ token, email, name, role }` |
| `POST` | `/api/auth/login` | `{ email, password }` | `{ token, email, name, role }` |
| `POST` | `/api/auth/forgot-password` | `{ email }` | `{ message }` — always 200 (don't leak whether email exists) |
| `POST` | `/api/auth/reset-password` | `{ token, password }` | `{ message }` — 400 on invalid/expired token |

> Reset tokens expire after 30 minutes. Requesting a new reset for the same user invalidates any previous tokens.

### Products (GET = public, write = ADMIN only)

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/products` | Public | All available products |
| `GET` | `/api/products?stone=X` | Public | Filter by stone type |
| `GET` | `/api/products/{id}` | Public | Single product |
| `POST` | `/api/products` | ADMIN | Create product |
| `PUT` | `/api/products/{id}` | ADMIN | Update product (all fields incl. imageUrl) |
| `DELETE` | `/api/products/{id}` | ADMIN | Delete product |

### Upload (ADMIN only)

| Method | Path | Body | Response |
|---|---|---|---|
| `POST` | `/api/upload` | `multipart/form-data` file | `{ url, publicId }` |

> Uploads to Cloudinary folder `shubhcrystals/products/`. Returns the CDN URL to store on the product.

### Orders (authenticated)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/orders` | User | Place order. Body: `{ items: [{productId, quantity}], shippingName, shippingPhone, shippingAddress, shippingCity, shippingPincode }`. Returns full `OrderResponse`. Server snapshots prices — never trusts client. |
| `GET` | `/api/orders/my` | User | Current user's orders, newest first |

### Admin (ADMIN only)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/admin/stats` | `{ totalProducts, availableProducts, totalUsers }` |
| `GET` | `/api/admin/users` | All users `[{ id, name, email, role }]` |
| `GET` | `/api/admin/products` | All products including hidden ones |
| `GET` | `/api/admin/orders` | All orders, newest first |
| `PUT` | `/api/admin/orders/{id}/status` | Update status. Body: `{ status: PENDING\|CONFIRMED\|SHIPPED\|DELIVERED\|CANCELLED }` |

## Database — Neon PostgreSQL

**Host:** `ep-purple-rice-ao7ou1mw.c-2.ap-southeast-1.aws.neon.tech`
**Database:** `neondb`
**DDL:** `ddl-auto=update` — Hibernate adds/alters columns, never drops

| Table | Columns |
|---|---|
| `products` | id, name, stone, description, price, chakra, badge, emoji, image_url, available |
| `product_benefits` | product_id, benefit (via `@ElementCollection`) |
| `app_users` | id, name, email, password (BCrypt), role |
| `orders` | id, user_id, subtotal, shipping, total, status, shipping_name, shipping_phone, shipping_address, shipping_city, shipping_pincode, created_at, updated_at |
| `order_items` | id, order_id, product_id, product_name, product_image_url, product_emoji, price, quantity (snapshots — survive product edits) |
| `password_reset_tokens` | id, user_id, token (UUID), expires_at, used, created_at — 30-min TTL, single-use |

**Seeded on first boot:**
- 6 crystal bracelets (Amethyst, Rose Quartz, Clear Quartz, Black Tourmaline, Citrine, Lapis Lazuli)
- 1 admin user: `admin@shubhcrystals.com / Admin@123`

**Never set `ddl-auto=create` in production** — it drops all tables.

## Security Rules

| Endpoint pattern | Who |
|---|---|
| `GET /api/products/**` | Everyone |
| `POST /api/auth/**` | Everyone |
| `POST /api/products` | ADMIN |
| `PUT /api/products/**` | ADMIN |
| `DELETE /api/products/**` | ADMIN |
| `POST /api/upload` | ADMIN |
| `/api/admin/**` | ADMIN |
| `/api/orders/**` | Authenticated (USER or ADMIN) |
| Everything else | Authenticated (USER or ADMIN) |

JWT token format: `Authorization: Bearer <token>` header. Token expiry: 7 days (configurable via `jwt.expiry-ms`).

## Key Conventions

- **Controller → Service → Repository** — never skip layers. Controller never touches repository directly.
- **`ProductService.update()` must copy ALL fields** — when adding new fields to `Product`, always add the corresponding `existing.setX(updated.getX())` line in `update()`. Missing this causes silent data loss on edit.
- **DTOs for auth** — `AuthResponse`, `LoginRequest`, `RegisterRequest`, `UserResponse` are in `dto/`. `Product` entity is still used directly in controllers (add `ProductResponse` DTO before launch).
- **Validation** — use `@Valid` + Bean Validation on request bodies. Controller should not contain `if (x == null)` checks.
- **Error responses** — always return `Map.of("error", message)` with correct HTTP status. Never let stack traces reach the client.
- **Logging** — use SLF4J. Never `System.out.println`.
- **`application-local.properties`** is gitignored — real credentials go there only.

## Current State

### Done

- [x] Product CRUD API with stone filter
- [x] Neon PostgreSQL connected (`ddl-auto=update`)
- [x] Seed data — 6 products + admin user on first boot
- [x] Maven wrapper (`mvnw.cmd`) — no local Maven required
- [x] Cloudinary image upload (`POST /api/upload`) → returns CDN URL
- [x] `imageUrl` field on `Product` entity
- [x] Spring Security 6 + JWT (JJWT 0.12.6)
- [x] `User` model + `Role` enum (USER / ADMIN)
- [x] Register + Login endpoints with BCrypt password hashing
- [x] JWT filter validates `Authorization: Bearer <token>` on every request
- [x] RBAC — product write + upload + admin endpoints gated to ADMIN role
- [x] Admin endpoints: stats, all users, all products (including hidden)
- [x] DTOs: `AuthResponse`, `LoginRequest`, `RegisterRequest`, `UserResponse`
- [x] CORS configured via `CorsConfigurationSource` bean
- [x] **Order model** — `Order` + `OrderItem` entities, `OrderStatus` enum (PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED)
- [x] **Place order endpoint** — `POST /api/orders` — server-side price snapshot, computes shipping (free over ₹999, else ₹99) + total
- [x] **My orders endpoint** — `GET /api/orders/my` — newest first
- [x] **Admin order endpoints** — `GET /api/admin/orders`, `PUT /api/admin/orders/{id}/status`
- [x] **Order DTOs** — `PlaceOrderRequest`, `OrderItemRequest`, `OrderResponse`, `OrderItemResponse`, `UpdateOrderStatusRequest`
- [x] **Email infrastructure** — `spring-boot-starter-mail` + Thymeleaf, `AsyncConfig` (`emailTaskExecutor`), `EmailProperties` (`app.email.*`)
- [x] **Transactional emails** — welcome (on register), order placed + admin alert (on placeOrder), order status update (on updateStatus, only when status actually changes), password reset
- [x] **Email templates** — 5 mobile-friendly HTML templates in `resources/templates/email/`
- [x] **Password reset flow** — `PasswordResetToken` entity (30-min TTL, single-use), `POST /api/auth/forgot-password`, `POST /api/auth/reset-password`. Forgot-password is silent on unknown emails (no enumeration).

### Not yet built

- [ ] **Razorpay integration** — create Razorpay order, verify payment webhook (currently orders default to PENDING / Cash on Delivery)
- [ ] **Email verification on signup** — currently signup is trust-based; defer until needed
- [ ] **Domain authentication** — add SPF + DKIM TXT records at registrar so Brevo can send from `noreply@shubhcrystals.com`
- [ ] **`ProductResponse` DTO** — stop exposing raw JPA entity in product endpoints
- [ ] **Pagination** — `?page=0&size=12` on `GET /api/products`
- [ ] **Global exception handler** — `@ControllerAdvice` for consistent error JSON
- [ ] **Rate limiting** — protect auth endpoints from brute force
- [ ] **Production hardening** — disable H2 console guard, review all env vars

## 15-Day Build Plan

| Days | Feature | Status |
|---|---|---|
| 1–2 | Cloudinary upload endpoint + `imageUrl` on Product | ✅ Done |
| 3–4 | Spring Security + JWT + RBAC + User model + auth endpoints | ✅ Done |
| + | Admin endpoints (stats, users, all products) | ✅ Done |
| + | Full mobile optimisation (FE — API unchanged) | ✅ Done |
| 5–6 | Order model + place order + my orders endpoints | ✅ Done |
| + | Admin order management endpoints (combined into 5–6) | ✅ Done |
| 7–8 | Razorpay payment webhook + verify endpoint | 🔲 (paused — Razorpay approval pending) |
| 7–8 (alt) | SMTP / transactional emails + password reset | ✅ Done |
| 11 | `ProductResponse` DTO + global exception handler | 🔲 |
| 12 | Pagination on product list | 🔲 |
| 13 | Rate limiting on auth endpoints | 🔲 |
| 14 | Production config audit + security review | 🔲 |
| 15 | Deploy to Railway or Render | 🔲 |
