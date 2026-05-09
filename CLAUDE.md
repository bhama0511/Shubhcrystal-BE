# ShubhCrystals — Backend

Spring Boot REST API for the ShubhCrystals crystal bracelet e-commerce site.

## Tech Stack

- **Java 17**, **Spring Boot 3.2.3**
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL** on [Neon](https://neon.tech) (hosted serverless Postgres)
- **H2** not used — Neon is used even in local dev
- **Maven** (use `mvnw.cmd` on Windows if `mvn` is not on PATH)

## How to Run

```bash
# Requires Java 17+ and Maven (or use mvnw.cmd)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Windows without Maven on PATH:
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Runs on **port 8081**. Frontend Vite proxy expects this port.

## Environment / Profiles

| Profile | Config file | Use for |
|---|---|---|
| `local` | `application-local.properties` | Local dev — gitignored, has real Neon credentials |
| (default) | `application.properties` | CI / production — reads from env vars |

### Required env vars for production

```
DB_URL        jdbc:postgresql://<neon-host>/neondb?sslmode=require
DB_USERNAME   neondb_owner
DB_PASSWORD   <secret>
CORS_ORIGINS  https://your-frontend-domain.com
```

`application-local.properties` is gitignored — never commit it.

## Folder Structure

```
src/main/java/com/shubhcrystals/
├── ShubhcrystalsApplication.java   # Entry point + seed data (runs if table is empty)
├── config/
│   └── CorsConfig.java             # Reads shubhcrystals.cors.allowed-origins
├── controller/                     # HTTP layer only — parse request, call service, return response
│   └── ProductController.java      # GET/POST/PUT/DELETE /api/products
├── dto/                            # Add here: separate API shapes from JPA entities
├── model/                          # JPA entities mapped to Neon tables
│   └── Product.java
├── repository/                     # Spring Data JPA interfaces — DB queries only
│   └── ProductRepository.java
└── service/                        # All business logic lives here
    └── ProductService.java
```

## API Endpoints

Base path: `/api`

### Products

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/products` | All available products |
| `GET` | `/api/products?stone=Amethyst` | Filter by stone type |
| `GET` | `/api/products/{id}` | Single product |
| `POST` | `/api/products` | Create product |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Delete product |

### Coming soon

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register user |
| `POST` | `/api/auth/login` | Login → returns JWT |
| `POST` | `/api/orders` | Place order |
| `GET` | `/api/orders/my` | Orders for logged-in user |
| `PUT` | `/api/orders/{id}/status` | Admin: update order status |

## Database — Neon PostgreSQL

Connection: `ep-purple-rice-ao7ou1mw.c-2.ap-southeast-1.aws.neon.tech`
Database: `neondb`

Tables (managed by Hibernate `ddl-auto=update`):
- `products` — id, name, stone, description, price, chakra, badge, emoji, available
- `product_benefits` — product_id, benefit (one-to-many via @ElementCollection)

Seed data runs automatically on first start if `products` table is empty (see `ShubhcrystalsApplication.java`).

**Never set `ddl-auto=create` in production** — it drops all tables.

## Key Conventions

- **Controller → Service → Repository** — never skip layers. Controller never touches the repository directly.
- **Add DTOs** before building auth/orders — don't expose JPA entities directly as API responses (password hash exposure risk, over-fetching).
- **Validation** — use `@Valid` + Bean Validation annotations on request bodies. Controller should not contain `if (x == null)` checks.
- **Error responses** — always return `Map.of("error", message)` with appropriate HTTP status. Never let stack traces reach the client.
- **Logging** — use SLF4J (`private static final Logger log = LoggerFactory.getLogger(...)`), never `System.out.println`.

## Current State (as of project start)

Built:
- [x] Product CRUD API
- [x] Stone-based filter (`?stone=X`)
- [x] CORS configured for localhost:3000
- [x] Neon PostgreSQL connected
- [x] Seed data (6 crystal bracelets with emoji + benefits)
- [x] Maven wrapper (`mvnw.cmd`)

Not yet built:
- [ ] DTOs (ProductResponse, OrderRequest etc.)
- [ ] Spring Security + JWT auth
- [ ] User model + registration/login
- [ ] Order model + checkout endpoint
- [ ] Razorpay webhook handler
- [ ] Admin role + protected endpoints
- [ ] Image URL field on Product (Cloudinary)
- [ ] Pagination on product list

## 15-Day Build Plan

| Days | Feature |
|---|---|
| 1–2 | Add `imageUrl` to Product, image upload endpoint (Cloudinary) |
| 3–4 | Spring Security + JWT — User model, register/login endpoints |
| 5–6 | Order model (PENDING/CONFIRMED/SHIPPED/DELIVERED), place order endpoint |
| 7–8 | Razorpay payment — create order, verify webhook |
| 9–10 | My orders endpoint (auth-protected) |
| 11–12 | Admin endpoints — list all orders, update status |
| 13 | Input validation hardening + proper error responses |
| 14 | Rate limiting, env var audit, production config |
| 15 | Deploy to Railway or Render |
