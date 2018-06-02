package se.tink.analytics.jobs.loandata;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.spark.api.java.function.Function;

public class FilterByProvider implements Function<LoanData, Boolean> {

    private ImmutableSet<String> providerNames;

    public FilterByProvider(ImmutableSet<String> providerNames) {

        this.providerNames = providerNames;
    }

    @Override
    public Boolean call(LoanData loanData) throws Exception {
        if (Strings.isNullOrEmpty(loanData.getProviderName())) {
            return false;
        }
        return providerNames.contains(loanData.getProviderName());
    }
}
