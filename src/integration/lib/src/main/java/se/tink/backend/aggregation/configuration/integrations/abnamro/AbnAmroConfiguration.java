package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

public class AbnAmroConfiguration {
    @JsonProperty @Deprecated private boolean downForMaintenance;
    @JsonProperty private ImmutableMap<Integer, Integer> rateLimitPermits;
    @JsonProperty private boolean ignoreCreditCardErrors = false;

    @JsonProperty
    private AbnAmroInternetBankingConfiguration internetBanking =
            new AbnAmroInternetBankingConfiguration();

    @JsonProperty private TrustStoreConfiguration trustStore = new TrustStoreConfiguration();

    @JsonProperty
    private DatawarehouseRemoteConfiguration datawarehouseExportRemote =
            new DatawarehouseRemoteConfiguration();

    @JsonProperty
    private AbnAmroEnrollmentConfiguration enrollment = new AbnAmroEnrollmentConfiguration();

    public ImmutableMap<Integer, Integer> getRateLimitPermits() {
        return rateLimitPermits;
    }

    @Deprecated
    public boolean isDownForMaintenance() {
        return downForMaintenance;
    }

    public AbnAmroInternetBankingConfiguration getInternetBankingConfiguration() {
        return internetBanking;
    }

    public TrustStoreConfiguration getTrustStoreConfiguration() {
        return trustStore;
    }

    public DatawarehouseRemoteConfiguration getDatawarehouseRemoteConfiguration() {
        return datawarehouseExportRemote;
    }

    public AbnAmroEnrollmentConfiguration getEnrollmentConfiguration() {
        return enrollment;
    }

    public boolean shouldIgnoreCreditCardErrors() {
        return ignoreCreditCardErrors;
    }
}
