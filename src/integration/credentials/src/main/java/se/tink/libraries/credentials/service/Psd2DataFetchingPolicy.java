package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Psd2DataFetchingPolicy {
    /**
     * This value decides if accounts classified as PSD2 Payment Accounts should be filtered out
     * (removed) from the results or not.
     */
    boolean filterOutPaymentAccounts;
    /**
     * This configuration lets Applications decide if PSD2 Undetermined accounts should be filtered
     * in the same way PSD2 Payment accounts are (@see filterPaymentAccounts).
     */
    boolean treatUndeterminedAccountsAsPaymentAccounts;

    public boolean isFilterOutPaymentAccounts() {
        return filterOutPaymentAccounts;
    }

    public void setFilterOutPaymentAccounts(boolean filterOutPaymentAccounts) {
        this.filterOutPaymentAccounts = filterOutPaymentAccounts;
    }

    public boolean isTreatUndeterminedAccountsAsPaymentAccounts() {
        return treatUndeterminedAccountsAsPaymentAccounts;
    }

    public void setTreatUndeterminedAccountsAsPaymentAccounts(
            boolean treatUndeterminedAccountsAsPaymentAccounts) {
        this.treatUndeterminedAccountsAsPaymentAccounts =
                treatUndeterminedAccountsAsPaymentAccounts;
    }
}
