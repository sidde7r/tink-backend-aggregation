package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankMarketConfiguration;

public class SwedbankSEConfiguration implements SwedbankMarketConfiguration {

    @Override
    public String getBIC() {
        return BICProduction.SWEDEN;
    }

    @Override
    public String getAuthenticationMethodId() {
        return RequestValues.MOBILE_ID;
    }

    @Override
    public String getBookingStatus() {
        return SwedbankConstants.QueryValues.BOOKING_STATUS_BOTH;
    }
}
