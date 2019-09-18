package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;

public class DeutscheBankDEConfiguration extends DeutscheBankConfiguration {
    private String baseUrl;
    public static final String REGION_ENDPOINT_DE = "/DE/PFB";
    public static final String REGION_PSU_ID_DE = "DE_ONLB_DB";

    @Override
    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(
                        DeutscheBankConstants.ErrorMessages.INVALID_CONFIGURATION, "Base URL"));
        return baseUrl.concat(REGION_ENDPOINT_DE);
    }

    @Override
    public String getPsuIdType() {
        return REGION_PSU_ID_DE;
    }
}
