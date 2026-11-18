# SwapEd — Campus Student Book & Gear Exchange

**Your Campus. Your Marketplace.**

🌐 **Live Production Website**: [https://swaped-campus-marketplace.onrender.com/](https://swaped-campus-marketplace.onrender.com/)  
📄 **UCS553 Project Report (PDF)**: [SwapEd_UCS553_Project_Report.pdf](SwapEd_UCS553_Project_Report.pdf)

SwapEd is a full-stack, student-exclusive campus marketplace built for **Thapar Institute of Engineering & Technology (TIET)** students to buy, sell, and exchange second-hand textbooks, lab coats, drawing & drafting tools, calculators, engineering equipment, and academic essentials.

---

## Project Team — BrewCoders (Group 3C54)

| Team Member | Roll No | Official Thapar Email | Role & Focus |
| :--- | :--- | :--- | :--- |
| **Aaditya Bansal** | `1024030768` | `abansal_be24@thapar.edu` | Full-Stack Architecture, UI/UX & Spring MVC |
| **Aayati Kalra** | `1024030783` | `akalra_be24@thapar.edu` | Relational Database Design (MySQL), JPA & OOP Domain Model |
| **Rishita Jain** | `1024030782` | `rjain1_be24@thapar.edu` | Spring Security, Reservation/Trade Business Logic & Integration Testing |

---

## 1. Technology Stack

- **Backend**: Java 21+, Spring Boot 3.4, Spring MVC, Spring Security 6, Spring Data JPA, Hibernate, Jakarta Bean Validation, Maven
- **Database**: MySQL (`swap_ed_db`) with relational foreign keys, composite unique constraints, pessimistic row locking (`SELECT ... FOR UPDATE`), and secondary indexes
- **Frontend**: HTML5, CSS3, JavaScript (ES6+), Bootstrap 5.3, Bootstrap Icons, Thymeleaf (`thymeleaf-extras-springsecurity6`)
- **Security & Utilities**:
  - Salted `BCryptPasswordEncoder` password hashing
  - Single-use SHA-256 hashed email verification & password-reset tokens
  - `JavaMailSender` SMTP integration via environment variables
  - Session-based authentication with CSRF protection & Remember-Me cookies
  - Multipart file uploads (`uploads/`) with MIME-type and file-size validation

---

## 2. Database Architecture (`swap_ed_db`)

Defined in [`src/main/resources/schema.sql`](src/main/resources/schema.sql):

1. **`students`** — Verified `@thapar.edu` student accounts (`student_id`, `full_name`, `email`, `college_id`, `department`, `course`, `password_hash`, `email_verified`, `created_at`)
2. **`categories`** — 6 academic item categories (`category_id`, `category_name`)
3. **`listings`** — Marketplace items with course codes and condition ratings (`listing_id`, `seller_id`, `category_id`, `title`, `description`, `course_name`, `course_code`, `selling_price`, `original_price`, `item_condition`, `image_path`, `pickup_location`, `listing_status`, `created_at`, `updated_at`)
4. **`reservations`** — Buyer pickup reservation requests (`reservation_id`, `listing_id`, `buyer_id`, `reservation_status`, `requested_at`, `updated_at`)
5. **`trades`** — Completed campus handover records (`trade_id`, `listing_id`, `buyer_id`, `seller_id`, `final_price`, `trade_status`, `completed_at`)
6. **`wishlists`** — Saved items per student (`wishlist_id`, `student_id`, `listing_id`, `created_at`) with composite unique constraint `(student_id, listing_id)`
7. **`email_verifications`** — Single-use, 24-hour email verification tokens (`verification_id`, `student_id`, `token_hash`, `expires_at`, `used_at`)
8. **`password_resets`** — Single-use, 30-minute password reset tokens (`reset_id`, `student_id`, `token_hash`, `expires_at`, `used_at`)

---

## 3. Sample Student Accounts (Individual Passwords)

All demo passwords are stored as salted **BCrypt hashes** in MySQL. Each student has their own unique password:

| Student Name | Thapar Email | Student ID | Department | Password |
| :--- | :--- | :--- | :--- | :--- |
| **Rishita Jain** | `rjain1_be24@thapar.edu` | `1024030782` | CSED | `Rishita@2026` |
| **Aaditya Bansal** | `abansal_be24@thapar.edu` | `1024030768` | CSED | `Aaditya@2026` |
| **Aayati Kalra** | `akalra_be24@thapar.edu` | `1024030783` | CSED | `Aayati@2026` |
| **Arjun Verma** | `averma_be23@thapar.edu` | `1023060114` | ECED | `Arjun@2026` |
| **Mehakpreet Kaur** | `mkaur_be24@thapar.edu` | `1024080219` | MED | `Mehak@2026` |
| **Kabir Malhotra** | `kmalhotra_be23@thapar.edu` | `1023030451` | CSED | `Kabir@2026` |
| **Ananya Sharma** | `asharma_be25@thapar.edu` | `1025040192` | EIED | `Ananya@2026` |
| **Rohan Gill** | `rgill_be23@thapar.edu` | `1023050308` | CHED | `Rohan@2026` |

---

## 4. Running the Application Locally

### Option A: One-Command Startup Script
```bash
./run.sh
```
Then open **http://localhost:8080** in your browser.

### Option B: Standard Maven Command
1. Ensure MySQL is running and configure your database credentials via environment variables (see [`.env.example`](.env.example)):
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/swap_ed_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   export DB_USERNAME="root"
   export DB_PASSWORD="your_mysql_password"
   ```
2. (Optional) Configure live SMTP email delivery for verification and password-reset emails:
   ```bash
   export MAIL_HOST="smtp.gmail.com"
   export MAIL_PORT="587"
   export MAIL_USERNAME="your_email@gmail.com"
   export MAIL_PASSWORD="your_16_char_app_password"
   ```
   *Note: When `MAIL_USERNAME` and `MAIL_PASSWORD` are not set, SwapEd clearly states that SMTP is unconfigured and displays the generated single-use verification/reset link on screen and in the server console for local evaluation.*
3. Run the Spring Boot server:
   ```bash
   ./mvnw spring-boot:run
   ```

### Running the Automated Integration Test Suite
```bash
./mvnw test
```
