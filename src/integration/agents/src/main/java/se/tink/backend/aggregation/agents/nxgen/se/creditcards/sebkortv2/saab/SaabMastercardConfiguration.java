package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.saab;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

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
