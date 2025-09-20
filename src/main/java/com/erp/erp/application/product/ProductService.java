package com.erp.erp.application.product;

import com.erp.erp.application.dto.ProductDto;
import com.erp.erp.domain.model.item.ProductMaster;
import com.erp.erp.domain.model.item.ProductMasterRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductService {
    private final ProductMasterRepository productMasterRepository;

    public Page<ProductDto> getByBrand(String brand, int page) {
        Pageable pg = PageRequest.of(page, 10, Sort.by("productName").ascending());
        return productMasterRepository
            .findByBrandIgnoreCase(brand, pg);
    }

    public Page<ProductMaster> getByProductName(String brand, String productName, int page) {
        Pageable pg = PageRequest.of(page, 10, Sort.by("productName").ascending());
        return productMasterRepository
            .findByBrandAndFuzzyName(brand, productName, pg);
    }

    public Map<String, List<String>> getSpecsByProduct(Long productId) {
        Optional<ProductMaster> product = productMasterRepository.findByProductMasterId(productId);
        if(product.isEmpty()) {
            throw new IllegalArgumentException("Product not found.");
        }
        List<String> RAMROMSpecs = new ArrayList<>();
        List<String> ColorSpecs = new ArrayList<>();
        if (product.get().getInternalMemory() != null && !product.get().getInternalMemory().isBlank()) {
            RAMROMSpecs = Arrays.stream(product.get().getInternalMemory().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        }
        if(product.get().getColors() != null && !product.get().getColors().isBlank()) {
            ColorSpecs = Arrays.stream(product.get().getColors().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        }
        return Map.of(
            "RAM-ROM specs", RAMROMSpecs,
            "Color specs", ColorSpecs
        );
    }

}
