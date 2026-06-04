# Campus Digital Payment & Expense Management Platform

A console-based Java application for a college campus payment system — think
**Google Pay + Splitwise + College Wallet**. Students register, manage a digital
wallet, make campus payments, split expenses with each other, and view reports —
all backed by a relational database with full transaction management.

Built for Buildathon-1 as an evolution of the Smart Banking & Wallet System case study.

---

## Features

- **Student Management** — register, update, and search students; login with Student ID + PIN.
- **Digital Wallet** — add money, withdraw, and transfer, with a per-wallet balance cap
  and a daily transfer limit.
- **Campus Payments** — pay for canteen, library fines, hackathon fees, workshop fees,
  and hostel fees. Payment behaviour is modelled with a functional interface
  (`PaymentProcessor`).
- **Expense Sharing (Splitwise-style)** — create equal or unequal splits across a group,
  track pending dues, and settle them individually or all at once.
- **Transaction History & Reports** — built with Java Streams: total spend per student,
  top spenders, monthly summaries, and per-type counts.
- **Transaction Management** — wallet transfers debit, credit, and insert the transaction
  record as a single atomic unit; any failure triggers a full `rollback()`.
- **Fraud Detection** — flags students who make too many outgoing transactions inside a
  short time window (Streams/SQL based).
- **Exception Handling & Logging** — custom exceptions for domain errors and file logging
  of all failures via Logback.

---

## Tech Stack

| Concern        | Choice                                |
|----------------|---------------------------------------|
| Language       | Java 17                               |
| Build          | Maven (shade plugin → executable JAR) |
| Database       | PostgreSQL (JDBC)                     |
| Logging        | SLF4J + Logback (file logging)        |
| Boilerplate    | Lombok                                |

---

## Project Structure

```
src/main/java/com/campus
├── console/      Menu-driven UI (MainMenu is the entry point)
├── service/      Business logic + transaction management
├── dao/          JDBC data-access objects
├── model/        Domain models (Student, Wallet, Transaction, SplitExpense)
├── interfaces/   Functional interfaces (PaymentProcessor, TransferHandler)
├── exception/    Custom exceptions
└── util/         DBConnection, FileLogger, FraudDetector, InputValidator

src/main/resources
├── schema.sql    Canonical database schema (PostgreSQL)
└── logback.xml   Logging configuration
```

---

## Database Schema

Four tables: `students`, `wallets`, `transactions`, `dues`.

- A student has exactly one wallet. The 1:1 link is held by `wallets.student_id` (UNIQUE).
- `transactions` records every wallet/payment movement; campus payments use `receiver_id = 0`.
- `dues` holds Splitwise-style pending shares, grouped by `expense_id`.

The full DDL is in [`src/main/resources/schema.sql`](src/main/resources/schema.sql).
The app also creates these tables automatically on startup
(`DBConnection.createTables()`), so applying the schema manually is optional.

---

## Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- A running PostgreSQL instance

### 1. Configure database connection

Connection settings are read from environment variables, with a `.env` file in the
project root as a fallback. Create a `.env` file:

```env
DB_URL=jdbc:postgresql://localhost:5432/campus
DB_USER=your_user
DB_PASSWORD=your_password
```

Real environment variables take precedence over `.env` values.

### 2. (Optional) Apply the schema manually

```bash
psql -d campus -f src/main/resources/schema.sql
```

Otherwise the tables are created automatically the first time you run the app.

---

## Build & Run

### Build the executable JAR

```bash
mvn clean package
```

This produces `target/campus-payment-platform.jar` (a fat JAR with all dependencies).

### Run

```bash
java -jar target/campus-payment-platform.jar
```

You'll be greeted with the registration/login menu.

### Run with Docker

```bash
docker build -t campus-payment-platform .
docker run --rm -it \
  -e DB_URL="jdbc:postgresql://host:5432/campus" \
  -e DB_USER="user" -e DB_PASSWORD="pass" \
  campus-payment-platform
```

---

## Usage Flow

1. **Register** — enter name, phone (required, unique), optional email, and a numeric PIN.
   You receive a generated Student ID and a wallet (balance ₹0, balance cap ₹20,000).
2. **Login** — with your Student ID and PIN (3 attempts).
3. From the main menu, access **Wallet**, **Campus Payments**, **Split Expenses**,
   **Reports**, and **Student Management**.

---

## Logging

All activity and errors are logged to file via Logback (configured in
`src/main/resources/logback.xml`), with output under `logs/`.
