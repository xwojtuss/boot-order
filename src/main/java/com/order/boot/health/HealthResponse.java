package com.order.boot.health;

import io.swagger.v3.oas.annotations.media.Schema;

public record HealthResponse(@Schema(example = "ok") String status) {
}
