package com.cardano_lms.server.DTO.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerRequest {
    private Long id;
    private String content;
    private boolean correct;
}
