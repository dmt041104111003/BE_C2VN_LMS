package com.cardano_lms.server.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiscountRequest {
    private Double discountPercent;
    private LocalDateTime discountEndTime;
}




