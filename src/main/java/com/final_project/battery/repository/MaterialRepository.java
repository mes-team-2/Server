package com.final_project.battery.repository;

import com.final_project.battery.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByMaterialCode(String materialCode);
}