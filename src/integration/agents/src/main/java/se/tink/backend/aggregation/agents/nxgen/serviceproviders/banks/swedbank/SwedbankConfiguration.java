package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

public interface SwedbankConfiguration {
    String getApiKey();
    String getBankId();
    String getName();
    boolean isSavingsBank();
}
