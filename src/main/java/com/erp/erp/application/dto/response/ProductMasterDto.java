package com.erp.erp.application.dto.response;

import jakarta.validation.constraints.NotNull;

/**
 * Exact DTO representation of ProductMaster entity.
 * Field names match the entity exactly.
 */
public record ProductMasterDto(

    @NotNull Long productMasterId,

    String bands2g,
    String bands3g,
    String batteryType,
    String brand,
    String productLink,
    String displaySize,
    String productDescription,
    String itemName,
    String dimensions,
    String productName,
    String weight,
    String price,
    String internalMemory,
    String talkTime,
    String mainCamera,
    String mainCameraFeatures,
    String status,
    String displayResolution,
    String colors,
    String releaseDate

) {}
