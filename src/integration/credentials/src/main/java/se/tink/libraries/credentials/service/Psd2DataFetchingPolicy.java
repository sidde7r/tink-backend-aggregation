package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Psd2DataFetchingPolicy {
    /**
     * This value decides if accounts classified as PSD2 Payment Accounts should be filtered
     * (removed) from the results or not.
     */
    boolean filterPaymentAccounts;
    /**
     * This configuration lets Applications decide if PSD2 Undetermined accounts should be filtered
     * in the same way PSD2 Payment accounts are (@see filterPaymentAccounts).
     */
    boolean treatUndeterminedAccountsAsPaymentAccounts;

    public boolean isFilterPaymentAccounts() {
        return filterPaymentAccounts;
    }

    public void setFilterPaymentAccounts(boolean filterPaymentAccounts) {
        this.filterPaymentAccounts = filterPaymentAccounts;
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
