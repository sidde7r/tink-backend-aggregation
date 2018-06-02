package se.tink.backend.main.rpc.calendar;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.libraries.date.ResolutionTypes;

public class GetPeriodListCommand {

        private final String userId;
        private final String period;
        private final ResolutionTypes periodMode;
        private final int periodAdjustedDay;

        public GetPeriodListCommand(String userId, String period, ResolutionTypes periodMode, int periodAdjustedDay) {
            validate(userId, period, periodMode, periodAdjustedDay);
            this.userId = userId;
            this.period = period;
            this.periodMode = periodMode;
            this.periodAdjustedDay = periodAdjustedDay;
        }

        private void validate(String userId, String period, ResolutionTypes periodMode, int periodAdjustedDay) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(userId), "User id cannot be empty.");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(period), "Period cannot be null/empty.");
            Preconditions.checkNotNull(periodMode, "Period mode cannot be null.");
            Preconditions.checkArgument(periodAdjustedDay > 1 && periodAdjustedDay < 31, "Adjusted period on user profile was not valid.");
        }


        public String getPeriod() {
            return period;
        }

        public ResolutionTypes getPeriodMode() {
            return periodMode;
        }

        public int getPeriodAdjustedDay() {
            return periodAdjustedDay;
        }

        public String getUserId() {
            return userId;
        }
}
