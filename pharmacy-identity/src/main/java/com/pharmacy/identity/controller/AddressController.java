package com.pharmacy.identity.controller;

import com.pharmacy.common.dto.ApiResponse;
import com.pharmacy.identity.dto.AddressDTO;
import com.pharmacy.identity.dto.AddressRequest;
import com.pharmacy.identity.security.JwtUserPrincipal;
import com.pharmacy.identity.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "User address management APIs")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get all addresses", description = "Returns all saved addresses for the current user")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddresses(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        List<AddressDTO> addresses = addressService.getUserAddresses(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Returns a specific address by ID")
    public ResponseEntity<ApiResponse<AddressDTO>> getAddress(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long id) {
        AddressDTO address = addressService.getAddressById(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PostMapping
    @Operation(summary = "Add new address", description = "Adds a new delivery address for the current user")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody AddressRequest request) {
        AddressDTO address = addressService.createAddress(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Updates an existing address")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressDTO address = addressService.updateAddress(principal.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Removes an address from the user's saved addresses")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long id) {
        addressService.deleteAddress(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set default address", description = "Sets an address as the default delivery address")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefaultAddress(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long id) {
        AddressDTO address = addressService.setDefaultAddress(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", address));
    }
}
