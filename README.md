# What a Fridge - Backend REST API 💻

This is the backend REST API for **What a Fridge**. It is built using **Java 25** and **Spring Boot 4.1.1** with **PostgreSQL** as the database. It handles secure JWT authentication, inventory tracking, meal logging, and integrates with the **Open Food Facts API** to fetch nutritional data dynamically.

---

## 🛠️ Technology Stack

- **Java Version**: 25 (Temurin JDK)
- **Framework**: Spring Boot 4.1.1 (WebMVC, Security, Validation, JPA)
- **Database**: PostgreSQL
- **Database Migrations/Schema**: Hibernate (`ddl-auto: update` for dev, SQL schema initialization script for production)
- **Security**: Spring Security with JWT (JSON Web Tokens) stateless authentication
- **External Integration**: Open Food Facts API (text search and barcode lookups)
- **Build Tool**: Maven

---

## ✨ Features

1. **Authentication**: JWT-based login and session verification (`/api/auth`).
2. **Auto-Seeding**: A `DataSeeder` run at startup seeds an initial admin user (with customizable username and password) if the user table is empty.
3. **Fridge Inventory Management**: REST endpoints to get, add, and delete inventory items (with support for expiration dates and quantities).
4. **Meal Logger**: Tracks daily consumed food and quantity. Logs are grouped and fetched by date.
5. **Open Food Facts Lookup**: Proxy queries to Open Food Facts to fetch calories, protein, carbohydrates, and fat. Supports both text search and scanner barcode lookups.

---

## 📋 API Endpoints

All requests require a `Bearer <token>` in the `Authorization` header, except for `/api/auth/login`.

### Authentication (`/api/auth`)
* `POST /api/auth/login` — Public endpoint to log in. Returns JWT token and user info.
  * *Request Body*: `{"username": "...", "password": "..."}`
* `GET /api/auth/me` — Returns the current authenticated user's ID and username.

### Food Search & Metadata (`/api/foods`)
* `GET /api/foods/search?query=...` — Search products on Open Food Facts by text.
* `GET /api/foods/barcode/{barcode}` — Fetch a specific product's metrics by barcode (EAN/UPC).

### Inventory (`/api/inventory`)
* `GET /api/inventory` — Fetch all inventory items for the authenticated user.
* `POST /api/inventory` — Add an item to the user's inventory.
  * *Request Body*: `{"food": FoodDto, "quantity": Double, "expirationDate": "YYYY-MM-DD"}`
* `DELETE /api/inventory/{id}` — Remove an item from the inventory.

### Meal Logging (`/api/meals`)
* `GET /api/meals?date=YYYY-MM-DD` — Fetch all logged meals for a specific day.
* `POST /api/meals` — Log a new meal.
  * *Request Body*: `{"food": FoodDto, "quantity": Double}`

---

## ⚙️ Configuration & Profiles

The application uses **Spring Boot Profiles** to manage configuration across environments:

* **`default` (active: `dev`)**: Group profile which loads `application-dev.yml` and `application-local.yml` configuration details.
* **`local`**: Used for local environment variables and overrides.
* **`prod`**: Used for production builds. Disables debug features, enables CORS mapping to custom domains, and expects DB credentials via environment variables.

### Environment Variables

| Variable Name | Default (Dev) | Description |
| :--- | :--- | :--- |
| `DB_PASSWORD` | `postgres` | PostgreSQL Database Password |
| `ADMIN_USERNAME` | *(Required)* | Seeded Administrator Username |
| `ADMIN_PASSWORD` | *(Required)* | Seeded Administrator Password |
| `JWT_SECRET` | *(Required)* | Key for signing JWTs (Must be at least 256 bits/32 bytes) |
| `CORS_ALLOWED_ORIGINS`| `*` | List of allowed CORS domains (e.g. `http://localhost:5173`) |
| `OFF_CONTACT_EMAIL` | *(Required)* | Email sent in the user-agent header to Open Food Facts |

---

## 🚀 Running Locally

### Prerequisites
1. **Java 25 SDK** installed and configured in your environment.
2. **PostgreSQL** running locally.
3. A database named `whatafridge` created.

### Running with Maven
Start the application using the Spring Boot Maven plugin:

```bash
# From the backend directory
# On Windows:
mvnw.cmd clean spring-boot:run

# On macOS/Linux:
chmod +x mvnw
./mvnw clean spring-boot:run
```

By default, the server will start on port `8080`.
The application automatically runs with the `dev,local` profiles loaded, initializing the PostgreSQL database with the tables in `schema.sql` (if not present).

---

## 🐳 Docker Setup

You can build and run the backend inside a Docker container:

```bash
# Build the Docker image
docker build -t whatafridge-backend .

# Run the container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/whatafridge \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e ADMIN_USERNAME=admin \
  -e ADMIN_PASSWORD=supersecureadmin \
  -e JWT_SECRET=some_super_long_secret_key_at_least_64_characters_long_for_security \
  -e CORS_ALLOWED_ORIGINS=http://localhost:5173 \
  -e OFF_CONTACT_EMAIL=yourname@example.com \
  whatafridge-backend
```

---

## 🚢 Deploying to Dokploy (Production)

To deploy to **Dokploy**:

1. Create a new **PostgreSQL Database** in Dokploy named `whatafridge` and retrieve its internal connection string.
2. Run the queries in [`schema.sql`](file:///d:/repos/what-a-fridge/backend/src/main/resources/schema.sql) manually on the database using a client like DBeaver (Hibernate is set to `validate` in production, meaning it will not auto-generate tables).
3. Create a new **Application** in Dokploy named `whatafridge-backend` and link it to your git repository.
4. Set the **Build Provider** to **Dockerfile**.
5. Add the necessary production environment variables in the **Environment** tab:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL=jdbc:postgresql://<dokploy-postgres-service-name>:5432/whatafridge`
   - `DB_USERNAME=postgres`
   - `DB_PASSWORD=<db-password>`
   - `ADMIN_USERNAME=<custom-admin>`
   - `ADMIN_PASSWORD=<custom-admin-password>`
   - `JWT_SECRET=<secure-random-string>`
   - `CORS_ALLOWED_ORIGINS=https://app.yourdomain.com`
   - `OFF_CONTACT_EMAIL=yourname@example.com`
6. Add your backend domain (e.g. `api.yourdomain.com`) in the **Domains** tab and map it to **Container Port** `8080`.
7. Trigger a deployment.
