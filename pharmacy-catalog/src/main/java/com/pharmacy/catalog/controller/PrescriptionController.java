package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.PrescriptionDTO;
import com.pharmacy.catalog.security.JwtUserPrincipal;
import com.pharmacy.catalog.service.FileStorageService;
import com.pharmacy.catalog.service.PrescriptionService;
import com.pharmacy.common.feign.PrescriptionCheckDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Prescription management APIs")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @Operation(summary = "Get user prescriptions", description = "Returns all prescriptions for the current user")
    public ResponseEntity<List<PrescriptionDTO>> getUserPrescriptions(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        return ResponseEntity.ok(prescriptionService.getUserPrescriptions(principal.getUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prescription by ID", description = "Returns a specific prescription")
    public ResponseEntity<PrescriptionDTO> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload prescription", description = "Uploads a prescription for a medicine")
    public ResponseEntity<PrescriptionDTO> uploadPrescription(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @RequestParam Long medicineId,
            @RequestParam MultipartFile file) {
        PrescriptionDTO prescription = prescriptionService.uploadPrescription(
                principal.getUserId(), medicineId, file);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download prescription file", description = "Downloads the prescription file")
    public ResponseEntity<byte[]> downloadPrescription(@PathVariable Long id) {
        PrescriptionDTO prescription = prescriptionService.getPrescriptionById(id);
        byte[] fileContent = fileStorageService.downloadFile(prescription.getFilePath().replace("/api/catalog/prescriptions/" + id + "/download", ""));

        String contentType = "application/octet-stream";
        if (prescription.getFileName() != null) {
            if (prescription.getFileName().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (prescription.getFileName().endsWith(".jpg") || prescription.getFileName().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (prescription.getFileName().endsWith(".png")) {
                contentType = "image/png";
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + prescription.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileContent);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending prescriptions", description = "Returns all pending prescriptions for admin review")
    public ResponseEntity<List<PrescriptionDTO>> getPendingPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getPendingPrescriptions());
    }

    @GetMapping("/count/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count pending prescriptions", description = "Returns count of pending prescriptions")
    public ResponseEntity<Long> countPendingPrescriptions() {
        return ResponseEntity.ok(prescriptionService.countPendingPrescriptions());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve prescription", description = "Approves a prescription by admin")
    public ResponseEntity<PrescriptionDTO> approvePrescription(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        PrescriptionDTO prescription = prescriptionService.approvePrescription(id, principal.getUserId());
        return ResponseEntity.ok(prescription);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject prescription", description = "Rejects a prescription with reason")
    public ResponseEntity<PrescriptionDTO> rejectPrescription(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        PrescriptionDTO prescription = prescriptionService.rejectPrescription(id, principal.getUserId(), reason);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/check")
    @Operation(summary = "Check prescription validity", description = "Checks if user has a valid prescription for a medicine")
    public ResponseEntity<PrescriptionCheckDTO> checkPrescription(
            @RequestParam Long userId,
            @RequestParam Long medicineId) {
        boolean hasValid = prescriptionService.hasValidPrescription(userId, medicineId);
        var prescription = prescriptionService.findApprovedPrescription(userId, medicineId);
        
        PrescriptionCheckDTO response = PrescriptionCheckDTO.builder()
                .hasValidPrescription(hasValid)
                .prescriptionId(prescription.map(p -> p.getId()).orElse(null))
                .status(prescription.map(p -> p.getStatus().name()).orElse(null))
                .build();
        
        return ResponseEntity.ok(response);
    }
}
