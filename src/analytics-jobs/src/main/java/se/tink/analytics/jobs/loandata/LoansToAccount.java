package se.tink.analytics.jobs.loandata;

import java.util.UUID;
import org.apache.spark.api.java.function.Function;

public class LoansToAccount implements Function<LoanData, UUID> {
    @Override
    public UUID call(LoanData data) throws Exception {
        return data.getAccountId();
    }
}
