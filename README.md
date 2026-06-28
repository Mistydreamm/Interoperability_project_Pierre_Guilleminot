# GymPR - Enterprise Integration & Personal Record Tracker

GymPR is a distributed enterprise application for managing personal strength records. It is structured into multiple decoupled modules, implementing REST APIs, a JavaFX desktop interface, asynchronous JMS auditing via Apache Camel, and BPMN process orchestration via Camunda.

---

## Architecture Overview

1. **Backend API (`gympr`)**: Spring Boot REST application managing data (H2), user security (JWT), and transactions.
2. **Desktop Client (`frontend`)**: JavaFX desktop GUI utilizing MVC architecture for user authentication, CRUD operations, searching by ID, and invoking database backup/restore operations.
3. **Integration Middleware (`camel`)**: Apache Camel routing system that intercepts transaction events, pushes messages to an ActiveMQ queue, and logs transaction metadata locally using AES-128 encryption.
4. **Message Broker (ActiveMQ)**: JMS server mediating messaging between Camel routes and logging queues.
5. **Business Process (`camunda`)**: Camunda BPMN engine orchestrating database operations via HTTP connectors.

---

## Prerequisites

Ensure you have the following installed on your machine:
* **Java Development Kit (JDK)**: Version 25 (e.g., Microsoft Build of OpenJDK 25.0.3)
* **Build Tool**: Apache Maven 3.9+
* **Message Broker**: Apache ActiveMQ (Classic)
* **BPMN Platform**: Camunda 7 or Camunda 8 (Self-Managed or SaaS)

---

## Step-by-Step Execution Guide

Follow these steps in order to start and run the entire ecosystem:

### Step 1: Start the ActiveMQ Server
ActiveMQ acts as the message broker for logging event payloads asynchronously.
1. Download and extract **Apache ActiveMQ Classic** on your machine.
2. Open a terminal and navigate to the ActiveMQ installation directory.
3. Run the broker startup script:
   * **Linux/macOS:** `./bin/activemq start`
   * **Windows:** `.\bin\win64\activemq.bat`
4. Verify that the ActiveMQ web console is accessible at [http://localhost:8161](http://localhost:8161) (default credentials: `admin`/`admin`).
5. Ensure the JMS broker port is listening on `tcp://localhost:61616`.

---

### Step 2: Start the Spring Boot Backend API (`gympr`)
The backend manages database access, security filters, JWT tokens, and database operations.
1. Open a terminal and navigate to the `/gympr` directory.
2. Build the project using Maven:
   ```bash
   mvn clean compile
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Verify the backend is up and running on port `8081`:
   * **REST API Swagger Documentation:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
   * **In-Memory H2 Database Console:** [http://localhost:8081/h2-console](http://localhost:8081/h2-console) (JDBC URL: `jdbc:h2:mem:gymprdb`, User: `sa`, empty password).

---

### Step 3: Run the Camunda BPM Business Process
The business workflow orchestrates record management using BPMN HTTP Connectors.
1. **Modeler Configuration:** Open the BPMN configuration file `http-json-connector.json` (or the process XML/BPMN diagram) in **Camunda Modeler** or the web-based modeller.
2. Configure the target REST endpoints for the service tasks to point to `http://localhost:8081/api/lifts/` and `http://localhost:8081/api/auth/login`.
3. **Deploy the Process:** Deploy the configured BPMN model to your running Camunda Engine (local or SaaS) via the Modeler deploy menu.
4. **Execution:** Log in to **Camunda Tasklist/Operate** to start a process instance, fill in the record details, and track the REST integration steps.

---

### Step 4: Configure and Run the Apache Camel Module
Camel processes log event messages asynchronously, routing them to the ActiveMQ broker and saving encrypted local log files.
1. Navigate to the `/camel` directory.
2. Verify in `application.properties` that the endpoint links and credentials match the running backend:
   ```properties
   gympr.api.base-url=http://localhost:8081/api/lifts
   gympr.api.login-url=http://localhost:8081/api/auth/login
   gympr.api.refresh-url=http://localhost:8081/api/auth/refresh
   gympr.admin.username=admin
   gympr.admin.password=admin123
   gympr.payload.encryption-key=MySuperSecretKey
   ```
3. Run the Camel middleware module:
   ```bash
   mvn spring-boot:run
   ```
4. Verify in the logs that the routes start successfully and hook onto ActiveMQ (`tcp://localhost:61616`).

---

### Step 5: Start the JavaFX Desktop Client (`frontend`)
The client provides a GUI dashboard for users to authenticate, query records, search by ID, and execute backups/restores.
1. Navigate to the `/frontend` directory.
2. Build and run the desktop interface using the JavaFX Maven plugin:
   ```bash
   mvn clean compile javafx:run
   ```
3. The GUI login screen will appear. Log in using one of the default user accounts:
   * **Admin User:** `admin` / `admin123` (Full CRUD, backup/restore, search by ID)
   * **Standard User:** `user` / `user123` (Read-only, search by ID)
