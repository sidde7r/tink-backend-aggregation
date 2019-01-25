package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.agents.utils.typeguesser.TypeGuesser;

public class SdcNoConfiguration extends SdcConfiguration {

    public SdcNoConfiguration(Provider provider) {
        super(provider);
        if (SdcNoConstants.Market.EIKA_BANKS.contains(provider.getPayload())) {
            baseUrl = SdcNoConstants.Market.EIKA_BASE_URL + bankCode + "/";
        } else {
            baseUrl = SdcNoConstants.Market.BASE_URL + bankCode + "/";
        }

        typeGuesser = TypeGuesser.NORWEGIAN;
    }


    @Override
    public boolean canRetrieveInvestmentData() {
        return true;
    }

    @Override
    public String getPhoneCountryCode() {
        return SdcNoConstants.Market.PHONE_COUNTRY_CODE;
    }

    @Override
    public LogTag getLoanLogTag() {
        return SdcNoConstants.Fetcher.LOAN_LOGGING;
    }

    @Override
    public LogTag getInvestmentsLogTag() {
        return SdcNoConstants.Fetcher.INVESTMENTS_LOGGING;
    }
}
