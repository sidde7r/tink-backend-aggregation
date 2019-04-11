package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcSeConfiguration extends SdcConfiguration {

    public SdcSeConfiguration(Provider provider) {
        super(provider);
        baseUrl = SdcSeConstants.Market.BASE_URL + bankCode + "/";
    }

    @Override
    public boolean canRetrieveInvestmentData() {
        return false;
    }

    @Override
    public String getPhoneCountryCode() {
        return SdcSeConstants.Market.PHONE_COUNTRY_CODE;
    }

    @Override
    public LogTag getLoanLogTag() {
        return SdcSeConstants.Fetcher.LOAN_LOGGING;
    }

    @Override
    public LogTag getInvestmentsLogTag() {
        return SdcSeConstants.Fetcher.INVESTMENTS_LOGGING;
    }
}
