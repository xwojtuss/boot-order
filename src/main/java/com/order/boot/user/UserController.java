package com.order.boot.user;

import com.order.boot.api.ApiErrorResponse;
import com.order.boot.api.ApiPaths;
import com.order.boot.user.dto.CreateUserRequest;
import com.order.boot.user.dto.UpdateUserRequest;
import com.order.boot.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1)
public class UserController {
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "Users returned", content = @Content(
            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
    ))
    @GetMapping("/users")
    List<UserResponse> getUsers() {
        return userService.findAllUsers().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Operation(summary = "Get user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(
                    schema = @Schema(implementation = UserResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            ))
    })
    @GetMapping("/users/{id}")
    UserResponse getUserById(@Parameter(example = "1") @PathVariable Long id) {
        return UserResponse.from(userService.findUserById(id));
    }

    @Operation(summary = "Get user by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(
                    schema = @Schema(implementation = UserResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/search")
    ResponseEntity<UserResponse> getUserByUsername(@Parameter(example = "alice") @RequestParam String username) {
        return userService.findByUsername(username)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created", content = @Content(
                    schema = @Schema(implementation = UserResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            ))
    })
    @PostMapping("/users")
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);

        return ResponseEntity
                .created(URI.create(ApiPaths.V1 + "/users/" + user.getId()))
                .body(UserResponse.from(user));
    }

    @Operation(summary = "Update user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated", content = @Content(
                    schema = @Schema(implementation = UserResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            )),
            @ApiResponse(responseCode = "409", description = "Username already exists", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            ))
    })
    @PutMapping("/users/{id}")
    UserResponse updateUser(@Parameter(example = "1") @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(userService.updateUser(id, request));
    }

    @Operation(summary = "Delete user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    schema = @Schema(implementation = ApiErrorResponse.class)
            ))
    })
    @DeleteMapping("/users/{id}")
    ResponseEntity<Void> deleteUser(@Parameter(example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
