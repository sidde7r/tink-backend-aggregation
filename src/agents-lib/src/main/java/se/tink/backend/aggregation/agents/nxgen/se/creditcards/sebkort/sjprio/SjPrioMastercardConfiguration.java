package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.sjprio;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class SjPrioMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return SjPrioMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return SjPrioMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return SjPrioMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return SjPrioMastercardConstants.BANKID_METHOD;
    }
}
