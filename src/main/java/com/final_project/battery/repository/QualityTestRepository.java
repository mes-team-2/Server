package com.final_project.battery.repository;

import com.final_project.battery.domain.QualityTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityTestRepository extends JpaRepository<QualityTest, Long> {
}
