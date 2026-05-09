package com.shubhcrystals.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

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

    private boolean available = true;

    public Product() {}

    public Product(Long id, String name, String stone, String description,
                   BigDecimal price, String chakra, String badge, boolean available) {
        this.id = id;
        this.name = name;
        this.stone = stone;
        this.description = description;
        this.price = price;
        this.chakra = chakra;
        this.badge = badge;
        this.available = available;
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

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
