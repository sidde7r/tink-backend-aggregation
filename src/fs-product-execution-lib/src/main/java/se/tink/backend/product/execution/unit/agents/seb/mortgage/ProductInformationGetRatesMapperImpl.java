package se.tink.backend.product.execution.unit.agents.seb.mortgage;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.LocalDate;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateRequest;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.GetRateResponse;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.model.PropertyType;
import se.tink.backend.product.execution.annotations.CurrentDate;
import se.tink.libraries.application.ApplicationFieldOptionValues;

public class ProductInformationGetRatesMapperImpl implements ProductInformationGetRatesMapper {

    private static final ImmutableSet<FetchProductInformationParameterKey> MANDATORY_PARAMETERS = ImmutableSet.of(
            FetchProductInformationParameterKey.SSN,
            FetchProductInformationParameterKey.MORTGAGE_AMOUNT,
            FetchProductInformationParameterKey.PROPERTY_TYPE);

    private final LocalDate currentDate;

    @Inject
    public ProductInformationGetRatesMapperImpl(@CurrentDate LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public GetRateRequest toRateRequest(Map<FetchProductInformationParameterKey, Object> parameters) {
        for (FetchProductInformationParameterKey mandatoryParameter : MANDATORY_PARAMETERS) {
            Object mandatoryValue = parameters.get(mandatoryParameter);
            Preconditions.checkArgument(mandatoryValue != null,
                    String.format("Mandatory parameter value for %s.%s is missing",
                            FetchProductInformationParameterKey.class.getSimpleName(),
                            mandatoryParameter.name()));
        }

        GetRateRequest.Builder requestBuilder = GetRateRequest.builder();

        // Mandatory parameters
        int age = getAge(parameters);
        requestBuilder.withAge(age);

        double mortgageAmount = getLoanAmount(parameters);
        requestBuilder.withLoanAmount(mortgageAmount);

        PropertyType propertyType = getPropertyType(parameters);
        requestBuilder.withPropertyType(propertyType);

        // Optionals
        // FIXME: Shouldn't need to dummy fill arguments here, but SEB requires us to as of v1.0
        Object marketValue = parameters.get(FetchProductInformationParameterKey.MARKET_VALUE);
        requestBuilder.withOptionalMarketValue(marketValue != null ? ((Number) marketValue).doubleValue() : 0.0);

        Object newPlacementVolume = parameters.get(FetchProductInformationParameterKey.NEW_PLACEMENT_VOLUME);
        requestBuilder.withOptionalNewPlacementVolume(newPlacementVolume != null ? ((Number) newPlacementVolume)
                .doubleValue() : 0.0);

        // FIXME: The default to use since we only target Stockholm from the start of Tink 2.0
        Object municipality = parameters.get(FetchProductInformationParameterKey.MUNICIPALITY);
        requestBuilder.withOptionalMunicipality(municipality != null ? (String) municipality : "Stockholm");

        return requestBuilder.build();
    }

    private PropertyType getPropertyType(Map<FetchProductInformationParameterKey, Object> parameters) {
        String propertyTypeString = (String) parameters.get(FetchProductInformationParameterKey.PROPERTY_TYPE);
        
        switch (propertyTypeString) {
        case ApplicationFieldOptionValues.APARTMENT:
            return PropertyType.APARTMENT;
        case ApplicationFieldOptionValues.HOUSE:
            return PropertyType.VILLA;
        default:
            return PropertyType.OTHERS;
        }
    }

    private double getLoanAmount(Map<FetchProductInformationParameterKey, Object> parameters) {
        return ((Number) parameters.get(FetchProductInformationParameterKey.MORTGAGE_AMOUNT)).doubleValue();
    }

    private int getAge(Map<FetchProductInformationParameterKey, Object> parameters) {
        String ssnString = (String) parameters.get(FetchProductInformationParameterKey.SSN);

        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(ssnString);
        return ssn.getAge(
                java.time.LocalDate.of(
                        currentDate.getYear(),
                        currentDate.getMonthOfYear(),
                        currentDate.getDayOfMonth()));
    }

    @Override
    public HashMap<ProductPropertyKey, Object> toProductProperties(GetRateResponse rateResponse) {
        HashMap<ProductPropertyKey, Object> properties = new HashMap<>();
        properties.put(ProductPropertyKey.VALIDITY_END_DATE, rateResponse.getDateValid().getTime());
        properties.put(ProductPropertyKey.INTEREST_RATE, rateResponse.getIndicativeRate() / 100);

        return properties;
    }
}
