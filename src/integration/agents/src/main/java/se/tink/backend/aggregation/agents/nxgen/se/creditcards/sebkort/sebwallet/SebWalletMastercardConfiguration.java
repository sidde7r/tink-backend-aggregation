package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.sebwallet;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class SebWalletMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return SebWalletMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return SebWalletMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return SebWalletMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return SebWalletMastercardConstants.BANKID_METHOD;
    }
}
