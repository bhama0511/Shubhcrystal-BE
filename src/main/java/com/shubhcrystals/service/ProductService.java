package com.shubhcrystals.service;

import com.shubhcrystals.model.Product;
import com.shubhcrystals.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> getAllAvailable() {
        return repo.findByAvailableTrue();
    }

    public List<Product> getByStone(String stone) {
        return repo.findByStoneIgnoreCaseAndAvailableTrue(stone);
    }

    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    public Product create(Product product) {
        return repo.save(product);
    }

    public Product update(Long id, Product updated) {
        Product existing = getById(id);
        existing.setName(updated.getName());
        existing.setStone(updated.getStone());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setChakra(updated.getChakra());
        existing.setBadge(updated.getBadge());
        existing.setAvailable(updated.isAvailable());
        return repo.save(existing);
    }

    public void delete(Long id) {
        getById(id);
        repo.deleteById(id);
    }
}
