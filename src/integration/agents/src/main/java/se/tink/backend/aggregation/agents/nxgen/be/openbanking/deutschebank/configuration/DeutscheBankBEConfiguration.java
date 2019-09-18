package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;

public class DeutscheBankBEConfiguration extends DeutscheBankConfiguration {
    private String baseUrl;
    public static final String REGION_ENDPOINT_BE = "/BE/DB";
    public static final String REGION_PSU_ID_BE = "BE_ONLB_DB";

    @Override
    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(
                        DeutscheBankConstants.ErrorMessages.INVALID_CONFIGURATION, "Base URL"));
        return baseUrl.concat(REGION_ENDPOINT_BE);
    }

    @Override
    public String getPsuIdType() {
        return REGION_PSU_ID_BE;
    }
}
