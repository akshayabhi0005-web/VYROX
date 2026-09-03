package com.veltrion.vyrox.seeder;

import com.veltrion.vyrox.model.*;
import com.veltrion.vyrox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductSpecificationRepository specificationRepository;
    private final ProductReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final CoinWalletRepository coinWalletRepository;
    private final CoinTransactionRepository coinTransactionRepository;
    private final DarkstoreRepository darkstoreRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TrackingLogRepository trackingLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already seeded
        }

        System.out.println(">>> SEEDING VYROX SMART COMMERCE PLATFORM DATABASE (Team VELTRION) <<<");

        // 1. Seed Users
        User customer = User.builder()
                .fullName("Akshay N")
                .email("customer@vyrox.com")
                .mobile("9876543210")
                .password(passwordEncoder.encode("Customer@123"))
                .roles(Set.of(Role.ROLE_CUSTOMER))
                .emailVerified(true)
                .mobileVerified(true)
                .active(true)
                .build();
        customer = userRepository.save(customer);

        User seller = User.builder()
                .fullName("Veltrion Technologies")
                .email("seller@vyrox.com")
                .mobile("9876543211")
                .password(passwordEncoder.encode("Seller@123"))
                .roles(Set.of(Role.ROLE_SELLER, Role.ROLE_CUSTOMER))
                .emailVerified(true)
                .mobileVerified(true)
                .active(true)
                .build();
        seller = userRepository.save(seller);

        User delivery = User.builder()
                .fullName("Ramesh Kumar")
                .email("rider@vyrox.com")
                .mobile("9876543212")
                .password(passwordEncoder.encode("Rider@123"))
                .roles(Set.of(Role.ROLE_DELIVERY))
                .emailVerified(true)
                .mobileVerified(true)
                .active(true)
                .build();
        delivery = userRepository.save(delivery);

        User admin = User.builder()
                .fullName("Admin VELTRION")
                .email("admin@vyrox.com")
                .mobile("9876543213")
                .password(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_CUSTOMER))
                .emailVerified(true)
                .mobileVerified(true)
                .active(true)
                .build();
        admin = userRepository.save(admin);

        // 2. Seed Wallets
        CoinWallet customerWallet = CoinWallet.builder()
                .user(customer)
                .balance(350)
                .lifetimeEarned(350)
                .lifetimeSpent(0)
                .build();
        customerWallet = coinWalletRepository.save(customerWallet);

        coinTransactionRepository.save(CoinTransaction.builder()
                .wallet(customerWallet)
                .type(CoinTransactionType.EARNED_REWARD)
                .amount(100)
                .description("Welcome reward on joining VYROX")
                .build());
        coinTransactionRepository.save(CoinTransaction.builder()
                .wallet(customerWallet)
                .type(CoinTransactionType.EARNED_SPIN_WIN)
                .amount(250)
                .description("Won from Daily Spin & Win lucky wheel")
                .build());

        // 3. Seed Addresses
        Address homeAddr = Address.builder()
                .user(customer)
                .name("Akshay N")
                .mobile("9876543210")
                .street("Flat 402, Skyline Heights, 12th Main")
                .locality("Indiranagar")
                .city("Bengaluru")
                .state("Karnataka")
                .pincode("560038")
                .landmark("Near Metro Station")
                .addressType("HOME")
                .isDefault(true)
                .latitude(12.9784)
                .longitude(77.6408)
                .build();
        addressRepository.save(homeAddr);

        // 4. Seed Darkstores and Delivery Partner
        Darkstore ds1 = Darkstore.builder()
                .name("VYROX Darkstore #101 - Indiranagar Central")
                .code("DS-BLR-01")
                .address("100 Feet Road, HAL 2nd Stage")
                .city("Bengaluru")
                .pincode("560038")
                .latitude(12.9716)
                .longitude(77.6412)
                .active(true)
                .serviceRadiusKm(8)
                .build();
        darkstoreRepository.save(ds1);

        DeliveryPartner riderPartner = DeliveryPartner.builder()
                .user(delivery)
                .vehicleNumber("KA-01-VY-4098")
                .vehicleType("Ather 450X EV Scooter")
                .phone("+91 98765 43212")
                .currentLatitude(12.9740)
                .currentLongitude(77.6380)
                .isAvailable(true)
                .rating(4.92)
                .completedDeliveries(348)
                .build();
        deliveryPartnerRepository.save(riderPartner);

        // 5. Seed Brands
        Brand apple = brandRepository.save(Brand.builder().name("Apple").slug("apple").description("Designed by Apple in California").build());
        Brand samsung = brandRepository.save(Brand.builder().name("Samsung").slug("samsung").description("Inspire the World, Create the Future").build());
        Brand sony = brandRepository.save(Brand.builder().name("Sony").slug("sony").description("Be Moved - Premium Audio & Visuals").build());
        Brand dell = brandRepository.save(Brand.builder().name("Dell").slug("dell").description("Powering Innovation and Performance").build());
        Brand nike = brandRepository.save(Brand.builder().name("Nike").slug("nike").description("Just Do It - Sportswear & Athletics").build());
        Brand philips = brandRepository.save(Brand.builder().name("Philips").slug("philips").description("Innovation and You").build());
        Brand vyroxChoice = brandRepository.save(Brand.builder().name("VYROX Select").slug("vyrox-select").description("Curated Smart Living Products").build());

        // 6. Seed Categories
        Category catMobiles = categoryRepository.save(Category.builder().name("Mobiles").slug("mobiles").iconUrl("Smartphone").featured(true).displayOrder(1).build());
        Category catElectronics = categoryRepository.save(Category.builder().name("Electronics").slug("electronics").iconUrl("Laptop").featured(true).displayOrder(2).build());
        Category catFashion = categoryRepository.save(Category.builder().name("Fashion").slug("fashion").iconUrl("Shirt").featured(true).displayOrder(3).build());
        Category catHome = categoryRepository.save(Category.builder().name("Home & Living").slug("home-living").iconUrl("Home").featured(true).displayOrder(4).build());
        Category catAppliances = categoryRepository.save(Category.builder().name("Appliances").slug("appliances").iconUrl("Tv").featured(true).displayOrder(5).build());
        Category catBeauty = categoryRepository.save(Category.builder().name("Beauty").slug("beauty").iconUrl("Sparkles").featured(true).displayOrder(6).build());
        Category catSports = categoryRepository.save(Category.builder().name("Sports").slug("sports").iconUrl("Activity").featured(true).displayOrder(7).build());
        Category catQuick = categoryRepository.save(Category.builder().name("Quick Commerce (15-Min)").slug("quick-commerce").iconUrl("Zap").featured(true).displayOrder(8).build());

        // 7. Seed Products with Specs
        // Product 1: MacBook Pro M3
        Product p1 = Product.builder()
                .title("Apple MacBook Pro 16\" (M3 Pro Chip, 18GB RAM, 512GB SSD, Space Black)")
                .sku("APP-MBP16-M3P")
                .description("The 16-inch MacBook Pro blasts forward with M3 Pro, an incredibly advanced chip that brings massive performance and capabilities for extreme workflows.")
                .category(catElectronics)
                .brand(apple)
                .mrp(BigDecimal.valueOf(249900))
                .sellingPrice(BigDecimal.valueOf(219900))
                .stockQuantity(24)
                .inStock(true)
                .averageRating(4.9)
                .reviewCount(1840)
                .mainImageUrl("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80")
                .images(List.of(
                        "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&auto=format&fit=crop&q=80",
                        "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=800&auto=format&fit=crop&q=80"
                ))
                .highlights(List.of("Apple M3 Pro 12-core CPU, 18-core GPU", "16.2-inch Liquid Retina XDR Display (120Hz ProMotion)", "Up to 22 Hours Battery Life", "18GB Unified Memory | 512GB High-Speed SSD"))
                .bankOffers(List.of("₹5,000 Instant Discount on HDFC/ICICI Credit Cards", "No Cost EMI starting from ₹18,325/month", "Up to ₹22,000 off on Exchange"))
                .sellerName("Apple Authorized Premium Store")
                .sellerRating(4.9)
                .warrantyInfo("1 Year Apple Worldwide Limited Warranty + AppleCare eligible")
                .isTopDeal(true)
                .isFeatured(true)
                .isTrending(true)
                .isBestSeller(true)
                .estimatedDeliveryDays("Tomorrow by 11 AM")
                .freeDelivery(true)
                .build();
        p1 = productRepository.save(p1);

        saveSpecs(p1, List.of(
                new String[]{"Performance", "Processor", "Apple M3 Pro (12-core)"},
                new String[]{"Performance", "RAM", "18 GB Unified Memory"},
                new String[]{"Storage", "Storage Capacity", "512 GB SSD"},
                new String[]{"Display", "Display Size", "16.2 Inch Liquid Retina XDR"},
                new String[]{"Display", "Refresh Rate", "120 Hz ProMotion"},
                new String[]{"Battery", "Battery Life", "Up to 22 Hours"},
                new String[]{"Operating System", "OS", "macOS Sonoma"}
        ));

        // Product 2: Dell XPS 16
        Product p2 = Product.builder()
                .title("Dell XPS 16 Laptop (Intel Core Ultra 9 185H, 32GB DDR5, 1TB NVMe, RTX 4070)")
                .sku("DELL-XPS16-U9")
                .description("Experience desktop-class performance in a breathtaking CNC aluminum design with 4K OLED touch display.")
                .category(catElectronics)
                .brand(dell)
                .mrp(BigDecimal.valueOf(279990))
                .sellingPrice(BigDecimal.valueOf(234990))
                .stockQuantity(15)
                .inStock(true)
                .averageRating(4.7)
                .reviewCount(640)
                .mainImageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&auto=format&fit=crop&q=80")
                .images(List.of("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=800&auto=format&fit=crop&q=80"))
                .highlights(List.of("Intel Core Ultra 9 185H with Dedicated AI NPU", "NVIDIA GeForce RTX 4070 8GB GDDR6", "16.3-inch 4K+ OLED InfinityEdge Touch Display", "32GB LPDDR5X RAM | 1TB NVMe Gen4 SSD"))
                .bankOffers(List.of("₹4,000 Instant Discount with SBI Cards", "No Cost EMI available"))
                .sellerName("Dell Official Online")
                .sellerRating(4.8)
                .warrantyInfo("2 Years Dell On-Site Premium Support")
                .isTopDeal(true)
                .isTrending(true)
                .estimatedDeliveryDays("2 Days")
                .freeDelivery(true)
                .build();
        p2 = productRepository.save(p2);

        saveSpecs(p2, List.of(
                new String[]{"Performance", "Processor", "Intel Core Ultra 9 185H (16-core)"},
                new String[]{"Performance", "RAM", "32 GB LPDDR5X"},
                new String[]{"Storage", "Storage Capacity", "1 TB NVMe SSD"},
                new String[]{"Display", "Display Size", "16.3 Inch 4K+ OLED Touch"},
                new String[]{"Display", "Refresh Rate", "120 Hz"},
                new String[]{"Battery", "Battery Life", "Up to 14 Hours"},
                new String[]{"Operating System", "OS", "Windows 11 Pro"}
        ));

        // Product 3: Samsung Galaxy S24 Ultra
        Product p3 = Product.builder()
                .title("Samsung Galaxy S24 Ultra 5G (Titanium Gray, 12GB RAM, 256GB Storage, Galaxy AI)")
                .sku("SAM-S24U-256")
                .description("Welcome to the era of mobile AI. With Galaxy S24 Ultra in your hands, you can unleash whole new levels of creativity, productivity and possibility.")
                .category(catMobiles)
                .brand(samsung)
                .mrp(BigDecimal.valueOf(134999))
                .sellingPrice(BigDecimal.valueOf(119999))
                .stockQuantity(40)
                .inStock(true)
                .averageRating(4.8)
                .reviewCount(5420)
                .mainImageUrl("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800&auto=format&fit=crop&q=80")
                .images(List.of("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=800&auto=format&fit=crop&q=80"))
                .highlights(List.of("Galaxy AI: Circle to Search, Live Translate, Note Assist", "Titanium Frame with Gorilla Armor Glass", "200MP Quad Telephoto Camera with 100x Space Zoom", "Snapdragon 8 Gen 3 for Galaxy (4nm)"))
                .bankOffers(List.of("₹6,000 HDFC Bank Card Instant Cashback", "₹10,000 Exchange Bonus"))
                .sellerName("Samsung India Retail")
                .sellerRating(4.9)
                .warrantyInfo("1 Year Manufacturer Warranty for Device and 6 Months for In-Box Accessories")
                .isTopDeal(true)
                .isBestSeller(true)
                .estimatedDeliveryDays("Tomorrow by 11 AM")
                .freeDelivery(true)
                .build();
        p3 = productRepository.save(p3);

        saveSpecs(p3, List.of(
                new String[]{"Performance", "Processor", "Snapdragon 8 Gen 3 for Galaxy"},
                new String[]{"Performance", "RAM", "12 GB LPDDR5X"},
                new String[]{"Storage", "Storage Capacity", "256 GB UFS 4.0"},
                new String[]{"Display", "Display Size", "6.8 Inch Dynamic AMOLED 2X (2600 nits)"},
                new String[]{"Display", "Refresh Rate", "1-120 Hz Adaptive"},
                new String[]{"Camera", "Rear Camera", "200MP + 50MP + 12MP + 10MP"},
                new String[]{"Camera", "Front Camera", "12 MP Dual Pixel"},
                new String[]{"Battery", "Battery Capacity", "5000 mAh (45W Fast Charging)"}
        ));

        // Product 4: iPhone 15 Pro Max
        Product p4 = Product.builder()
                .title("Apple iPhone 15 Pro Max (256 GB, Natural Titanium, A17 Pro Chip)")
                .sku("APP-IP15PM-256")
                .description("Forged in titanium and featuring the groundbreaking A17 Pro chip, a customizable Action button, and the most powerful iPhone camera system ever.")
                .category(catMobiles)
                .brand(apple)
                .mrp(BigDecimal.valueOf(159900))
                .sellingPrice(BigDecimal.valueOf(148900))
                .stockQuantity(30)
                .inStock(true)
                .averageRating(4.9)
                .reviewCount(8920)
                .mainImageUrl("https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&auto=format&fit=crop&q=80")
                .images(List.of("https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&auto=format&fit=crop&q=80"))
                .highlights(List.of("A17 Pro Chip with 6-core GPU and Ray Tracing", "Strong and light aerospace-grade titanium design", "48MP Main Camera with 5x Telephoto Optical Zoom", "Action button & USB-C 3.0 with 10Gbps speeds"))
                .bankOffers(List.of("₹4,000 Instant Discount on ICICI/HDFC Cards", "No Cost EMI starting ₹12,408/mo"))
                .sellerName("Apple India Official")
                .sellerRating(4.9)
                .warrantyInfo("1 Year Apple Limited Warranty")
                .isTopDeal(true)
                .isTrending(true)
                .estimatedDeliveryDays("Tomorrow by 11 AM")
                .freeDelivery(true)
                .build();
        p4 = productRepository.save(p4);

        saveSpecs(p4, List.of(
                new String[]{"Performance", "Processor", "Apple A17 Pro (3nm)"},
                new String[]{"Performance", "RAM", "8 GB Unified"},
                new String[]{"Storage", "Storage Capacity", "256 GB NVMe"},
                new String[]{"Display", "Display Size", "6.7 Inch Super Retina XDR OLED"},
                new String[]{"Display", "Refresh Rate", "120 Hz ProMotion Always-On"},
                new String[]{"Camera", "Rear Camera", "48MP + 12MP (5x Telephoto) + 12MP Ultra-Wide"},
                new String[]{"Camera", "Front Camera", "12 MP TrueDepth"},
                new String[]{"Battery", "Battery Capacity", "4422 mAh (MagSafe Wireless)"}
        ));

        // Product 5: Sony WH-1000XM5 ANC Headphones
        Product p5 = Product.builder()
                .title("Sony WH-1000XM5 Wireless Noise Cancelling Over-Ear Headphones (Silver)")
                .sku("SNY-WH1000XM5-SLV")
                .description("Industry-leading noise canceling with two processors and 8 microphones for unprecedented call quality and magnificent acoustics.")
                .category(catElectronics)
                .brand(sony)
                .mrp(BigDecimal.valueOf(34990))
                .sellingPrice(BigDecimal.valueOf(26990))
                .stockQuantity(55)
                .inStock(true)
                .averageRating(4.8)
                .reviewCount(3450)
                .mainImageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80")
                .highlights(List.of("Industry Leading Noise Cancellation with 8 Mics", "Up to 30 Hours Battery Life (3 min charge = 3 hours)", "Ultra-comfortable, lightweight soft fit leather design", "Speak-to-Chat & Multipoint Bluetooth Connection"))
                .bankOffers(List.of("₹2,000 Instant Discount on Credit Cards"))
                .sellerName("Sony Audio India")
                .sellerRating(4.9)
                .warrantyInfo("1 Year Brand Warranty")
                .isTopDeal(true)
                .isBestSeller(true)
                .estimatedDeliveryDays("Tomorrow by 11 AM")
                .freeDelivery(true)
                .build();
        p5 = productRepository.save(p5);

        // Product 6: Nike Air Max 270
        Product p6 = Product.builder()
                .title("Nike Air Max 270 Men's Running & Lifestyle Sneakers (Black/White)")
                .sku("NKE-AM270-BW")
                .description("Nike's first lifestyle Air unit delivers energy with every step. Boasts Nike's biggest heel Air unit yet for a super-soft ride.")
                .category(catFashion)
                .brand(nike)
                .mrp(BigDecimal.valueOf(13995))
                .sellingPrice(BigDecimal.valueOf(9790))
                .stockQuantity(80)
                .inStock(true)
                .averageRating(4.6)
                .reviewCount(1920)
                .mainImageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80")
                .highlights(List.of("Max Air 270 unit delivers unrivaled comfort", "Engineered mesh in the upper provides breathability", "Dual-density foam sole cushions every stride", "Rubber outsole for durable traction"))
                .sellerName("Nike Brand Authorized Store")
                .sellerRating(4.7)
                .warrantyInfo("3 Months Manufacturer Warranty against manufacturing defects")
                .isTopDeal(true)
                .estimatedDeliveryDays("Tomorrow")
                .freeDelivery(true)
                .build();
        p6 = productRepository.save(p6);

        // Product 7: Philips Air Fryer XXL
        Product p7 = Product.builder()
                .title("Philips XXL Digital Airfryer (7.2L Capacity, Fat Removal Technology, 2000W)")
                .sku("PHL-AFXXL-72")
                .description("Cook with little to no oil. The Rapid Air technology swirls hot air to create delicious foods that are crispy on the outside and tender on the inside.")
                .category(catAppliances)
                .brand(philips)
                .mrp(BigDecimal.valueOf(18995))
                .sellingPrice(BigDecimal.valueOf(11990))
                .stockQuantity(35)
                .inStock(true)
                .averageRating(4.7)
                .reviewCount(4210)
                .mainImageUrl("https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=800&auto=format&fit=crop&q=80")
                .highlights(List.of("7.2L XXL Basket (Feeds up to 6 people)", "Fat Removal technology captures excess fat", "16-in-1 Cooking functions: Fry, bake, grill, roast, dehydrate", "Dishwasher safe removable parts"))
                .sellerName("Philips Home Retail")
                .sellerRating(4.8)
                .warrantyInfo("2 Years Philips Worldwide Guarantee")
                .isTopDeal(true)
                .estimatedDeliveryDays("Tomorrow")
                .freeDelivery(true)
                .build();
        p7 = productRepository.save(p7);

        // Product 8: Quick Commerce - Roasted Almonds & Trail Mix
        Product p8 = Product.builder()
                .title("VYROX Fresh California Roasted Almonds & Cranberry Trail Mix (500g)")
                .sku("VYR-QC-ALM500")
                .description("Handpicked premium California almonds lightly salted and paired with sweet sun-dried cranberries. Delivered in 15 minutes.")
                .category(catQuick)
                .brand(vyroxChoice)
                .mrp(BigDecimal.valueOf(650))
                .sellingPrice(BigDecimal.valueOf(399))
                .stockQuantity(120)
                .inStock(true)
                .averageRating(4.8)
                .reviewCount(780)
                .mainImageUrl("https://images.unsplash.com/photo-1508746829417-e6f548d8d6ed?w=800&auto=format&fit=crop&q=80")
                .highlights(List.of("100% California Premium Grade Almonds", "High Protein & Rich in Vitamin E", "Vacuum packed for maximum freshness", "Instant 15-Minute Darkstore Dispatch"))
                .sellerName("VYROX Express Darkstore #101")
                .sellerRating(4.95)
                .isQuickCommerceEligible(true)
                .isTopDeal(true)
                .estimatedDeliveryDays("15 Mins")
                .freeDelivery(false)
                .build();
        p8 = productRepository.save(p8);

        // Product 9: Quick Commerce - Organic Cold Pressed Juice 1L
        Product p9 = Product.builder()
                .title("VYROX Organic Cold-Pressed Valencia Orange Juice (1 Litre, No Added Sugar)")
                .sku("VYR-QC-JUICE1L")
                .description("Pure, cold-pressed 100% natural Valencia orange juice. Chilled and delivered directly to your doorstep in 15 minutes.")
                .category(catQuick)
                .brand(vyroxChoice)
                .mrp(BigDecimal.valueOf(250))
                .sellingPrice(BigDecimal.valueOf(179))
                .stockQuantity(90)
                .inStock(true)
                .averageRating(4.9)
                .reviewCount(540)
                .mainImageUrl("https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=800&auto=format&fit=crop&q=80")
                .highlights(List.of("100% Raw Cold Pressed Orange Juice", "Zero Preservatives, Zero Added Sugar", "Cold chain maintained at 4°C", "Delivered in under 15 minutes"))
                .sellerName("VYROX Express Darkstore #101")
                .sellerRating(4.95)
                .isQuickCommerceEligible(true)
                .isTopDeal(true)
                .estimatedDeliveryDays("15 Mins")
                .freeDelivery(false)
                .build();
        p9 = productRepository.save(p9);

        // 8. Seed Customer Reviews
        reviewRepository.save(ProductReview.builder()
                .product(p1)
                .user(customer)
                .reviewerName("Karan Sharma")
                .rating(5)
                .title("Absolute powerhouse of a machine!")
                .comment("Upgraded from an Intel MacBook and the M3 Pro is mind-blowing. Battery lasts 2 full working days without breaking a sweat.")
                .verifiedPurchase(true)
                .helpfulCount(42)
                .build());

        reviewRepository.save(ProductReview.builder()
                .product(p3)
                .user(customer)
                .reviewerName("Priya Venkatesh")
                .rating(5)
                .title("Best Android camera on the market hands down.")
                .comment("Galaxy AI features like live call translate and Circle to Search are game changers for my work travel.")
                .verifiedPurchase(true)
                .helpfulCount(31)
                .build());

        // 9. Seed Coupons
        couponRepository.save(Coupon.builder()
                .code("VYROX100")
                .description("Flat ₹100 OFF on orders above ₹499")
                .discountType(DiscountType.FLAT_AMOUNT)
                .discountValue(BigDecimal.valueOf(100))
                .minOrderAmount(BigDecimal.valueOf(499))
                .active(true)
                .validUntil(LocalDateTime.now().plusMonths(6))
                .build());

        couponRepository.save(Coupon.builder()
                .code("SMART20")
                .description("20% OFF up to ₹500 on all Electronics & Mobiles")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .minOrderAmount(BigDecimal.valueOf(999))
                .maxDiscountAmount(BigDecimal.valueOf(500))
                .active(true)
                .validUntil(LocalDateTime.now().plusMonths(6))
                .build());

        couponRepository.save(Coupon.builder()
                .code("FESTIVE500")
                .description("Mega ₹500 OFF on orders above ₹2,999")
                .discountType(DiscountType.FLAT_AMOUNT)
                .discountValue(BigDecimal.valueOf(500))
                .minOrderAmount(BigDecimal.valueOf(2999))
                .active(true)
                .validUntil(LocalDateTime.now().plusMonths(6))
                .build());

        // 10. Seed Demo Live Order for live GPS tracking demonstration
        Order demoOrder = Order.builder()
                .orderNumber("VYR-2026-90412")
                .user(customer)
                .status(OrderStatus.OUT_FOR_DELIVERY)
                .subtotal(BigDecimal.valueOf(578))
                .discountAmount(BigDecimal.valueOf(322))
                .couponDiscount(BigDecimal.valueOf(100))
                .coinsDiscount(BigDecimal.valueOf(50))
                .deliveryFee(BigDecimal.valueOf(15))
                .grandTotal(BigDecimal.valueOf(443))
                .coinsEarned(22)
                .coinsRedeemed(50)
                .couponCodeApplied("VYROX100")
                .shippingAddress(homeAddr)
                .paymentMethod(PaymentMethod.UPI)
                .paymentStatus(PaymentStatus.SUCCESS)
                .doorstepOtp("4829")
                .quickCommerce(true)
                .estimatedDeliveryTime("Delivering in 8 Minutes")
                .deliveryLatitude(12.9784)
                .deliveryLongitude(77.6408)
                .build();
        demoOrder = orderRepository.save(demoOrder);

        OrderItem oi1 = OrderItem.builder()
                .order(demoOrder)
                .product(p8)
                .productTitle(p8.getTitle())
                .productSku(p8.getSku())
                .mainImageUrl(p8.getMainImageUrl())
                .unitPrice(p8.getSellingPrice())
                .quantity(1)
                .totalPrice(p8.getSellingPrice())
                .build();
        OrderItem oi2 = OrderItem.builder()
                .order(demoOrder)
                .product(p9)
                .productTitle(p9.getTitle())
                .productSku(p9.getSku())
                .mainImageUrl(p9.getMainImageUrl())
                .unitPrice(p9.getSellingPrice())
                .quantity(1)
                .totalPrice(p9.getSellingPrice())
                .build();
        orderItemRepository.saveAll(List.of(oi1, oi2));

        trackingLogRepository.save(TrackingLog.builder()
                .order(demoOrder)
                .status(OrderStatus.CONFIRMED)
                .description("Order confirmed by Darkstore #101")
                .locationName("Indiranagar Darkstore")
                .latitude(12.9716)
                .longitude(77.6412)
                .build());
        trackingLogRepository.save(TrackingLog.builder()
                .order(demoOrder)
                .status(OrderStatus.PACKED)
                .description("Items picked, fresh cold-chain bag sealed")
                .locationName("Indiranagar Darkstore")
                .latitude(12.9716)
                .longitude(77.6412)
                .build());
        trackingLogRepository.save(TrackingLog.builder()
                .order(demoOrder)
                .status(OrderStatus.OUT_FOR_DELIVERY)
                .description("Rider Ramesh Kumar picked up package on Ather EV Scooter")
                .locationName("HAL 2nd Stage Main Road")
                .latitude(12.9740)
                .longitude(77.6380)
                .build());

        System.out.println(">>> VYROX SEED DATA COMPLETE! Ready for Web & Android Clients. <<<");
    }

    private void saveSpecs(Product product, List<String[]> specList) {
        int order = 1;
        for (String[] s : specList) {
            ProductSpecification spec = ProductSpecification.builder()
                    .product(product)
                    .specGroup(s[0])
                    .specName(s[1])
                    .specValue(s[2])
                    .displayOrder(order++)
                    .build();
            specificationRepository.save(spec);
        }
    }
}
