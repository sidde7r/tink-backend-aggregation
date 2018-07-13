package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

public class FilterAccountsRequest {
    private boolean includeCreditAccounts;
    private boolean includeDebitAccounts;
    private boolean onlyQueryable;
    private boolean onlyFavorites;
    private boolean includeLoans;

    public boolean isIncludeCreditAccounts() {
        return includeCreditAccounts;
    }

    public FilterAccountsRequest setIncludeCreditAccounts(boolean includeCreditAccounts) {
        this.includeCreditAccounts = includeCreditAccounts;
        return this;
    }

    public boolean isIncludeDebitAccounts() {
        return includeDebitAccounts;
    }

    public FilterAccountsRequest setIncludeDebitAccounts(boolean includeDebitAccounts) {
        this.includeDebitAccounts = includeDebitAccounts;
        return this;
    }

    public boolean isOnlyQueryable() {
        return onlyQueryable;
    }

    public FilterAccountsRequest setOnlyQueryable(boolean onlyQueryable) {
        this.onlyQueryable = onlyQueryable;
        return this;
    }

    public boolean isOnlyFavorites() {
        return onlyFavorites;
    }

    public FilterAccountsRequest setOnlyFavorites(boolean onlyFavorites) {
        this.onlyFavorites = onlyFavorites;
        return this;
    }

    public boolean isIncludeLoans() {
        return includeLoans;
    }

    public FilterAccountsRequest setIncludeLoans(boolean includeLoans) {
        this.includeLoans = includeLoans;
        return this;
    }
}
