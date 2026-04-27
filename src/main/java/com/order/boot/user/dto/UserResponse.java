package com.order.boot.user.dto;

import com.order.boot.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserResponse", description = "User returned by the API")
public record UserResponse(Long id, String username) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
