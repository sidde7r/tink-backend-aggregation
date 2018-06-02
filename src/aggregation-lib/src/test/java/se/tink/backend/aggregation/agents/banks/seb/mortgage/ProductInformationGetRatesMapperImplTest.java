package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.GetRateResponse;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.PropertyType;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.product.ProductPropertyKey;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductInformationGetRatesMapperImplTest {

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void parametersToGetRatesRequest() {
        ProductInformationGetRatesMapperImpl mapper =
                new ProductInformationGetRatesMapperImpl(new LocalDate(2016, 12, 13));

        Map<FetchProductInformationParameterKey, Object> parameters =
                ImmutableMap.<FetchProductInformationParameterKey, Object>builder()
                        .put(FetchProductInformationParameterKey.SSN, "201212121212")
                        .put(FetchProductInformationParameterKey.MORTGAGE_AMOUNT, 123.45)
                        .put(FetchProductInformationParameterKey.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)
                        .put(FetchProductInformationParameterKey.MARKET_VALUE, 234.56)
                        .put(FetchProductInformationParameterKey.MUNICIPALITY, "Stockholm")
                        .put(FetchProductInformationParameterKey.NEW_PLACEMENT_VOLUME, 345.67)
                        .build();

        GetRateRequest getRateRequest = mapper.toRateRequest(parameters);

        GetRateRequest expectedRequest = GetRateRequest.builder()
                .withAge(4)
                .withLoanAmount(123.45)
                .withPropertyType(PropertyType.VILLA)
                .withOptionalMarketValue(234.56)
                .withOptionalMunicipality("Stockholm")
                .withOptionalNewPlacementVolume(345.67)
                .build();

        assertThat(getRateRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void parametersToGetRatesRequest_withoutOptionalParameters() {
        ProductInformationGetRatesMapperImpl mapper =
                new ProductInformationGetRatesMapperImpl(new LocalDate(2016, 12, 13));

        Map<FetchProductInformationParameterKey, Object> parameters =
                ImmutableMap.<FetchProductInformationParameterKey, Object>builder()
                        .put(FetchProductInformationParameterKey.SSN, "201212121212")
                        .put(FetchProductInformationParameterKey.MORTGAGE_AMOUNT, 123.45)
                        .put(FetchProductInformationParameterKey.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)
                        .build();

        GetRateRequest getRateRequest = mapper.toRateRequest(parameters);

        // FIXME: We shouldn't need to map the optional values, but as of 1.0 SEB requires us to on all params
        GetRateRequest expectedRequest = GetRateRequest.builder()
                .withAge(4)
                .withLoanAmount(123.45)
                .withPropertyType(PropertyType.VILLA)
                .withOptionalMarketValue(0.0)
                .withOptionalNewPlacementVolume(0.0)
                .withOptionalMunicipality("Stockholm")
                .build();

        assertThat(getRateRequest).isEqualTo(expectedRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parametersToGetRatesRequest_withoutMandatoryParameters_throwsIllegalArgument() {
        ProductInformationGetRatesMapperImpl mapper =
                new ProductInformationGetRatesMapperImpl(new LocalDate(2016, 12, 13));

        Map<FetchProductInformationParameterKey, Object> parameters =
                ImmutableMap.<FetchProductInformationParameterKey, Object>builder()
                        .put(FetchProductInformationParameterKey.SSN, "201212121212")
                        .put(FetchProductInformationParameterKey.PROPERTY_TYPE, "VILLA")
                        .build();

        mapper.toRateRequest(parameters);
    }

    @Test
    public void responseToProductProperties() {
        GetRateResponse response = new GetRateResponse();
        response.setIndicativeRate(3.51);
        response.setDateValid("2016-12-13T00:00:00");

        ProductInformationGetRatesMapperImpl mapper = new ProductInformationGetRatesMapperImpl(null);
        HashMap<ProductPropertyKey, Object> properties = mapper.toProductProperties(response);

        assertThat(properties.get(ProductPropertyKey.INTEREST_RATE))
                .isEqualTo(0.0351);
         
        assertThat(new Date((long) properties.get(ProductPropertyKey.VALIDITY_END_DATE)))
                .isEqualTo(new DateTime(2016, 12, 13, 0, 0, 0, 0).toDate());
    }
}
