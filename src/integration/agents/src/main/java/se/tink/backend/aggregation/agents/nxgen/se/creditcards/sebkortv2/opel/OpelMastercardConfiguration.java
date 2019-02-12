package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.opel;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

public class OpelMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return OpelMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return OpelMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return OpelMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return OpelMastercardConstants.BANKID_METHOD;
    }
}
