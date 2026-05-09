package com.shubhcrystals.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String stone;

    @Column(length = 1000)
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    private String chakra;
    private String badge;
    private String emoji;

    @Column(name = "image_url")
    private String imageUrl;

    private boolean available = true;

    @ElementCollection
    @CollectionTable(name = "product_benefits", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "benefit")
    private List<String> benefits;

    public Product() {}

    public Product(Long id, String name, String stone, String description,
                   BigDecimal price, String chakra, String badge, boolean available,
                   String emoji, List<String> benefits) {
        this.id = id;
        this.name = name;
        this.stone = stone;
        this.description = description;
        this.price = price;
        this.chakra = chakra;
        this.badge = badge;
        this.available = available;
        this.emoji = emoji;
        this.benefits = benefits;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStone() { return stone; }
    public void setStone(String stone) { this.stone = stone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getChakra() { return chakra; }
    public void setChakra(String chakra) { this.chakra = chakra; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
}
