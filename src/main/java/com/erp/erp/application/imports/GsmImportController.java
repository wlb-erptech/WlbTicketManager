package com.erp.erp.application.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/gsm")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class GsmImportController {

    private final GsmProductImportService importService;

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> importGsm(@RequestParam("file") MultipartFile file) throws Exception {
        importService.upsertFromCsv(file);
        return ResponseEntity.ok().build();
    }
}
