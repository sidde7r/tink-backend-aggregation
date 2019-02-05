package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.ingo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class IngoMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return IngoMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return IngoMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return IngoMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return IngoMastercardConstants.BANKID_METHOD;
    }
}
