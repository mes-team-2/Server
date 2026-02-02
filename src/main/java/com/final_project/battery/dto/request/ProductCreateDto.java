package com.final_project.battery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductCreateDto {
    private ProductInfo product;
    private List<BomItem> bomItems;

    @Getter @Setter @NoArgsConstructor
    public static class ProductInfo {
        private String productCode;
        private String productName;
        private Integer voltage;
        private Integer capacityAh;
    }

    @Getter @Setter @NoArgsConstructor
    public static class BomItem {
        private String materialCode;
        private Double qty;
    }
}