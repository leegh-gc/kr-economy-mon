package com.example.kreconomonmon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartoonResponse {
    private final String status;
    private final String imageData;
    private final boolean cached;
}
