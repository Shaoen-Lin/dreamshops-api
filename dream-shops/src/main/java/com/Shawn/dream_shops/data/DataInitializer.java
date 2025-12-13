package com.Shawn.dream_shops.data;

import com.Shawn.dream_shops.model.Category;
import com.Shawn.dream_shops.model.Product;
import com.Shawn.dream_shops.model.Role;
import com.Shawn.dream_shops.model.User;
import com.Shawn.dream_shops.repository.CategoryRepository;
import com.Shawn.dream_shops.repository.ProductRepository;
import com.Shawn.dream_shops.repository.RoleRepository;
import com.Shawn.dream_shops.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Transactional
@Component //  宣告為 Spring 組件 (Component)，在應用程式啟動時自動掃描、偵測並管理這個類別。
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private final UserRepository userRepo;
    @Autowired
    private final RoleRepository roleRepo;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final ProductRepository productRepo;
    @Autowired
    private final CategoryRepository categoryRepo;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Set<String> defaultRoles = Set.of("ROLE_ADMIN","ROLE_USER");
        createDefaultUserIfNotExits();
        createDefaultRoleIfNotExits(defaultRoles);
        createDefaultAdminIfNotExits();
    }

    private void createDefaultUserIfNotExits() {

        Role userRole = roleRepo.findByName("ROLE_USER").get();
        for (int i = 1; i <= 5; ++i) {
            String defaultEmail = "user" + i + "@email.com";

            if (userRepo.existsByEmail(defaultEmail)) {
                continue;
            }
            User user = new User();
            user.setFirstName("The User");
            user.setLastName("User" + i);
            user.setEmail(defaultEmail);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRoles(Set.of(userRole));
            userRepo.save(user);
            System.out.println("Default vet user " + i + " created successfully.");
        }
    }

    private void createDefaultAdminIfNotExits() {

        Role adminRole = roleRepo.findByName("ROLE_ADMIN").get();
        for (int i = 1; i <= 2; ++i) {
            String defaultEmail = "admin" + i + "@email.com";

            if (userRepo.existsByEmail(defaultEmail)) {
                continue;
            }
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Admin" + i);
            user.setEmail(defaultEmail);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRoles(Set.of(adminRole));
            userRepo.save(user);
            System.out.println("Default vet user " + i + " created successfully.");
        }
    }

    private void createDefaultRoleIfNotExits(Set<String> roles){
            roles.stream()
                    .filter(role -> roleRepo.findByName(role). isEmpty())
                    .map(Role::new).forEach(roleRepo::save);
    }

    private void createDefaultProductsIfNotExists() {

        // 1. 準備分類
        Category catElectronics = categoryRepo.findByName("Electronics");
        if (catElectronics == null) {
            catElectronics = new Category("Electronics");
            categoryRepo.save(catElectronics);
        }

        Category catAudio = categoryRepo.findByName("Audio");
        if (catAudio == null) {
            catAudio = new Category("Audio");
            categoryRepo.save(catAudio);
        }

        // 2. 建立商品
        createProduct("Apple Watch", "Apple", new BigDecimal("400.00"), 47,
                "Apple new AI Watch", catElectronics);

        createProduct("Apple Pencil", "Apple", new BigDecimal("100.00"), 200,
                "Apple new Apple Pencil", catElectronics);

        createProduct("Apple Airpods Pro", "Apple", new BigDecimal("250.00"), 996,
                "Apple new Earphone", catAudio);

        createProduct("Samsung Watch", "Samsung", new BigDecimal("200.00"), 500,
                "Samsung new AI Watch", catElectronics);
    }

    private void createProduct(String name, String brand, BigDecimal price, int inventory, String description, Category category) {

        if (productRepo.existsByName(name)) {
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setInventory(inventory);
        product.setDescription(description);
        product.setCategory(category);

        productRepo.save(product);
        System.out.println("Default product " + name + " created.");
    }
}