package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.ApiRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class GetRateRequestTest {
    @Test
    public void uriFromMandatoryParameters() {
        ApiRequest ratesRequest = GetRateRequest.builder()
                .withLoanAmount(1000000.00)
                .withAge(24)
                .withPropertyType(PropertyType.VILLA)
                .build();

        String queryString = ratesRequest.getUriPath();
        assertThat(queryString)
                .startsWith("/rates?")
                .contains("loan_amount=1000000.00")
                .contains("age=24")
                .contains("property_type=02");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void tenancyIsNotValidSecurity() {
        GetRateRequest.builder()
                .withLoanAmount(1000000.00)
                .withAge(24)
                .withPropertyType(PropertyType.TENANCY)
                .build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void otherIsNotValidSecurity() {
        GetRateRequest.builder()
                .withLoanAmount(1000000.00)
                .withAge(24)
                .withPropertyType(PropertyType.OTHERS)
                .build();
    }

    @Test
    public void uriFromOptionalParameters() {
        ApiRequest ratesRequest = GetRateRequest.builder()
                .withLoanAmount(1000000.00)
                .withAge(24)
                .withPropertyType(PropertyType.VILLA)
                .withOptionalMarketValue(123.00)
                .withOptionalMunicipality("SVEDALA")
                .withOptionalNewPlacementVolume(1234.00)
                .build();

        String queryString = ratesRequest.getUriPath();
        assertThat(queryString)
                .startsWith("/rates?")
                .contains("loan_amount=1000000.00")
                .contains("age=24")
                .contains("property_type=02")
                .contains("market_value=123.00")
                .contains("municipality=SVEDALA")
                .contains("new_placement_volume=1234.00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void uriWithoutMandatoryParameter_Throws() {
        GetRateRequest.builder()
                //.withLoanAmount(1000000.00)
                .withAge(24)
                .withPropertyType(PropertyType.VILLA)
                .build();
    }

    @Test
    public void uriIsEncoded() {
        ApiRequest ratesRequest = GetRateRequest.builder()
                .withLoanAmount(1000000.00)
                .withAge(24)
                .withPropertyType(PropertyType.VILLA)
                .withOptionalMunicipality("Götte borg")
                .build();

        String queryString = ratesRequest.getUriPath();
        assertThat(queryString)
                .startsWith("/rates?")
                .contains("municipality=G%C3%B6tte+borg");
    }
}
