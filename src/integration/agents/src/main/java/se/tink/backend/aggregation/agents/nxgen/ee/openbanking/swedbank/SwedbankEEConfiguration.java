package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.swedbank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;

public class SwedbankEEConfiguration implements SwedbankBaseConfiguration {

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
