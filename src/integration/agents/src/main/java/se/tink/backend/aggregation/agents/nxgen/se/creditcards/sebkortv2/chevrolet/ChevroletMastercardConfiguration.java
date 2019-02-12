package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.chevrolet;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

public class ChevroletMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return ChevroletMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return ChevroletMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return ChevroletMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return ChevroletMastercardConstants.BANKID_METHOD;
    }
}
