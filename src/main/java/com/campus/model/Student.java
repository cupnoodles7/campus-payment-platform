package com.campus.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    private int studentId;
    private String name;
    private Optional<String> email;
    private Optional<String> phone;
    private Wallet wallet;
}