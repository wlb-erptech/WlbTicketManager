package com.erp.erp.application.dto;

import java.util.List;

public record CartItemPatchRequest(
    List<CartItemDetailUpdateDto> details // optional
) {}