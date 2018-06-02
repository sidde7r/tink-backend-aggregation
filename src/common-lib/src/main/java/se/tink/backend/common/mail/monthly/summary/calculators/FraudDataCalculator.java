package se.tink.backend.common.mail.monthly.summary.calculators;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import se.tink.backend.common.mail.monthly.summary.model.FraudData;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.Market;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.User;
import se.tink.libraries.date.DateUtils;

import java.util.List;

public class FraudDataCalculator {

    private User user;
    private List<FraudDetails> fraudDetails;
    private List<FraudItem> fraudItems;

    public FraudDataCalculator(User user, List<FraudDetails> fraudDetails, List<FraudItem> fraudItems) {
        this.user = user;
        this.fraudDetails = fraudDetails;
        this.fraudItems = fraudItems;
    }

    private boolean isFraudActivated() {
        return user.getProfile().getFraudPersonNumber() != null;
    }

    public FraudData getFraudDataForPeriod(final ResolutionTypes resolution, final int periodBreakDate,
            final String period) {

        FraudData result = new FraudData();
        result.setActivated(isFraudActivated());

        if (result.isFraudInactive()) {
            return result;
        }

        // Check if there is outstanding fraud details
        ImmutableList<Integer> unHandledCount = FluentIterable.from(fraudItems).transform(
                FraudItem::getUnhandledDetailsCount).toList();

        int totalUnhandled = 0;
        for (Integer i : unHandledCount) {
            totalUnhandled += i;
        }

        // Check which fraud items that was handled during period
        int updatedInPeriod = FluentIterable.from(fraudDetails).filter(
                fraudDetails -> fraudDetails.getUpdated() != null && (Objects.equal(period,
                        DateUtils.getMonthPeriod(fraudDetails.getUpdated(), resolution, periodBreakDate)))).size();

        result.setUpdatedCount(updatedInPeriod);
        result.setUnhandledEventsCount(totalUnhandled);

        // Only enable fraud for SE market
        result.setFraudEnabledOnUserMarket(Market.Code.SE.equals(user.getProfile().getMarketAsCode()));

        return result;

    }

}
