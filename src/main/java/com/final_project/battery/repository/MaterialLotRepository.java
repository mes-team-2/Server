package com.final_project.battery.repository;

import com.final_project.battery.domain.Machine;
import com.final_project.battery.domain.Material;
import com.final_project.battery.domain.MaterialLot;
import com.final_project.battery.domain.common.MaterialLotStatus;
import com.final_project.battery.dto.response.MaterialLotAllResponseDto;
import com.final_project.battery.dto.response.MaterialLotBasicInfoDto;
import com.final_project.battery.dto.response.MaterialLotManagementResponseDto;
import io.micrometer.common.KeyValues;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MaterialLotRepository extends JpaRepository<MaterialLot, Long> {

    // [핵심] 선입선출(FIFO)을 위해 '가장 먼저 들어온 순서(inputDate ASC)'로 조회
    // 단, 재고가 남아있는(AVAILABLE) 것만 가져옴
    @Query("SELECT ml FROM MaterialLot ml WHERE ml.material = :material AND ml.status = 'AVAILABLE' ORDER BY ml.inputDate ASC")
    List<MaterialLot> findAvailableLotsByMaterial(@Param("material") Material material);

    Optional<MaterialLot> findFirstByMaterial_MaterialIdAndCurrentMachineIsNullAndStatusOrderByInputDateAsc(
            Long materialId, MaterialLotStatus status);

    List<MaterialLot> findByMaterialOrderByInputDateDesc(Material material);

    List<MaterialLot> findByCurrentMachineAndStatus(Machine currentMachine, MaterialLotStatus status);

    // 조건부 전체 합계 가져오기
    @Query("""
select new com.final_project.battery.dto.response.MaterialLotAllResponseDto(
    count(l),
    coalesce(sum(case 
        when l.status = com.final_project.battery.domain.common.MaterialLotStatus.AVAILABLE 
        then 1 else 0 end), 0),
    coalesce(sum(case 
        when l.status = com.final_project.battery.domain.common.MaterialLotStatus.HOLD 
        then 1 else 0 end), 0),
    coalesce(sum(case 
        when l.status = com.final_project.battery.domain.common.MaterialLotStatus.EXHAUSTED 
        then 1 else 0 end), 0)
)
from MaterialLot l
join l.material m
where (:status is null or l.status = :status)
  and (
    :keyword is null or
    lower(m.materialName) like lower(concat('%', :keyword, '%')) or
    lower(m.materialCode) like lower(concat('%', :keyword, '%')) or
    lower(l.materialLotNo) like lower(concat('%', :keyword, '%'))
  )
  and (:startDate is null or l.inputDate >= :startDate)
  and (:endDate is null or l.inputDate <= :endDate)
""")
    MaterialLotAllResponseDto getSummary(
            @Param("status") MaterialLotStatus status,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );



    // 조건부 데이터 페이지로 가져오기
    @Query(
            value = """
            select new com.final_project.battery.dto.response.MaterialLotManagementResponseDto(
                l.materialLotId,          
                l.inputDate,              
                l.status,                 
                l.materialLotNo,        
                m.materialCode,        
                m.materialName,        
                l.inQty,                
                (l.inQty - l.remainQty),  
                l.inputDate               
            )
            from MaterialLot l
            join l.material m
            where (:status is null or l.status = :status)
              and (
                :keyword is null or
                lower(m.materialName) like lower(concat('%', :keyword, '%')) or
                lower(m.materialCode) like lower(concat('%', :keyword, '%')) or
                lower(l.materialLotNo) like lower(concat('%', :keyword, '%'))
              )
              and (:startDate is null or l.inputDate >= :startDate)
              and (:endDate is null or l.inputDate <= :endDate)
            """,
            countQuery = """
            select count(l)
            from MaterialLot l
            join l.material m
            where (:status is null or l.status = :status)
              and (
                :keyword is null or
                lower(m.materialName) like lower(concat('%', :keyword, '%')) or
                lower(m.materialCode) like lower(concat('%', :keyword, '%')) or
                lower(l.materialLotNo) like lower(concat('%', :keyword, '%'))
              )
              and (:startDate is null or l.inputDate >= :startDate)
              and (:endDate is null or l.inputDate <= :endDate)
            """
            )
    Page<MaterialLotManagementResponseDto> searchLot(
            @Param("status") MaterialLotStatus status,
            @Param("keyword") String keyword,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );


    // 자재lot 상세 기본정보
    @Query("""
        select new com.final_project.battery.dto.response.MaterialLotBasicInfoDto(
            ml.materialLotId,
            ml.materialLotNo,
            ml.status,
            ml.inputDate,
            m.materialCode,
            m.materialName,
            m.unit,
            ml.remainQty
        )
        from MaterialLot ml
        join ml.material m
        where ml.materialLotId = :lotId
    """)
    Optional<MaterialLotBasicInfoDto> findBasicInfo(@Param("lotId") Long lotId);
}