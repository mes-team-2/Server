package com.final_project.battery.dto.response;

import com.final_project.battery.domain.BOM;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BomResponseDto {
    private Long id;            // BOM ID (수정 시 필요)
    private String materialCode;
    private String materialName;
    private Double qty;         // 소요량
    private String unit;
    private String process;     // 투입 공정 (BOM.note 필드 활용)

    public BomResponseDto(BOM bom) {
        this.id = bom.getBomId();
        this.materialCode = bom.getMaterial().getMaterialCode();
        this.materialName = bom.getMaterial().getMaterialName();
        this.qty = bom.getRequiredQty().doubleValue();
        this.unit = bom.getMaterial().getUnit();
        this.process = bom.getNote();
    }
}