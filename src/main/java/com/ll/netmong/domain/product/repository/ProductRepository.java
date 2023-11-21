package com.ll.netmong.domain.product.repository;

import com.ll.netmong.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
