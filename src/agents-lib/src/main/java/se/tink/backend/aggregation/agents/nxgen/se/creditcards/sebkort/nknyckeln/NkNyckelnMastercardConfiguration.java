package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.nknyckeln;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class NkNyckelnMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return NkNyckelnMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return NkNyckelnMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return NkNyckelnMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return NkNyckelnMastercardConstants.BANKID_METHOD;
    }
}
