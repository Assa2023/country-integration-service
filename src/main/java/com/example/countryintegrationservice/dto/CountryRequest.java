package com.example.countryintegrationservice.dto;

import jakarta.validation.constraints.NotBlank;

public class CountryRequest {

    @NotBlank(message = "Country name is required")
    private String name;

    public CountryRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}