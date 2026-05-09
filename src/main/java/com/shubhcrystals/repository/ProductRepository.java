package com.shubhcrystals.repository;

import com.shubhcrystals.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByAvailableTrue();

    List<Product> findByStoneIgnoreCaseAndAvailableTrue(String stone);
}
