package com.example.countryintegrationservice.soap;

import com.example.countryintegrationservice.exception.SoapIntegrationException;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

@Service
public class CountrySoapClient {

    private static final String SOAP_ENDPOINT =
            "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso";

    private static final String COUNTRY_NAMESPACE =
            "http://www.oorsprong.org/websamples.countryinfo";

    private final WebServiceTemplate webServiceTemplate;

    public CountrySoapClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public String getCountryIsoCode(String countryName) {

        String soapBody = """
                <tns:CountryISOCode
                    xmlns:tns="http://www.oorsprong.org/websamples.countryinfo">
                    <tns:sCountryName>%s</tns:sCountryName>
                </tns:CountryISOCode>
                """.formatted(escapeXml(countryName));

        try {
            return sendSoapRequest(soapBody, "CountryISOCodeResult");
        } catch (Exception e) {
            throw new SoapIntegrationException(
                    "Failed to retrieve country ISO code from SOAP service",
                    e
            );
        }
    }

    public Document getFullCountryInfo(String isoCode) {

        String soapBody = """
                <tns:FullCountryInfo
                    xmlns:tns="http://www.oorsprong.org/websamples.countryinfo">
                    <tns:sCountryISOCode>%s</tns:sCountryISOCode>
                </tns:FullCountryInfo>
                """.formatted(escapeXml(isoCode));

        try {
            return sendSoapDocumentRequest(soapBody);
        } catch (Exception e) {
            throw new SoapIntegrationException(
                    "Failed to retrieve full country information from SOAP service",
                    e
            );
        }
    }

    private String sendSoapRequest(String soapBody, String resultElement) {

        DOMResult result = new DOMResult();

        webServiceTemplate.sendSourceAndReceiveToResult(
                SOAP_ENDPOINT,
                new StreamSource(new StringReader(soapBody)),
                result
        );

        Document document = (Document) result.getNode();

        NodeList nodes = document.getElementsByTagNameNS(
                COUNTRY_NAMESPACE,
                resultElement
        );

        if (nodes.getLength() == 0) {
            throw new RuntimeException(
                    "SOAP response did not contain " + resultElement
            );
        }

        return nodes.item(0).getTextContent();
    }

    private Document sendSoapDocumentRequest(String soapBody) {

        DOMResult result = new DOMResult();

        webServiceTemplate.sendSourceAndReceiveToResult(
                SOAP_ENDPOINT,
                new StreamSource(new StringReader(soapBody)),
                result
        );

        return (Document) result.getNode();
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

