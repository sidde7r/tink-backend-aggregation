package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.generators.se.swedbank.SwedbankScope;
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

    @Override
    public Set<SwedbankScope> getScopes() {
        return Sets.newHashSet(
                SwedbankScope.PSD2,
                SwedbankScope.READ_ACCOUNTS_BALANCES,
                SwedbankScope.READ_TRANSACTIONS_HISTORY,
                SwedbankScope.READ_TRANSACTIONS_HISTORY_OVER90);
    }
}
