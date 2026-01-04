package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.constant.CourseType;
import com.cardano_lms.server.constant.Currency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseShortInformationResponse {
    String id;
    String slug;
    String title;
    String imageUrl;
    boolean draft;
    CourseType courseType;
    Integer price;
    Integer discount;
    Integer enrollmentCount;
    
}
