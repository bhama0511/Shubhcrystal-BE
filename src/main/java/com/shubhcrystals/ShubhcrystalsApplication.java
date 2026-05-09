package com.shubhcrystals;

import com.shubhcrystals.model.Product;
import com.shubhcrystals.model.Role;
import com.shubhcrystals.model.User;
import com.shubhcrystals.repository.ProductRepository;
import com.shubhcrystals.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class ShubhcrystalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShubhcrystalsApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(ProductRepository productRepo,
                               UserRepository userRepo,
                               PasswordEncoder encoder) {
        return args -> {
            // Seed admin user
            if (!userRepo.existsByEmail("admin@shubhcrystals.com")) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@shubhcrystals.com");
                admin.setPassword(encoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);
                userRepo.save(admin);
                System.out.println(">>> Admin seeded: admin@shubhcrystals.com / Admin@123");
            }

            // Seed products
            if (productRepo.count() > 0) return;
            productRepo.saveAll(List.of(
                new Product(null, "Amethyst Calm Bracelet", "Amethyst",
                    "Amethyst promotes calmness, clarity, and spiritual protection. Perfect for reducing anxiety and enhancing intuition.",
                    new BigDecimal("799"), "Crown Chakra", "Bestseller", true,
                    "💜", List.of("Reduces stress & anxiety", "Enhances intuition", "Promotes restful sleep")),

                new Product(null, "Rose Quartz Love Bracelet", "Rose Quartz",
                    "Rose Quartz is the stone of unconditional love. It opens the heart chakra and attracts love and compassion.",
                    new BigDecimal("699"), "Heart Chakra", "New", true,
                    "🌸", List.of("Attracts love & relationships", "Heals emotional wounds", "Boosts self-love")),

                new Product(null, "Clear Quartz Energy Bracelet", "Clear Quartz",
                    "Clear Quartz is the master healer. It amplifies energy, enhances clarity, and balances all chakras.",
                    new BigDecimal("599"), "All Chakras", null, true,
                    "🔮", List.of("Amplifies intentions", "Enhances clarity", "Balances all chakras")),

                new Product(null, "Black Tourmaline Protection Bracelet", "Black Tourmaline",
                    "Black Tourmaline is a powerful protective stone that repels negative energies and shields against psychic attacks.",
                    new BigDecimal("899"), "Root Chakra", "Protection", true,
                    "🖤", List.of("Protection from negativity", "Grounding energy", "Stress relief")),

                new Product(null, "Citrine Abundance Bracelet", "Citrine",
                    "Citrine is the stone of abundance and manifestation. It attracts success, prosperity, and positive energy.",
                    new BigDecimal("749"), "Solar Plexus Chakra", null, true,
                    "💛", List.of("Attracts wealth & success", "Boosts confidence", "Promotes positivity")),

                new Product(null, "Lapis Lazuli Wisdom Bracelet", "Lapis Lazuli",
                    "Lapis Lazuli has been prized since antiquity for wisdom and truth. It stimulates enlightenment and enhances dream work.",
                    new BigDecimal("999"), "Third Eye Chakra", "Premium", true,
                    "💙", List.of("Enhances wisdom", "Promotes truth & clarity", "Stimulates creativity"))
            ));
        };
    }
}
