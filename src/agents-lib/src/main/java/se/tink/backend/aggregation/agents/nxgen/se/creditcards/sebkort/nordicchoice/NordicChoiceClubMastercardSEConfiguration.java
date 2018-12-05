package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.nordicchoice;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortConfiguration;

public class NordicChoiceClubMastercardSEConfiguration implements SebKortConfiguration {
    @Override
    public String getApiKey() {
        return NordicChoiceClubMastercardSEConstants.API_KEY;
    }

    @Override
    public String getProviderCode() {
        return NordicChoiceClubMastercardSEConstants.PROVIDER_CODE;
    }

    @Override
    public String getProductCode() {
        return NordicChoiceClubMastercardSEConstants.PRODUCT_CODE;
    }

    @Override
    public String getBankIdMethod() {
        return NordicChoiceClubMastercardSEConstants.BANKID_METHOD;
    }
}
