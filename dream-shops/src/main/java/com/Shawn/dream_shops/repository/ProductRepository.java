package com.Shawn.dream_shops.repository;

import com.Shawn.dream_shops.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    List<Product> findByCategoryName(String category);
    // 這裡會發現如果 直接寫 findByCategory 會發現 JPA 會以為我們要給他 category 物件 
    // 但我們其實要給的是一個string 所以名稱改成 findByCategoryName

    List<Product> findByBrand(String brand);

    List<Product> findByCategoryNameAndBrand(String category, String brand);

    List<Product> findByName(String name);

    List<Product> findByBrandAndName(String brand, String name);

    Long countByBrandAndName(String brand, String name);

    boolean existsByNameAndBrand(String name, String brand);
}