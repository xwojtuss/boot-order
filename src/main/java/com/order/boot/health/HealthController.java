package com.order.boot.health;

import com.order.boot.api.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1)
public class HealthController {

    @Operation(summary = "Get health status")
    @GetMapping("/health")
    public HealthResponse getHealth() {
        return new HealthResponse("ok");
    }
}
