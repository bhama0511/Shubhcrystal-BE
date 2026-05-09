package com.shubhcrystals;

import com.shubhcrystals.model.Product;
import com.shubhcrystals.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class ShubhcrystalsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShubhcrystalsApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(ProductRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.saveAll(List.of(
                new Product(null, "Amethyst Calm Bracelet", "Amethyst",
                    "Amethyst promotes calmness, clarity, and spiritual protection.", new BigDecimal("799"), "Crown Chakra", "Bestseller", true),
                new Product(null, "Rose Quartz Love Bracelet", "Rose Quartz",
                    "Rose Quartz opens the heart chakra and attracts love and compassion.", new BigDecimal("699"), "Heart Chakra", "New", true),
                new Product(null, "Clear Quartz Energy Bracelet", "Clear Quartz",
                    "Clear Quartz amplifies energy, enhances clarity, and balances all chakras.", new BigDecimal("599"), "All Chakras", null, true),
                new Product(null, "Black Tourmaline Protection Bracelet", "Black Tourmaline",
                    "Black Tourmaline repels negative energies and shields against psychic attacks.", new BigDecimal("899"), "Root Chakra", "Protection", true),
                new Product(null, "Citrine Abundance Bracelet", "Citrine",
                    "Citrine attracts success, prosperity, and positive energy.", new BigDecimal("749"), "Solar Plexus Chakra", null, true),
                new Product(null, "Lapis Lazuli Wisdom Bracelet", "Lapis Lazuli",
                    "Lapis Lazuli stimulates enlightenment and enhances dream work.", new BigDecimal("999"), "Third Eye Chakra", "Premium", true)
            ));
        };
    }
}
