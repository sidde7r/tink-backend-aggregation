package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.swedbank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankMarketConfiguration;

public class SwedbankEEConfiguration implements SwedbankMarketConfiguration {

    @Override
    public String getBIC() {
        return BICProduction.ESTONIA;
    }

    @Override
    public String getAuthenticationMethodId() {
        return RequestValues.SMART_ID;
    }

    @Override
    public String getBookingStatus() {
        return QueryValues.BOOKING_STATUS_BOOKED;
    }
}
