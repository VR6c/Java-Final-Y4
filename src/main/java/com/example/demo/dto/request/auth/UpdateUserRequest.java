package com.example.demo.dto.request.auth;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 50, message = "Full name must not exceed 50 characters")
    private String fullName;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
