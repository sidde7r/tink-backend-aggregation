package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.djurgardskortet;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class DjurgardskortetMastercardConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return DjurgardskortetMastercardConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return DjurgardskortetMastercardConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return DjurgardskortetMastercardConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return DjurgardskortetMastercardConstants.BANKID_METHOD;
    }
}
