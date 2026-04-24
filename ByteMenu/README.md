# ![BM](https://img.shields.io/badge/BM-1F4E79?style=flat&logoColor=white) ByteMenu — Cafeteria Management System

![Java](https://img.shields.io/badge/Java-17+-blue) ![JavaFX](https://img.shields.io/badge/JavaFX-21-blue) ![Maven](https://img.shields.io/badge/Maven-build-gray) ![SQLite](https://img.shields.io/badge/SQLite-database-gray) ![Version](https://img.shields.io/badge/Sprint_3-v0.3-green)

A desktop JavaFX application for managing university cafeteria operations — browse the menu, place orders, reserve seats, track order status, and manage everything from an admin/chef portal.

---

## Features

- **Browse menu** — filter by category, search items, add to cart with quantity tracking
- **Place orders** — pay by card, university wallet, or at the counter
- **Reserve seats** — real-time seat availability
- **Track orders** — check order status without logging in
- **Admin / Chef portal** — view and update all order statuses

---

## Prerequisites

- Java 17 or higher → check with `java -version`
- Maven 3.8+ → check with `mvn -version`
- JavaFX SDK is bundled via Maven — no manual install needed
- SQLite database is auto-created on first run

---

## Getting Started

```bash
# 1. Clone the repo
git clone https://github.com/your-username/ByteMenu.git
cd ByteMenu

# 2. Build and run
mvn clean javafx:run
```

---

## Project Structure

```
ByteMenu/
├── src/main/java/com/bytemenu/
│   ├── controller/   # UI controllers (Menu, Order, Admin, Login...)
│   ├── dao/          # Database access objects
│   ├── model/        # Data models (User, Order, MenuItem...)
│   └── util/         # Cart, session, and veto managers
├── src/main/resources/
│   ├── fxml/         # Screen layouts
│   ├── css/          # Stylesheet
│   └── sql/          # Database schema
└── pom.xml
```

---

## Login Roles

| Role | Access |
|------|--------|
| Student | Browse menu, place orders, reserve seats, view own orders |
| Admin / Chef | View and manage all orders |

---

## Built With

- [JavaFX 21](https://openjfx.io/) — UI framework
- [SQLite](https://www.sqlite.org/) via JDBC — local database
- [Maven](https://maven.apache.org/) — build and dependency management

---

*ByteMenu · Sprint 3 · v0.3*
