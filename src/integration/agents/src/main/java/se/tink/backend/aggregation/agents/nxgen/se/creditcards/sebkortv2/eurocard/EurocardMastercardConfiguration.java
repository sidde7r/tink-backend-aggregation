package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.eurocard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

public class EurocardMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return EurocardMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return EurocardMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return EurocardMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return EurocardMastercardConstants.BANKID_METHOD;
    }
}
