package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.circlek;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class CircleKMastercardSEConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return CircleKMastercardSEConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return CircleKMastercardSEConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return CircleKMastercardSEConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return CircleKMastercardSEConstants.BANKID_METHOD;
    }
}
