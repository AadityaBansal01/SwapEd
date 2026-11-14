-- ============================================================================
-- SwapEd — Campus Student Book & Gear Exchange
-- MySQL Relational Database Schema (swap_ed_db)
-- ============================================================================

CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    college_id VARCHAR(40) NOT NULL,
    department VARCHAR(120) NOT NULL,
    course VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_students_email UNIQUE (email),
    CONSTRAINT uk_students_college_id UNIQUE (college_id),
    INDEX idx_students_email (email),
    INDEX idx_students_department (department)
);

CREATE TABLE IF NOT EXISTS categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_categories_name UNIQUE (category_name)
);

CREATE TABLE IF NOT EXISTS listings (
    listing_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    course_code VARCHAR(40) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2) NULL,
    item_condition VARCHAR(30) NOT NULL,
    image_path VARCHAR(500) NULL,
    pickup_location VARCHAR(180) NOT NULL,
    listing_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_listings_category FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE RESTRICT,
    CONSTRAINT chk_listings_selling_price CHECK (selling_price >= 0),
    INDEX idx_listings_seller (seller_id),
    INDEX idx_listings_category (category_id),
    INDEX idx_listings_status (listing_status),
    INDEX idx_listings_course_code (course_code),
    INDEX idx_listings_condition (item_condition),
    INDEX idx_listings_price (selling_price),
    INDEX idx_listings_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    reservation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_listing FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_buyer FOREIGN KEY (buyer_id) REFERENCES students (student_id) ON DELETE CASCADE,
    INDEX idx_reservations_listing (listing_id),
    INDEX idx_reservations_buyer (buyer_id),
    INDEX idx_reservations_status (reservation_status)
);

CREATE TABLE IF NOT EXISTS trades (
    trade_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    trade_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trades_listing FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT fk_trades_buyer FOREIGN KEY (buyer_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_trades_seller FOREIGN KEY (seller_id) REFERENCES students (student_id) ON DELETE CASCADE,
    INDEX idx_trades_listing (listing_id),
    INDEX idx_trades_buyer (buyer_id),
    INDEX idx_trades_seller (seller_id),
    INDEX idx_trades_status (trade_status)
);

CREATE TABLE IF NOT EXISTS wishlists (
    wishlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wishlists_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlists_listing FOREIGN KEY (listing_id) REFERENCES listings (listing_id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlists_student_listing UNIQUE (student_id, listing_id),
    INDEX idx_wishlists_student (student_id),
    INDEX idx_wishlists_listing (listing_id)
);

CREATE TABLE IF NOT EXISTS email_verifications (
    verification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    CONSTRAINT fk_email_verifications_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT uk_email_verifications_token_hash UNIQUE (token_hash),
    INDEX idx_email_verifications_student (student_id),
    INDEX idx_email_verifications_token_hash (token_hash)
);

CREATE TABLE IF NOT EXISTS password_resets (
    reset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    CONSTRAINT fk_password_resets_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT uk_password_resets_token_hash UNIQUE (token_hash),
    INDEX idx_password_resets_student (student_id),
    INDEX idx_password_resets_token_hash (token_hash)
);
