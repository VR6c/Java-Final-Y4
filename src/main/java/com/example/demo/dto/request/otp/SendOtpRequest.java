package com.example.demo.dto.request.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to generate and send an OTP to email")
public class SendOtpRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Target email address where OTP will be delivered", example = "tharyvireak171@gmail.com")
    private String email;
}
