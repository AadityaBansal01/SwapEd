package com.swaped.config;

import com.swaped.entity.Category;
import com.swaped.entity.ItemCondition;
import com.swaped.entity.Listing;
import com.swaped.entity.ListingStatus;
import com.swaped.entity.Reservation;
import com.swaped.entity.ReservationStatus;
import com.swaped.entity.Student;
import com.swaped.entity.Trade;
import com.swaped.entity.TradeStatus;
import com.swaped.entity.Wishlist;
import com.swaped.repository.CategoryRepository;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.ReservationRepository;
import com.swaped.repository.StudentRepository;
import com.swaped.repository.TradeRepository;
import com.swaped.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds the MySQL database (swap_ed_db) on first startup if empty, creating
 * 6 categories, 8 verified Thapar University student accounts (each with their
 * own unique BCrypt-hashed password), 16 realistic academic listings, sample
 * reservations, completed trades, and wishlists.
 */
@Configuration
public class DataInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerConfig.class);

    @Bean
    public CommandLineRunner seedDatabaseIfEmpty(
            CategoryRepository categoryRepository,
            StudentRepository studentRepository,
            ListingRepository listingRepository,
            ReservationRepository reservationRepository,
            TradeRepository tradeRepository,
            WishlistRepository wishlistRepository,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate
    ) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (studentRepository.count() > 0 && listingRepository.count() > 0) {
                log.info("SwapEd database already initialized ({} students, {} listings).",
                        studentRepository.count(), listingRepository.count());
                return;
            }

            log.info("Seeding SwapEd MySQL database with sample Thapar University marketplace data...");

            // 1. Categories (6 required categories)
            Category catTextbooks = getOrCreateCategory(categoryRepository, "Textbooks");
            Category catLabCoats = getOrCreateCategory(categoryRepository, "Lab Coats");
            Category catDrawing = getOrCreateCategory(categoryRepository, "Drawing & Drafting Tools");
            Category catEquipment = getOrCreateCategory(categoryRepository, "Engineering Equipment");
            Category catCalculators = getOrCreateCategory(categoryRepository, "Calculators");
            Category catEssentials = getOrCreateCategory(categoryRepository, "Other Academic Essentials");

            // 2. Sample Student Accounts (8 accounts, each with a unique BCrypt password hash)
            Student rishita = studentRepository.save(new Student(
                    "Rishita Jain",
                    "rjain1_be24@thapar.edu",
                    "1024030782",
                    "Computer Science & Engineering (CSED)",
                    "B.E. Computer Engineering",
                    passwordEncoder.encode("Rishita@2026"),
                    true
            ));

            Student aaditya = studentRepository.save(new Student(
                    "Aaditya Bansal",
                    "abansal_be24@thapar.edu",
                    "1024030768",
                    "Computer Science & Engineering (CSED)",
                    "B.E. Computer Engineering",
                    passwordEncoder.encode("Aaditya@2026"),
                    true
            ));

            Student aayati = studentRepository.save(new Student(
                    "Aayati Kalra",
                    "akalra_be24@thapar.edu",
                    "1024030783",
                    "Computer Science & Engineering (CSED)",
                    "B.E. Computer Engineering",
                    passwordEncoder.encode("Aayati@2026"),
                    true
            ));

            Student arjun = studentRepository.save(new Student(
                    "Arjun Verma",
                    "averma_be23@thapar.edu",
                    "1023060114",
                    "Electronics & Communication (ECED)",
                    "B.E. Electronics & Communication",
                    passwordEncoder.encode("Arjun@2026"),
                    true
            ));

            Student mehak = studentRepository.save(new Student(
                    "Mehakpreet Kaur",
                    "mkaur_be24@thapar.edu",
                    "1024080219",
                    "Mechanical Engineering (MED)",
                    "B.E. Mechanical Engineering",
                    passwordEncoder.encode("Mehak@2026"),
                    true
            ));

            Student kabir = studentRepository.save(new Student(
                    "Kabir Malhotra",
                    "kmalhotra_be23@thapar.edu",
                    "1023030451",
                    "Computer Science & Engineering (CSED)",
                    "B.E. Computer Science & Business Systems",
                    passwordEncoder.encode("Kabir@2026"),
                    true
            ));

            Student ananya = studentRepository.save(new Student(
                    "Ananya Sharma",
                    "asharma_be25@thapar.edu",
                    "1025040192",
                    "Electrical & Instrumentation (EIED)",
                    "B.E. Electrical & Computer Engineering",
                    passwordEncoder.encode("Ananya@2026"),
                    true
            ));

            Student rohan = studentRepository.save(new Student(
                    "Rohan Gill",
                    "rgill_be23@thapar.edu",
                    "1023050308",
                    "Chemical Engineering (CHED)",
                    "B.E. Chemical Engineering",
                    passwordEncoder.encode("Rohan@2026"),
                    true
            ));

            // 3. Sample Listings (16 items across all categories, conditions, and courses)
            Listing l1 = createListing(aaditya, catTextbooks,
                    "Introduction to Algorithms (CLRS) — 3rd Edition",
                    "Classic Cormen, Leiserson, Rivest & Stein paperback edition. Crisp pages with zero pen markings. Includes sticky-note bookmarks for Graph Algorithms and Dynamic Programming.",
                    "Data Structures and Algorithms", "UCS301",
                    new BigDecimal("480.00"), new BigDecimal("995.00"),
                    ItemCondition.LIKE_NEW, "/images/products/textbook-dsa.svg",
                    "Central Library (Nava Nalanda) Entrance", ListingStatus.AVAILABLE, 1);

            Listing l2 = createListing(rishita, catTextbooks,
                    "Thomas' Calculus — 14th Edition (Pearson)",
                    "Essential first-year mathematics textbook covering multivariable calculus, partial derivatives, and multiple integrals. Well maintained with neat pencil formula annotations.",
                    "Mathematics-I", "UMA010",
                    new BigDecimal("350.00"), new BigDecimal("780.00"),
                    ItemCondition.GOOD, "/images/products/textbook-default.svg",
                    "COS Complex Café Seating", ListingStatus.AVAILABLE, 2);

            Listing l3 = createListing(aayati, catCalculators,
                    "Casio fx-991CW ClassWiz Non-Programmable Scientific Calculator",
                    "Approved for all Thapar mid-semester and end-semester examinations. High-resolution 4-gradation display, matrix/vector solver, and solar+battery backup. Includes hard slide cover.",
                    "Numerical Analysis", "UMA011",
                    new BigDecimal("850.00"), new BigDecimal("1395.00"),
                    ItemCondition.LIKE_NEW, "/images/products/calculator-default.svg",
                    "LT-101 Foyer (Academic Block)", ListingStatus.AVAILABLE, 3);

            Listing l4 = createListing(mehak, catDrawing,
                    "Omega Mini Drafter + Expandable Drawing Sheet Holder Tube",
                    "Complete first-year Engineering Drawing kit. Rust-free steel arm mini drafter with smooth protractor knob, 2 set squares, sheet clips, and waterproof cylindrical sheet tube.",
                    "Engineering Drawing", "UTA015",
                    new BigDecimal("420.00"), new BigDecimal("850.00"),
                    ItemCondition.GOOD, "/images/products/drafting-default.svg",
                    "Workshop Block Entrance", ListingStatus.AVAILABLE, 4);

            Listing l5 = createListing(rohan, catLabCoats,
                    "100% Cotton White Lab Coat (Size L — Unisex)",
                    "Thick, breathable cotton laboratory apron required for Applied Chemistry and Chemical Engineering labs. Freshly dry-cleaned, no chemical stains, all front snap buttons intact.",
                    "Applied Chemistry", "UCH101",
                    new BigDecimal("220.00"), new BigDecimal("450.00"),
                    ItemCondition.LIKE_NEW, "/images/products/labcoat-default.svg",
                    "G-Block Chemistry Lab Corridor", ListingStatus.AVAILABLE, 5);

            Listing l6 = createListing(arjun, catEquipment,
                    "Arduino Uno R3 SMD Board + 830-Point Breadboard & Jumper Kit",
                    "Complete microcontroller prototyping bundle for Engineering Design Project (Buggy). Includes USB cable, ultrasonic sensor HC-SR04, IR line sensors, and L298N motor driver module.",
                    "Engineering Design Project-I", "UTA018",
                    new BigDecimal("550.00"), new BigDecimal("1150.00"),
                    ItemCondition.GOOD, "/images/products/equipment-default.svg",
                    "TAN Building Ground Floor Lobby", ListingStatus.AVAILABLE, 6);

            Listing l7 = createListing(kabir, catTextbooks,
                    "Operating System Concepts (Silberschatz / Galvin) — 10th Edition",
                    "Dinosaur book for OS course. Covers process synchronization, deadlocks, virtual memory, and Linux kernel case studies. Spine and cover in near-mint condition.",
                    "Operating Systems", "UCS303",
                    new BigDecimal("440.00"), new BigDecimal("890.00"),
                    ItemCondition.LIKE_NEW, "/images/products/textbook-dsa.svg",
                    "CSED Department Atrium", ListingStatus.AVAILABLE, 7);

            Listing l8 = createListing(ananya, catLabCoats,
                    "White Chemistry & Physics Lab Coat (Size M)",
                    "Standard medium-size lab coat used for one semester in Applied Physics Lab. Clean cuffs and collar with breast pocket for pen and small lab notebook.",
                    "Applied Physics", "UPH004",
                    new BigDecimal("190.00"), new BigDecimal("420.00"),
                    ItemCondition.GOOD, "/images/products/labcoat-default.svg",
                    "Hostel I Visitor Gate", ListingStatus.AVAILABLE, 8);

            Listing l9 = createListing(arjun, catEquipment,
                    "Mastech MS830L Digital Multimeter with Continuity Buzzer",
                    "Digital multimeter with backlit LCD, DC/AC voltage, current, resistance, diode check, and transistor hFE socket. Comes with fresh 9V battery and insulated test probes.",
                    "Basic Electronics", "UEC101",
                    new BigDecimal("380.00"), new BigDecimal("720.00"),
                    ItemCondition.NEW, "/images/products/equipment-default.svg",
                    "B-Block Electronics Lab Entrance", ListingStatus.AVAILABLE, 9);

            Listing l10 = createListing(rishita, catTextbooks,
                    "Database System Concepts (Korth, Sudarshan) — 7th Edition",
                    "Comprehensive DBMS reference covering ER modeling, SQL, normalization (3NF/BCNF), indexing, and transaction concurrency control. Includes solved tutorial sheet bundle.",
                    "Database Management Systems", "UCS310",
                    new BigDecimal("390.00"), new BigDecimal("825.00"),
                    ItemCondition.GOOD, "/images/products/textbook-default.svg",
                    "Central Library Reading Hall Steps", ListingStatus.AVAILABLE, 10);

            Listing l11 = createListing(aaditya, catEssentials,
                    "UCS503 Software Engineering Printed Notes + UML Reference Spiral",
                    "Laser-printed lecture deck compilation, IEEE SRS templates, Use-Case & Sequence diagram workbook, and past 4 years' solved mid/end-sem question papers.",
                    "Software Engineering", "UCS503",
                    new BigDecimal("160.00"), new BigDecimal("340.00"),
                    ItemCondition.LIKE_NEW, "/images/products/essentials-default.svg",
                    "COS Complex Stationary Shop Front", ListingStatus.AVAILABLE, 11);

            Listing l12 = createListing(aayati, catDrawing,
                    "Faber-Castell Precision Compass Set + Roller Scale & French Curves",
                    "German-engineered quick-adjustment bow compass, 30cm roller parallel scale, set of 3 acrylic French curves, and Staedtler Mars Plastic eraser.",
                    "Engineering Drawing", "UTA015",
                    new BigDecimal("260.00"), new BigDecimal("520.00"),
                    ItemCondition.NEW, "/images/products/drafting-default.svg",
                    "Main Auditorium Steps", ListingStatus.AVAILABLE, 12);

            Listing l13 = createListing(mehak, catTextbooks,
                    "Engineering Mechanics: Statics & Dynamics (Hibbeler) — 14th Ed",
                    "Standard textbook for first-year Mechanics. Contains worked free-body diagrams and truss analysis problems. Slight cover crease, interior pages clean.",
                    "Engineering Mechanics", "UES103",
                    new BigDecimal("310.00"), new BigDecimal("760.00"),
                    ItemCondition.FAIR, "/images/products/textbook-default.svg",
                    "Mechanical Block Foyer", ListingStatus.RESERVED, 13);

            Listing l14 = createListing(kabir, catCalculators,
                    "Casio fx-991EX ClassWiz Scientific Calculator (552 Functions)",
                    "High-speed spreadsheet and 4x4 matrix calculator. Very popular for Linear Algebra and Numerical Methods exams.",
                    "Linear Algebra and Probability", "UMA004",
                    new BigDecimal("780.00"), new BigDecimal("1295.00"),
                    ItemCondition.GOOD, "/images/products/calculator-default.svg",
                    "Hostel J Main Gate", ListingStatus.SOLD, 16);

            Listing l15 = createListing(rohan, catTextbooks,
                    "Fundamentals of Engineering Thermodynamics (Moran & Shapiro)",
                    "Complete textbook for 2nd-year Thermodynamics. Includes steam tables booklet and psychometric chart insert.",
                    "Thermodynamics", "UME301",
                    new BigDecimal("410.00"), new BigDecimal("880.00"),
                    ItemCondition.GOOD, "/images/products/textbook-default.svg",
                    "F-Block Lawn", ListingStatus.SOLD, 18);

            Listing l16 = createListing(ananya, catEssentials,
                    "Workshop Safety Goggles + Digital Vernier Caliper Case",
                    "Anti-fog polycarbonate safety glasses and protective storage box for Manufacturing Processes lab sessions.",
                    "Manufacturing Processes", "UTA026",
                    new BigDecimal("240.00"), new BigDecimal("490.00"),
                    ItemCondition.LIKE_NEW, "/images/products/essentials-default.svg",
                    "Central Workshop Gate", ListingStatus.SOLD, 20);

            listingRepository.save(l1);
            listingRepository.save(l2);
            listingRepository.save(l3);
            listingRepository.save(l4);
            listingRepository.save(l5);
            listingRepository.save(l6);
            listingRepository.save(l7);
            listingRepository.save(l8);
            listingRepository.save(l9);
            listingRepository.save(l10);
            listingRepository.save(l11);
            listingRepository.save(l12);
            listingRepository.save(l13);
            listingRepository.save(l14);
            listingRepository.save(l15);
            listingRepository.save(l16);

            // 4. Sample Reservations (Pending, Accepted, and Completed)
            // Pending request on Rishita's Calculus book (l2) from Ananya
            Reservation r1 = new Reservation(l2, ananya, ReservationStatus.PENDING);
            reservationRepository.save(r1);

            // Pending request on Aaditya's CLRS book (l1) from Kabir
            Reservation r2 = new Reservation(l1, kabir, ReservationStatus.PENDING);
            reservationRepository.save(r2);

            // Accepted reservation on Mehak's Mechanics book (l13) for Rishita
            Reservation r3 = new Reservation(l13, rishita, ReservationStatus.ACCEPTED);
            reservationRepository.save(r3);

            // Completed reservations + Trade records for l14, l15, l16
            Reservation r4 = new Reservation(l14, rishita, ReservationStatus.COMPLETED);
            reservationRepository.save(r4);
            Trade t1 = new Trade(l14, rishita, kabir, l14.getSellingPrice(), TradeStatus.COMPLETED);
            t1.setCompletedAt(LocalDateTime.now().minusDays(6));
            tradeRepository.save(t1);

            Reservation r5 = new Reservation(l15, aaditya, ReservationStatus.COMPLETED);
            reservationRepository.save(r5);
            Trade t2 = new Trade(l15, aaditya, rohan, l15.getSellingPrice(), TradeStatus.COMPLETED);
            t2.setCompletedAt(LocalDateTime.now().minusDays(4));
            tradeRepository.save(t2);

            Reservation r6 = new Reservation(l16, aayati, ReservationStatus.COMPLETED);
            reservationRepository.save(r6);
            Trade t3 = new Trade(l16, aayati, ananya, l16.getSellingPrice(), TradeStatus.COMPLETED);
            t3.setCompletedAt(LocalDateTime.now().minusDays(2));
            tradeRepository.save(t3);

            // 5. Sample Wishlists
            wishlistRepository.save(new Wishlist(rishita, l1));
            wishlistRepository.save(new Wishlist(rishita, l3));
            wishlistRepository.save(new Wishlist(aaditya, l6));
            wishlistRepository.save(new Wishlist(aayati, l7));

            log.info("SwapEd sample data seeded: 8 students, 6 categories, 16 listings, 6 reservations, 3 completed trades.");
        });
    }

    private Category getOrCreateCategory(CategoryRepository repo, String name) {
        return repo.findByCategoryNameIgnoreCase(name)
                .orElseGet(() -> repo.save(new Category(name)));
    }

    private Listing createListing(
            Student seller,
            Category category,
            String title,
            String description,
            String courseName,
            String courseCode,
            BigDecimal sellingPrice,
            BigDecimal originalPrice,
            ItemCondition condition,
            String imagePath,
            String pickupLocation,
            ListingStatus status,
            int daysAgo
    ) {
        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle(title);
        listing.setDescription(description);
        listing.setCourseName(courseName);
        listing.setCourseCode(courseCode);
        listing.setSellingPrice(sellingPrice);
        listing.setOriginalPrice(originalPrice);
        listing.setItemCondition(condition);
        listing.setImagePath(imagePath);
        listing.setPickupLocation(pickupLocation);
        listing.setListingStatus(status);
        listing.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        listing.setUpdatedAt(LocalDateTime.now().minusDays(daysAgo));
        return listing;
    }
}
