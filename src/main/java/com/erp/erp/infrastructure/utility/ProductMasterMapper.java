package com.erp.erp.infrastructure.utility;

import com.erp.erp.application.dto.response.ProductMasterDto;
import com.erp.erp.domain.model.item.GsmProductMaster;

public class ProductMasterMapper {

    public static ProductMasterDto toDto(GsmProductMaster e) {
        return new ProductMasterDto(
            e.getGsmProductMasterId(),
            e.getNetwork2gBands(),
            e.getNetwork3gBands(),
            e.getBatteryType(),
            e.getBrand(),
            e.getPhoneLink(),
            e.getDisplaySize(),
            e.getPhoneLink(),
            e.getPhoneName(),
            e.getBodyDimensions(),
            e.getPhoneName(),
            e.getBodyWeight(),
            e.getMiscPrice(),
            e.getMemoryInternal(),
            e.getBatteryTalkTime(),
            e.getMainCameraSingle(),
            e.getMainCameraFeatures(),
            e.getLaunchStatus(),
            e.getDisplayResolution(),
            e.getMiscColors(),
            "UNAVAILABLE"
        );
    }
}
