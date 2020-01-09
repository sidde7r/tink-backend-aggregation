package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConstants.Market.EIKA_BANKS;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConstants.Logging;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class SdcNoConfiguration extends SdcConfiguration {

    public SdcNoConfiguration(Provider provider) {
        super(provider);
        if (EIKA_BANKS.contains(provider.getPayload())) {
            baseUrl = Market.EIKA_BASE_URL + bankCode + "/";
        } else {
            baseUrl = Market.BASE_URL + bankCode + "/";
        }
    }

    @Override
    public boolean canRetrieveInvestmentData() {
        return true;
    }

    @Override
    public String getPhoneCountryCode() {
        return Market.PHONE_COUNTRY_CODE;
    }

    @Override
    public LogTag getLoanLogTag() {
        return Logging.LOAN_TAG;
    }

    @Override
    public LogTag getInvestmentsLogTag() {
        return Logging.INVESTMENTS_TAG;
    }
}
