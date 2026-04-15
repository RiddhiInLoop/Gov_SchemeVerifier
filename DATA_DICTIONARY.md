#  Data Dictionary
**Database Name**: `scheme_verifier_db`

This data dictionary outlines the complete architecture of the database tables used in the GovScheme Verifier project.

---

## 1. Table: `Citizen`
Stores all registered citizen demographic, login, and application details necessary to automatically check eligibility for schemes.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `citizen_id` | `INT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique identifier for each citizen |
| `first_name` | `VARCHAR(100)` | `NOT NULL` | Citizen's first name |
| `middle_name` | `VARCHAR(100)` | | Citizen's middle name (optional) |
| `last_name` | `VARCHAR(100)` | `NOT NULL` | Citizen's last name |
| `age` | `INT` | `NOT NULL` | Age in years |
| `gender` | `ENUM` | `NOT NULL` | `Male`, `Female`, or `Other` |
| `category` | `ENUM` | `NOT NULL` | Caste/Social Category: `General`, `SC`, `ST`, `OBC`, `Others` |
| `citizenship` | `VARCHAR(100)` | `NOT NULL, DEFAULT 'Indian'` | Nationality/Citizenship of the user |
| `income` | `DECIMAL(15,2)` | `NOT NULL` | Annual family income in Indian Rupees (₹) |
| `residence_type` | `ENUM` | `NOT NULL` | Type of residence: `Owned`, `Rented`, `Other` |
| `area` | `VARCHAR(200)` | `NOT NULL` | Locality or area of residence |
| `landmark` | `VARCHAR(200)` | | Nearest landmark (optional) |
| `pin_code` | `VARCHAR(20)`| `NOT NULL` | Postal code of residence |
| `email` | `VARCHAR(100)` | `UNIQUE, NOT NULL` | Login email address |
| `password` | `VARCHAR(255)` | `NOT NULL` | PBKDF2 hashed password |

---

## 2. Table: `Scheme`
Stores the metadata, descriptions, and dynamic automated eligibility thresholds for government welfare schemes.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `scheme_id` | `INT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique identifier for each scheme |
| `name` | `VARCHAR(200)` | `NOT NULL` | Official name of the government scheme |
| `description` | `TEXT` | `NOT NULL` | Detailed description and benefits of the scheme |
| `min_age` | `INT` | | Minimum age required to be eligible (Null = no limit) |
| `max_age` | `INT` | | Maximum age required to be eligible (Null = no limit) |
| `income_limit` | `DECIMAL(15,2)` | | Maximum annual income required to be eligible (Null = no limit) |
| `gender_req` | `ENUM` | `DEFAULT 'All'` | Gender restriction (`Male`, `Female`, `All`) |
| `category_req` | `ENUM` | `DEFAULT 'All'` | Caste restriction (`General`, `SC`, `ST`, `OBC`, `Others`, `All`) |
| `citizenship_req`| `VARCHAR(100)` | `DEFAULT 'Indian'` | Nationality restriction |

---

## 3. Table: `Application`
A relational bridge/transaction table that maps a `Citizen`'s application to a specific `Scheme` and tracks its progression State by an Admin.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `application_id` | `INT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique identifier for the application |
| `status` | `VARCHAR(50)` | `DEFAULT 'Pending'` | Workflow status: `Pending`, `Approved`, `Rejected` |
| `applied_date` | `DATE` | `NOT NULL` | Timestamp the citizen successfully submitted the application |
| `rejection_reason`| `TEXT` | | Custom text reason provided by the Admin if rejected (Null otherwise) |
| `citizen_id` | `INT` | `NOT NULL, FOREIGN KEY` | References `Citizen(citizen_id) ON DELETE CASCADE` |
| `scheme_id` | `INT` | `NOT NULL, FOREIGN KEY` | References `Scheme(scheme_id) ON DELETE CASCADE` |

---

## 4. Table: `Admin`
Stores the system administrators permitted to review applications and manage schema thresholds.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `admin_id` | `INT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique identifier for the administrator account |
| `username` | `VARCHAR(100)` | `UNIQUE, NOT NULL` | Login email or username (e.g., `admin@gov.in`) |
| `password` | `VARCHAR(255)` | `NOT NULL` | PBKDF2 hashed password |
