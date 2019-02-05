package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.saseurobonus;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class SasEurobonusMastercardSEConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return SasEurobonusMastercardSEConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return SasEurobonusMastercardSEConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return SasEurobonusMastercardSEConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return SasEurobonusMastercardSEConstants.BANKID_METHOD;
    }
}
