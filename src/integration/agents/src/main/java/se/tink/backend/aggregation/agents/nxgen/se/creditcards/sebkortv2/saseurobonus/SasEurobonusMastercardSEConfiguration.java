package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.saseurobonus;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;

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
