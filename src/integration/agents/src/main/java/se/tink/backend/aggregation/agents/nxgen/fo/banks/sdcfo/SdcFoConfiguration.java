package se.tink.backend.aggregation.agents.nxgen.fo.banks.sdcfo;

import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcFoConfiguration extends SdcConfiguration {

    public SdcFoConfiguration(Provider provider) {
        super(provider);
        baseUrl = SdcFoConstants.Market.BASE_URL + bankCode + "/";
    }

    @Override
    public boolean canRetrieveInvestmentData() {
        return true;
    }

    @Override
    public String getPhoneCountryCode() {
        return SdcFoConstants.Market.PHONE_COUNTRY_CODE;
    }

    @Override
    public LogTag getLoanLogTag() {
        return SdcFoConstants.Fetcher.LOAN_LOGGING;
    }

    @Override
    public LogTag getInvestmentsLogTag() {
        return SdcFoConstants.Fetcher.INVESTMENTS_LOGGING;
    }
}
