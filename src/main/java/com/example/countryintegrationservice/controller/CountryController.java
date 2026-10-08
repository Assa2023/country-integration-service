package com.example.countryintegrationservice.controller;

import com.example.countryintegrationservice.dto.CountryRequest;
import com.example.countryintegrationservice.model.CountryInfo;
import com.example.countryintegrationservice.service.CountryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping
    public ResponseEntity<CountryInfo> importCountry(
            @Valid @RequestBody CountryRequest request) {

        CountryInfo countryInfo =
                countryService.importCountry(request.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(countryInfo);
    }

    @GetMapping
    public ResponseEntity<List<CountryInfo>> getAllCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryInfo> getCountryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                countryService.getCountryById(id)
        );
    }
    @PutMapping("/{id}")
public ResponseEntity<CountryInfo> updateCountry(
        @PathVariable Long id,
        @RequestBody CountryInfo countryInfo) {

    return ResponseEntity.ok(
            countryService.updateCountry(id, countryInfo)
    );
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCountry(
        @PathVariable Long id) {

    countryService.deleteCountry(id);

    return ResponseEntity.noContent().build();
}
}