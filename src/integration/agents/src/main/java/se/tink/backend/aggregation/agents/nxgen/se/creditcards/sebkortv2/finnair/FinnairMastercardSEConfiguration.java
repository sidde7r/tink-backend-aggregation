package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.finnair;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

public class FinnairMastercardSEConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return FinnairMastercardSEConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return FinnairMastercardSEConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return FinnairMastercardSEConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return FinnairMastercardSEConstants.BANKID_METHOD;
    }
}
