package com.cardano_lms.server.DTO.Request;

import lombok.Data;

@Data
public class AddStudentRequest {
    private String contactType; 
    private String contactValue;
}

