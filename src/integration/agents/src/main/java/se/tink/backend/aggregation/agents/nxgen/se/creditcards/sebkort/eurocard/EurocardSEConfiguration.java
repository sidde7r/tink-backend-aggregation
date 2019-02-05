package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.eurocard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class EurocardSEConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return EurocardSEConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return EurocardSEConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return EurocardSEConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return EurocardSEConstants.BANKID_METHOD;
    }
}
