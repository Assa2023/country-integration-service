package com.example.countryintegrationservice.service;

import com.example.countryintegrationservice.exception.ResourceNotFoundException;
import com.example.countryintegrationservice.model.CountryInfo;
import com.example.countryintegrationservice.model.Language;
import com.example.countryintegrationservice.repository.CountryInfoRepository;
import com.example.countryintegrationservice.soap.CountrySoapClient;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

@Service
public class CountryService {

    private static final String COUNTRY_NAMESPACE =
            "http://www.oorsprong.org/websamples.countryinfo";

    private final CountryInfoRepository countryInfoRepository;
    private final CountrySoapClient countrySoapClient;

    public CountryService(
            CountryInfoRepository countryInfoRepository,
            CountrySoapClient countrySoapClient) {
        this.countryInfoRepository = countryInfoRepository;
        this.countrySoapClient = countrySoapClient;
    }

    public CountryInfo importCountry(String countryName) {

        String formattedName = toSentenceCase(countryName);

        String isoCode = countrySoapClient.getCountryIsoCode(formattedName);

        if (isoCode == null || isoCode.isBlank()) {
            throw new RuntimeException(
                    "Country ISO code was not found for: " + formattedName
            );
        }

        Document document = countrySoapClient.getFullCountryInfo(isoCode);

        CountryInfo countryInfo = mapCountryInfo(document);

        return countryInfoRepository.save(countryInfo);
    }
    
public List<CountryInfo> getAllCountries() {
    return countryInfoRepository.findAll();
}

public CountryInfo getCountryById(Long id) {
    return countryInfoRepository.findById(id)
            .orElseThrow(() ->
        new ResourceNotFoundException("Country not found with id: " + id)
);
            
}
public CountryInfo updateCountry(Long id, CountryInfo updatedCountry) {

    CountryInfo existingCountry = countryInfoRepository.findById(id)
            .orElseThrow(() ->
        new ResourceNotFoundException("Country not found with id: " + id)
);

    existingCountry.setIsoCode(updatedCountry.getIsoCode());
    existingCountry.setName(updatedCountry.getName());
    existingCountry.setCapitalCity(updatedCountry.getCapitalCity());
    existingCountry.setPhoneCode(updatedCountry.getPhoneCode());
    existingCountry.setContinentCode(updatedCountry.getContinentCode());
    existingCountry.setCurrencyIsoCode(updatedCountry.getCurrencyIsoCode());
    existingCountry.setCountryFlag(updatedCountry.getCountryFlag());
    existingCountry.getLanguages().clear();
existingCountry.getLanguages().addAll(updatedCountry.getLanguages());

return countryInfoRepository.save(existingCountry);
}
public void deleteCountry(Long id) {

    if (!countryInfoRepository.existsById(id)) {
        throw new ResourceNotFoundException("Country not found with id: " + id);
    }

    countryInfoRepository.deleteById(id);
}
    private CountryInfo mapCountryInfo(Document document) {

        CountryInfo countryInfo = new CountryInfo();

        countryInfo.setIsoCode(getText(document, "sISOCode"));
        countryInfo.setName(getText(document, "sName"));
        countryInfo.setCapitalCity(getText(document, "sCapitalCity"));
        countryInfo.setPhoneCode(getText(document, "sPhoneCode"));
        countryInfo.setContinentCode(getText(document, "sContinentCode"));
        countryInfo.setCurrencyIsoCode(getText(document, "sCurrencyISOCode"));
        countryInfo.setCountryFlag(getText(document, "sCountryFlag"));

        List<Language> languages = extractLanguages(document);
        countryInfo.setLanguages(languages);

        return countryInfo;
    }

    private List<Language> extractLanguages(Document document) {

        List<Language> languages = new ArrayList<>();

        NodeList languageNodes = document.getElementsByTagNameNS(
                COUNTRY_NAMESPACE,
                "tLanguage"
        );

        for (int i = 0; i < languageNodes.getLength(); i++) {

            Node languageNode = languageNodes.item(i);

            Language language = new Language();

            language.setIsoCode(
                    getChildText(languageNode, "sISOCode")
            );

            language.setName(
                    getChildText(languageNode, "sName")
            );

            languages.add(language);
        }

        return languages;
    }

    private String getText(Document document, String elementName) {

        NodeList nodes = document.getElementsByTagNameNS(
                COUNTRY_NAMESPACE,
                elementName
        );

        if (nodes.getLength() == 0) {
            return null;
        }

        return nodes.item(0).getTextContent();
    }

    private String getChildText(Node parent, String elementName) {

        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node child = children.item(i);

            if (elementName.equals(child.getLocalName())) {
                return child.getTextContent();
            }
        }

        return null;
    }

    private String toSentenceCase(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Country name cannot be empty"
            );
        }

        String trimmed = value.trim().toLowerCase();

        return Character.toUpperCase(trimmed.charAt(0))
                + trimmed.substring(1);
    }
}