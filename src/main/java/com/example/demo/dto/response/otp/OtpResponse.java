package com.example.demo.dto.response.otp;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response returned for OTP operations")
public class OtpResponse {

    @Schema(description = "Target email address", example = "tharyvireak171@gmail.com")
    private String email;

    @Schema(description = "Verification status flag", example = "true")
    private Boolean verified;

    @Schema(description = "Informative status message", example = "OTP sent successfully to tharyvireak171@gmail.com")
    private String message;

    @Schema(description = "Expiration time in minutes", example = "5")
    private Integer expiresInMinutes;

    @Schema(description = "Timestamp of the operation")
    private LocalDateTime timestamp;
}
