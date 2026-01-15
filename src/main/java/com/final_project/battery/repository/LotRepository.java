package com.final_project.battery.repository;

import com.final_project.battery.domain.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotRepository extends JpaRepository<Lot, Long> {
}
