package se.tink.analytics.jobs.loandata;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

import java.util.Map;
import se.tink.backend.core.Loan;

/**
 * Filter to only keep lonas where:
 *  - we have user
 *  - we have postal code for user
 *  - interest is not null
 *  - balance is not null
 *  - is a mortgage
 */
public class LoanDataFilter implements Function<LoanData, Boolean> {
    private Broadcast<Map<UUID, UUID>> areaByUser;

    public LoanDataFilter(Broadcast<Map<UUID, UUID>> areaByUser) {
        this.areaByUser = areaByUser;
    }

    @Override
    public Boolean call(LoanData data) throws Exception {
        Map<UUID, UUID> areaByUserMap = areaByUser.value();

        if (!areaByUserMap.containsKey(data.getUserId())) {
            return false;
        }

        if (areaByUserMap.get(data.getUserId()) == null) {
            return false;
        }

        if (data.getInterest() == null) {
            return false;
        }

        if (data.getBalance() == null) {
            return false;
        }

        if (Strings.isNullOrEmpty(data.getType())) {
            return false;
        }

        if (!Objects.equals(Loan.Type.MORTGAGE.toString(), data.getType())) {
            return false;
        }

        return true;
    }
}
