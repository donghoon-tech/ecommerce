package com.example.ecommerce.config;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Profile("!test") // 테스트 환경에선 실행 X
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final BusinessProfileRepository businessProfileRepository;
        private final CategoryRepository categoryRepository;
        private final ProductRepository productRepository;
        private final ProductImageRepository productImageRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                // 기존 데이터 포맷 마이그레이션 (하이픈 제거)
                fixPhoneNumberFormat();

                System.out.println("Initializing Mock Data...");

                // 1. 카테고리 생성 (존재하면 스킵)
                List<Category> categories = createCategories();

                // 2. 유저 생성 (Admin, Seller, Buyer) -> 없으면 생성
                List<User> sellers = createUsers();

                // 3. 상품 생성 (Seller들이 등록) -> 없으면 생성
                createProducts(sellers, categories);

                System.out.println("Mock Data Initialization Completed!");
        }

        private void fixPhoneNumberFormat() {
                List<User> users = userRepository.findAll();
                boolean updated = false;
                for (User user : users) {
                        String phone = user.getRepresentativePhone();
                        if (phone != null && phone.contains("-")) {
                                user.setRepresentativePhone(phone.replaceAll("-", ""));
                                updated = true;
                        }
                }
                if (updated) {
                        userRepository.saveAll(users);
                        System.out.println("Migrated existing phone numbers to plain text format.");
                }
        }

        private List<Category> createCategories() {
                if (categoryRepository.count() > 0) {
                        return categoryRepository.findAll();
                }

                List<Category> list = new ArrayList<>();

                // 1. 가설재 (Root)
                Category c1 = categoryRepository.save(Category.builder().name("가설재").depth(0).displayOrder(1).build());

                // 가설재 하위 (품목)
                Category c1_1 = categoryRepository
                                .save(Category.builder().parent(c1).name("파이프").depth(1).displayOrder(1).build());
                Category c1_2 = categoryRepository
                                .save(Category.builder().parent(c1).name("안전발판").depth(1).displayOrder(2).build());
                Category c1_3 = categoryRepository
                                .save(Category.builder().parent(c1).name("써포트").depth(1).displayOrder(3).build());

                list.add(c1_1);
                list.add(c1_2);
                list.add(c1_3);

                // 2. 유로폼 (Root)
                Category c2 = categoryRepository.save(Category.builder().name("유로폼").depth(0).displayOrder(2).build());

                // 유로폼 하위 (품목 - 신재/쇼트 등은 상품 레벨에서 '상태'나 '스펙'으로 처리하거나 카테고리로 분리 가능. 여기선 일단 품목을
                // '유로폼' 자체로)
                // 유로폼은 규격이 중요하므로 규격을 카테고리에 넣거나 상품명에 포함.
                // 요구사항: 구분(유로폼) > 품목(신재/고재) > 규격
                Category c2_1 = categoryRepository
                                .save(Category.builder().parent(c2).name("유로폼(신재)").depth(1).displayOrder(1).build());
                Category c2_2 = categoryRepository
                                .save(Category.builder().parent(c2).name("유로폼(고재)").depth(1).displayOrder(2).build());
                Category c2_3 = categoryRepository
                                .save(Category.builder().parent(c2).name("유로폼(쇼트)").depth(1).displayOrder(3).build());

                list.add(c2_1);
                list.add(c2_2);
                list.add(c2_3);

                return list; // 2depth 카테고리들 반환
        }

        private List<User> createUsers() {
                List<User> sellers = new ArrayList<>();

                // 1. Admin
                if (userRepository.findByUsername("admin").isEmpty()) {
                        User admin = User.builder()
                                        .username("admin")
                                        .passwordHash(passwordEncoder.encode("admin1234"))
                                        .name("관리자")
                                        .representativePhone("01000000000")
                                        .role(User.Role.admin)
                                        .businessNumber("000-00-00000")
                                        .build();
                        userRepository.save(admin);
                }
                Optional<User> adminOptional = userRepository.findByUsername("admin");
                User admin = adminOptional.orElse(null); // Get admin if exists, otherwise null

                // 2. Seller 1 (대박자재)
                User seller1 = userRepository.findByUsername("seller").orElseGet(() -> {
                        User newSeller = User.builder()
                                        .username("seller")
                                        .passwordHash(passwordEncoder.encode("seller1234"))
                                        .name("김판매")
                                        .representativePhone("01011111111")
                                        .email("seller@example.com")
                                        .role(User.Role.user)
                                        .businessNumber("111-11-11111")
                                        .build();
                        userRepository.save(newSeller);

                        // Profile 생성 (사용자가 새로 생성되었을 때만)
                        BusinessProfile profile = BusinessProfile.builder()
                                        .user(newSeller)
                                        .businessName("대박자재")
                                        .businessNumber("111-11-11111")
                                        .representativeName("김판매")
                                        .officeAddress("서울시 강남구")
                                        .storageAddress("경기도 하남시 천현동")
                                        .status(BusinessProfile.Status.approved)
                                        .isMain(true)
                                        .approvedAt(java.time.LocalDateTime.now())
                                        .approvedBy(admin) // approvedBy는 생략하거나 조회해서 넣어야 함
                                        .build();
                        businessProfileRepository.save(profile);
                        return newSeller;
                });
                sellers.add(seller1);

                // 2. Seller 2 (형제건설)
                User seller2 = userRepository.findByUsername("seller2").orElseGet(() -> {
                        User newSeller = User.builder()
                                        .username("seller2")
                                        .passwordHash(passwordEncoder.encode("seller1234"))
                                        .name("이형제")
                                        .representativePhone("01012345678")
                                        .email("seller2@example.com")
                                        .role(User.Role.user)
                                        .businessNumber("222-22-22222")
                                        .build();
                        userRepository.save(newSeller);

                        BusinessProfile profile = BusinessProfile.builder()
                                        .user(newSeller)
                                        .businessName("형제건설")
                                        .businessNumber("222-22-22222")
                                        .representativeName("이형제")
                                        .officeAddress("경기도 성남시")
                                        .storageAddress("경기도 광주시")
                                        .status(BusinessProfile.Status.approved)
                                        .isMain(true)
                                        .approvedAt(java.time.LocalDateTime.now())
                                        .approvedBy(admin)
                                        .build();
                        businessProfileRepository.save(profile);
                        return newSeller;
                });
                sellers.add(seller2);

                // 2. Seller 3 (서울자재)
                User seller3 = userRepository.findByUsername("seller3").orElseGet(() -> {
                        User newSeller = User.builder()
                                        .username("seller3")
                                        .passwordHash(passwordEncoder.encode("seller1234"))
                                        .name("박서울")
                                        .representativePhone("01087654321")
                                        .email("seller3@example.com")
                                        .role(User.Role.user)
                                        .businessNumber("333-33-33333")
                                        .build();
                        userRepository.save(newSeller);

                        BusinessProfile profile = BusinessProfile.builder()
                                        .user(newSeller)
                                        .businessName("서울자재")
                                        .businessNumber("333-33-33333")
                                        .representativeName("박서울")
                                        .officeAddress("서울시 송파구")
                                        .storageAddress("경기도 남양주시")
                                        .status(BusinessProfile.Status.approved)
                                        .isMain(true)
                                        .approvedAt(java.time.LocalDateTime.now())
                                        .approvedBy(admin)
                                        .build();
                        businessProfileRepository.save(profile);
                        return newSeller;
                });
                sellers.add(seller3);

                // 3. Buyer (구매자)
                if (userRepository.findByUsername("buyer").isEmpty()) {
                        User buyer = User.builder()
                                        .username("buyer")
                                        .passwordHash(passwordEncoder.encode("buyer1234"))
                                        .name("최구매")
                                        .representativePhone("01022222222")
                                        .email("buyer@example.com")
                                        .role(User.Role.user)
                                        .businessNumber("444-44-44444")
                                        .build();
                        userRepository.save(buyer);

                        BusinessProfile buyerProfile = BusinessProfile.builder()
                                        .user(buyer)
                                        .businessName("튼튼건설")
                                        .businessNumber("444-44-44444")
                                        .representativeName("최구매")
                                        .officeAddress("부산시 해운대구")
                                        .status(BusinessProfile.Status.approved) // 승인 완료 가정
                                        .isMain(true)
                                        .approvedAt(java.time.LocalDateTime.now())
                                        .approvedBy(admin)
                                        .build();
                        businessProfileRepository.save(buyerProfile);
                }

                // 4. Pending User (가입 대기 중)
                if (userRepository.findByUsername("pending_user").isEmpty()) {
                        User pendingUser = User.builder()
                                        .username("pending_user")
                                        .passwordHash(passwordEncoder.encode("user1234"))
                                        .name("정대기")
                                        .representativePhone("01033333333")
                                        .email("pending@example.com")
                                        .role(User.Role.user)
                                        .businessNumber("555-55-55555")
                                        .build();
                        userRepository.save(pendingUser);

                        BusinessProfile pendingProfile = BusinessProfile.builder()
                                        .user(pendingUser)
                                        .businessName("신규자재")
                                        .businessNumber("555-55-55555")
                                        .representativeName("정대기")
                                        .officeAddress("인천시 남동구")
                                        .storageAddress("인천시 서구")
                                        .status(BusinessProfile.Status.pending) // 대기 중
                                        .isMain(true)
                                        .build();
                        businessProfileRepository.save(pendingProfile);
                }

                return sellers;
        }

        private void createProducts(List<User> sellers, List<Category> categories) {
                String[] locations = { "경기도 하남시", "충청북도 음성군", "경상북도 경산시", "경기도 광주시", "경기도 남양주시" };
                String[] conditions = { "신재", "고재" };

                // Randomly distribute ~30 products among sellers
                for (int i = 0; i < 30; i++) {
                        Category category = categories.get(i % categories.size()); // 랜덤 카테고리
                        User seller = sellers.get(i % sellers.size()); // 랜덤 판매자 (순환)

                        // 해당 판매자가 이미 상품을 가지고 있다면 스킵 (중복 생성 방지 - 단순화된 로직)
                        if (productRepository.countBySellerId(seller.getId()) > 5) { // 5개 이상이면 충분하다고 판단
                                continue;
                        }

                        String condition = conditions[i % conditions.length];

                        Product product = Product.builder()
                                        .seller(seller)
                                        .category(category)
                                        .itemName(category.getName() + " " + condition + " 상품-" + i + " ("
                                                        + seller.getName() + ")")
                                        .itemCondition(condition) // 신재/고재
                                        .loadingAddress("주소 데이터 " + i)
                                        .loadingAddressDisplay(locations[i % locations.length])
                                        .unitPrice(new BigDecimal((i + 1) * 1000))
                                        .saleUnit("개")
                                        .stockQuantity(100 + i * 10)
                                        .totalAmount(new BigDecimal((i + 1) * 1000 * 100)) // 단순 계산
                                        .status(Product.Status.selling)
                                        .approvedAt(java.time.LocalDateTime.now())
                                        .isDisplayed(true)
                                        .build();

                        productRepository.save(product);

                        // 고화질 건설 자재 관련 이미지 URL 목록 (Unsplash)
                        String[] sampleImages = {
                                        "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80", // 파이프/철근
                                        "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=600&q=80", // 비계/Scaffolding
                                        "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600&q=80", // 나무/합판
                                        "https://images.unsplash.com/photo-1503387762-592deb58ef4e?w=600&q=80", // 공사현장
                                        "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=600&q=80" // 창고/적재
                        };

                        // 이미지
                        ProductImage img1 = ProductImage.builder()
                                        .product(product)
                                        .imageUrl(sampleImages[i % sampleImages.length]) // 이미지 순환 할당
                                        .displayOrder(1)
                                        .build();
                        productImageRepository.save(img1);
                }
        }
}
