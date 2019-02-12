package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.circlek;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

public class CircleKMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return CircleKMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return CircleKMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return CircleKMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return CircleKMastercardConstants.BANKID_METHOD;
    }
}
