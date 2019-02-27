package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcDkConfiguration extends SdcConfiguration {

    public SdcDkConfiguration(Provider provider) {
        super(provider);
        baseUrl = SdcDkConstants.Market.BASE_URL + bankCode + "/";
    }

    @Override
    public boolean canRetrieveInvestmentData() {
        return true;
    }

    @Override
    public String getPhoneCountryCode() {
        return SdcDkConstants.Market.PHONE_COUNTRY_CODE;
    }

    @Override
    public LogTag getLoanLogTag() {
        return SdcDkConstants.Fetcher.LOAN_LOGGING;
    }

    @Override
    public LogTag getInvestmentsLogTag() {
        return SdcDkConstants.Fetcher.INVESTMENTS_LOGGING;
    }
}
