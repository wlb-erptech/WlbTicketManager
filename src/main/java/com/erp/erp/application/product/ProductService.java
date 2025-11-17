package com.erp.erp.application.product;

import com.erp.erp.application.dto.ProductDto;
import com.erp.erp.application.dto.response.GsmProductCompactDto;
import com.erp.erp.application.dto.response.ProductMasterDto;
import com.erp.erp.domain.model.item.GsmProductMaster;
import com.erp.erp.domain.model.item.ProductMasterRepository;
import com.erp.erp.infrastructure.utility.GsmProductMapper;
import com.erp.erp.infrastructure.utility.ProductMasterMapper;
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
        Pageable pg = PageRequest.of(page, 10, Sort.by("phoneName").ascending());
        Page<GsmProductMaster> entityPage =
            productMasterRepository.findByBrandIgnoreCase(brand, pg);

        return entityPage.map(e ->
            new ProductDto(
                e.getGsmProductMasterId(),
                e.getPhoneName(),
                e.getPhoneLink()
            )
        );
    }

    public Page<ProductMasterDto> getByProductName(String brand, String productName, int page) {
        Pageable pg = PageRequest.of(page, 10, Sort.by("phoneName").ascending());
        Page<GsmProductMaster> entityPage = productMasterRepository
            .findByBrandAndFuzzyName(brand, productName, pg);
        return entityPage.map(ProductMasterMapper::toDto);
    }

    public Map<String, List<String>> getSpecsByProduct(Long productId) {
        Optional<GsmProductMaster> product = productMasterRepository.findByGsmProductMasterId(productId);
        if(product.isEmpty()) {
            throw new IllegalArgumentException("Product not found.");
        }
        List<String> ramRomSpecs = new ArrayList<>();
        List<String> colorSpecs = new ArrayList<>();
        if (product.get().getMemoryInternal() != null && !product.get().getMemoryInternal().isBlank()) {
            ramRomSpecs = Arrays.stream(product.get().getMemoryInternal().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        }
        if(product.get().getMiscColors() != null && !product.get().getMiscColors().isBlank()) {
            colorSpecs = Arrays.stream(product.get().getMiscColors().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        }
        return Map.of(
            "RAM-ROM specs", ramRomSpecs,
            "Color specs", colorSpecs
        );
    }

}
