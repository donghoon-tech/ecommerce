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
import java.util.List;

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
        if (userRepository.count() > 0) {
            System.out.println("Data already initialized. Skipping...");
            return;
        }

        System.out.println("Initializing Mock Data...");

        // 1. 카테고리 생성
        List<Category> categories = createCategories();

        // 2. 유저 생성 (Admin, Seller, Buyer)
        User seller = createUsers();

        // 3. 상품 생성 (Seller가 등록)
        createProducts(seller, categories);

        System.out.println("Mock Data Initialization Completed!");
    }

    private List<Category> createCategories() {
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

    private User createUsers() {
        // 1. Admin
        User admin = User.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin1234"))
                .name("관리자")
                .representativePhone("010-0000-0000")
                .role(User.Role.admin)
                .businessNumber("000-00-00000")
                .build();
        userRepository.save(admin);

        // 2. Seller (판매자)
        User seller = User.builder()
                .username("seller")
                .passwordHash(passwordEncoder.encode("seller1234"))
                .name("김판매")
                .representativePhone("010-1111-1111")
                .email("seller@example.com")
                .role(User.Role.user)
                .businessNumber("111-11-11111")
                .build();
        userRepository.save(seller);

        BusinessProfile sellerProfile = BusinessProfile.builder()
                .user(seller)
                .businessName("대박자재")
                .businessNumber("111-11-11111")
                .representativeName("김판매")
                .officeAddress("서울시 강남구")
                .storageAddress("경기도 하남시 천현동")
                .status(BusinessProfile.Status.approved)
                .isMain(true)
                .approvedAt(java.time.LocalDateTime.now())
                .approvedBy(admin)
                .build();
        businessProfileRepository.save(sellerProfile);

        // 3. Buyer (구매자)
        User buyer = User.builder()
                .username("buyer")
                .passwordHash(passwordEncoder.encode("buyer1234"))
                .name("이구매")
                .representativePhone("010-2222-2222")
                .email("buyer@example.com")
                .role(User.Role.user)
                .businessNumber("222-22-22222")
                .build();
        userRepository.save(buyer);

        BusinessProfile buyerProfile = BusinessProfile.builder()
                .user(buyer)
                .businessName("튼튼건설")
                .businessNumber("222-22-22222")
                .representativeName("이구매")
                .officeAddress("부산시 해운대구")
                .status(BusinessProfile.Status.approved) // 승인 완료 가정
                .isMain(true)
                .approvedAt(java.time.LocalDateTime.now())
                .approvedBy(admin)
                .build();
        businessProfileRepository.save(buyerProfile);

        return seller;
    }

    private void createProducts(User seller, List<Category> categories) {
        String[] locations = { "경기도 하남시", "충청북도 음성군", "경상북도 경산시" };

        for (int i = 0; i < 20; i++) {
            Category category = categories.get(i % categories.size()); // 랜덤 카테고리
            String condition = (i % 2 == 0) ? "신재" : "고재";

            Product product = Product.builder()
                    .seller(seller)
                    .category(category)
                    .itemName(category.getName() + " " + condition + " 상품-" + i)
                    .itemCondition(condition) // 신재/고재
                    .unitPrice(new BigDecimal((i + 1) * 1000))
                    .saleUnit("개")
                    .stockQuantity(100 + i * 10)
                    .totalAmount(new BigDecimal((i + 1) * 1000 * 100)) // 단순 계산
                    .loadingAddress("주소 데이터 " + i)
                    .loadingAddressDisplay(locations[i % locations.length])
                    .status(Product.Status.selling)
                    .approvedAt(java.time.LocalDateTime.now())
                    .isDisplayed(true)
                    .build();

            productRepository.save(product);

            // 이미지
            ProductImage img1 = ProductImage.builder()
                    .product(product)
                    .imageUrl("https://placehold.co/600x400?text=" + product.getItemName())
                    .displayOrder(1)
                    .build();
            productImageRepository.save(img1);
        }
    }
}
