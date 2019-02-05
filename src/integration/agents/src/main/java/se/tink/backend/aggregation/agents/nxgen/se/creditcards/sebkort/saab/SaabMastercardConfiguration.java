package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.saab;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class SaabMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return SaabMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return SaabMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return SaabMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return SaabMastercardConstants.BANKID_METHOD;
    }
}
