package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher;

public interface IdentifiableAccount {
    /**
     * @return Banks internal identifier for the account.
     */
    String getBankIdentifier();
}
