package com.erp.erp.application.controller;

import com.erp.erp.application.dto.ProductDto;
import com.erp.erp.application.dto.response.GsmProductCompactDto;
import com.erp.erp.application.dto.response.ProductMasterDto;
import com.erp.erp.application.product.ProductService;
import com.erp.erp.domain.model.item.GsmProductMaster;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/product")
@PreAuthorize("isAuthenticated()")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/brand/names")
  public ResponseEntity<?> fetchProductByBrand(@RequestParam String brand, @RequestParam(defaultValue = "0") int page) {
    try {
      Page<ProductDto> productDtoPage = productService.getByBrand(brand, page);
      if (productDtoPage.isEmpty()) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(productDtoPage);
    }
    catch (Exception ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  @GetMapping("/brand/names/products")
  public ResponseEntity<?> fetchProductByBrandAndProductName(
      @RequestParam(value = "brand", required = false) String brand,
      @RequestParam(value = "productName", required = false) String productName,
      @RequestParam(defaultValue = "0") int page) {
    try {
      Page<ProductMasterDto> productDtoPage = productService.getByProductName(brand, productName, page);
      if (productDtoPage.isEmpty()) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(productDtoPage);
    }
    catch (Exception ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

  @GetMapping("/select-specs/{id}")
  public ResponseEntity<?> fetchSpecsByProduct(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(productService.getSpecsByProduct(id));
    }
    catch (Exception ex) {
      return ResponseEntity.internalServerError().body(ex.getMessage());
    }
  }

}
