package com.example.countryintegrationservice.repository;

import com.example.countryintegrationservice.model.CountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryInfoRepository extends JpaRepository<CountryInfo, Long> {
}