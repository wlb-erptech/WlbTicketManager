package com.erp.erp.application.dto.response;

/**
 * Compact view of GsmProductMaster for listing/search API.
 */
public record GsmProductCompactDto(

    Long id,
    String brand,
    String name,
    String displaySize,
    String displayType,
    String platformChipset,
    String platformCpu,
    String platformGpu,
    String memoryInternal,
    String mainCameraSummary,
    String selfieCameraSummary,
    String batteryType,
    String batteryCharging,
    String miscPrice

) {}
