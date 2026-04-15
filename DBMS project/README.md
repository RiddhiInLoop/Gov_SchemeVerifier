# 🏛️ GovScheme Verifier

GovScheme Verifier is a comprehensive web-based platform allowing citizens to discover, verify eligibility for, and apply to various national and state-level government welfare schemes (such as PM-KISAN, PM Awas Yojana, MGNREGA, and more). It also features a secure administrative dashboard to review and manage applications.

---

## ✨ Features

### 👤 For Citizens
- **Scheme Discovery**: Browse an extensive catalog of active government schemes.
- **Smart Search**: Filter schemes instantly by keyword or description.
- **Automated Eligibility Checking**: An automated engine cross-references citizen demographics (Age, Income, Gender, Category, etc.) with strict scheme requirements to instantly generate eligibility confirmation.
- **Instant Application**: One-click application tracking and submission.
- **Personal Dashboard**: Track real-time status (Pending, Approved, Rejected) of all past scheme applications.

### 🛡️ For Administrators
- **Application Management**: Review incoming applications and approve or reject them with feedback reasons.
- **Scheme Administration**: Full CRUD operations to Update, Delete, or Add new government schemes directly through the dashboard.
- **System Metrics**: Visual overview of scheme statistics.

---

## 🛠️ Technology Stack

- **Frontend**: HTML5, Vanilla JavaScript (`fetch` API), CSS3, Bootstrap 5.
- **Backend API**: Pure Java 11+ (using native `com.sun.net.httpserver` - no massive frameworks!).
- **Database**: MySQL.
- **Security**: Robust PBKDF2 Password Hashing, Session Management.

---

## 🚀 Quick Start / Local Deployment

### Prerequisites
- **Java 11+** installed (`java` and `javac` in PATH).
- **MySQL Server** running locally on port `3306`.
- **MySQL Connector/J**: Add the MySQL connector `.jar` file manually to the `scheme-verifier/lib/` folder (or adjust the classpath in the execution commands).

### 1. Database Configuration
By default, the application connects to MySQL using `root` as both the username and password. If your local DB instance uses different credentials, set the environment variables in your terminal before running:
```powershell
$env:DB_USER = "your_mysql_username"
$env:DB_PASSWORD = "your_mysql_password"
```

### 2. Running the Backend Server
Open your terminal in the `scheme-verifier` directory and execute these commands:

```powershell
# Step 2a: Compile the Java Source files
javac -cp "lib\mysql-connector-j.jar;." backend/*.java

# Step 2b: Formally Reset the Database (Creates fresh schema & inserts real-world schemes)
java -cp "lib\mysql-connector-j.jar;." backend.DBReset

# Step 2c: Start the Http Server
java -cp "lib\mysql-connector-j.jar;." backend.Main
```
*The backend will now be actively listening on `http://localhost:8080/api/ping`.*

### 3. Launching the Frontend
Simply open the `index.html` file located in the root project folder directly in your favorite web browser! Our modular JavaScript architecture will automatically establish an HTTP connection with your local Java backend. 

---

## 🔐 Default Admin Credentials
To access the Admin Portal and manage schemes, use the following generated credentials:
- **Username:** `admin@gov.in`
- **Password:** `admin123`

---

## 📜 Database Schema Overview
- **`Citizen`**: Stores securely hashed user credentials and expansive demographics.
- **`Scheme`**: Holds dynamic eligibility requirements (`min_age`, `income_limit`, `gender_req`, etc.).
- **`Application`**: Relational bridge mapping citizen requests to specific schemes with review states.
- **`Admin`**: Specialized core system administrators. 

📖 **[View the complete Data Dictionary here](DATA_DICTIONARY.md)** detailing tables, primary/foreign keys, datatypes, and constraints!

*Designed and engineered as a scalable Database Management Systems (DBMS) Application Architecture.*
