package com.order.boot.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateUserRequest", description = "Payload used to update a user")
public record UpdateUserRequest(
        @Schema(example = "alice-updated", description = "New unique username")
        @NotBlank(message = "Username is required")
        @Size(
                min = 3,
                max = 50,
                message = "Username must be between 3 and 50 characters"
        )
        String username,

        @Schema(example = "new-strong-pass-123", description = "New raw password before hashing")
        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 100,
                message = "Password must be between 8 and 100 characters"
        )
        String password
) {
}
