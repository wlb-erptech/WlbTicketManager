package com.erp.erp.application.imports;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/gsm")
@RequiredArgsConstructor
public class GsmImportController {

    private final GsmProductImportService importService;

    @PostMapping("/import")
    public ResponseEntity<String> importGsm() throws Exception {
        importService.importCsv();
        return ResponseEntity.ok("Import triggered");
    }
}
