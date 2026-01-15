package com.final_project.battery.repository;

import com.final_project.battery.domain.BOM;
import com.final_project.battery.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BomRepository extends JpaRepository<BOM, Long> {
    List<BOM> findByProduct(Product product);
}