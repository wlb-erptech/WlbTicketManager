package com.erp.erp.infrastructure.utility;

import com.erp.erp.application.dto.response.GsmProductCompactDto;
import com.erp.erp.domain.model.item.GsmProductMaster;

public class GsmProductMapper {
    public static GsmProductCompactDto toCompactDto(GsmProductMaster e) {
        return new GsmProductCompactDto(
            e.getGsmProductMasterId(),
            e.getBrand(),
            e.getPhoneName(),
            e.getDisplaySize(),
            e.getDisplayType(),
            e.getPlatformChipset(),
            e.getPlatformCpu(),
            e.getPlatformGpu(),
            e.getMemoryInternal(),
            pickFirstNonNull(
                e.getMainCameraQuad(),
                e.getMainCameraTriple(),
                e.getMainCameraDual(),
                e.getMainCameraSingle()
            ),
            pickFirstNonNull(
                e.getSelfieCameraTriple(),
                e.getSelfieCameraDual(),
                e.getSelfieCameraSingle()
            ),
            e.getBatteryType(),
            e.getBatteryCharging(),
            e.getMiscPrice()
        );
    }

    private static String pickFirstNonNull(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
