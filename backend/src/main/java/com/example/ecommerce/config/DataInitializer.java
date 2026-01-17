package com.example.ecommerce.config;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Member;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.MemberRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (memberRepository.count() > 1) { // admin 외에 더 있으면 스킵
            System.out.println("Data already initialized. Skipping...");
            return;
        }

        System.out.println("Initializing Mock Data...");

        // 1. 테스트 유저 생성
        createTestUsers();

        // 2. 카테고리 조회 (V1__init.sql에서 생성됨)
        Category scaffolding = categoryRepository.findByCode("scaffolding"); // 가설재
        Category euroform = categoryRepository.findByCode("euroform"); // 유로폼
        
        if (scaffolding == null || euroform == null) {
            System.out.println("Categories not found. Check V1__init.sql");
            return;
        }

        // 3. 상품 생성
        createProducts(scaffolding, euroform);
        
        System.out.println("Mock Data Initialization Completed!");
    }

    private void createTestUsers() {
        // 일반 유저
        Member user = Member.builder()
                .username("user")
                .password(passwordEncoder.encode("user1234"))
                .role("USER")
                .phone("010-1111-2222")
                .email("user@example.com")
                .companyName("개미건설")
                .build();
        memberRepository.save(user);

        // 사업자 유저 (정보 있는)
        Member bizUser = Member.builder()
                .username("biz")
                .password(passwordEncoder.encode("biz1234"))
                .role("USER")
                .phone("010-3333-4444")
                .email("biz@example.com")
                .companyName("튼튼건설주식회사")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구 테헤란로 123")
                .yardAddress("경기도 하남시 창우동 999")
                .build();
        memberRepository.save(bizUser);
    }

    private void createProducts(Category scaffolding, Category euroform) {
        List<Product> products = new ArrayList<>();

        // --- 가설재 (Scaffolding) 상품들 ---
        String[] scafItems = {"파이프", "클램프", "연결핀", "안전발판"};
        String[] scafSpecs = {"6m", "4m", "3m", "2m", "1.5m"};
        String[] grades = {"NEW", "USED"}; // 공통 코드와 일치시켜야 함 (DB엔 한글 '신재'로 들어갔을 수 있으니 확인 필요, 아까 V1엔 코드로 NEW, USED 넣음)
        // V1__init.sql: ('GRADE', 'NEW', '신재', 1)... 
        // Product.grade 컬럼은 텍스트. 화면에 보여질 땐 '신재'로 보여야 하니, 여기엔 코드를 넣을지 이름을 넣을지 결정해야 함.
        // Product 테이블의 grade는 단순 TEXT 컬럼. 필터링 편의를 위해 'NEW', 'USED' 코드를 넣는게 좋음.

        for (int i = 0; i < 10; i++) {
            String item = scafItems[i % scafItems.length];
            String spec = scafSpecs[i % scafSpecs.length];
            String gradeCode = grades[i % 2];
            String gradeName = gradeCode.equals("NEW") ? "신재" : "고재";
            
            products.add(Product.builder()
                    .categoryId(scaffolding.getId())
                    .name("가설재 " + item + " " + spec + " (" + gradeName + ")")
                    .price(new BigDecimal((i + 1) * 5000))
                    .stockQuantity(100 + i * 10)
                    .grade(gradeCode) // 코드로 저장
                    .itemName(item)
                    .spec(spec)
                    .minOrderQuantity(10)
                    .description("최고 품질의 " + gradeName + " " + item + "입니다.")
                    .thumbnailUrl("https://placehold.co/400x300?text=Scaffolding+" + (i+1))
                    .imageUrls(List.of("https://placehold.co/600x400?text=Detail+1", "https://placehold.co/600x400?text=Detail+2"))
                    .build());
        }

        // --- 유로폼 (Euroform) 상품들 ---
        String[] euroSpecs = {"6012", "4012", "3012", "6009"};
        String[] euroGrades = {"NEW", "SHORT_NEW", "USED"}; // 신재, 쇼트신재, 고재

        for (int i = 0; i < 10; i++) {
            String spec = euroSpecs[i % euroSpecs.length];
            String gradeCode = euroGrades[i % 3];
            String gradeName = gradeCode.equals("NEW") ? "신재" : (gradeCode.equals("SHORT_NEW") ? "쇼트신재" : "고재");

            products.add(Product.builder()
                    .categoryId(euroform.getId())
                    .name("유로폼 " + spec + " (" + gradeName + ")")
                    .price(new BigDecimal((i + 5) * 4000))
                    .stockQuantity(500)
                    .grade(gradeCode)
                    .itemName("유로폼")
                    .spec(spec)
                    .minOrderQuantity(50)
                    .description("현장에서 가장 많이 쓰이는 유로폼 " + spec + " 규격입니다.")
                    .thumbnailUrl("https://placehold.co/400x300?text=Euroform+" + (i+1))
                    .imageUrls(List.of("https://placehold.co/600x400?text=Euro+Front", "https://placehold.co/600x400?text=Euro+Back"))
                    .build());
        }

        productRepository.saveAll(products);
    }
}
