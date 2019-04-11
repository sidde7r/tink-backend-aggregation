package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankConfigurationEntity {

    private List<String> configurations;
    private String applicationInstanceId;
    private String configurationName;
    private String updated;

    public List<String> getConfigurations() {
        return configurations;
    }

    public OpBankConfigurationEntity setConfigurations(List<String> configurations) {
        this.configurations = configurations;
        return this;
    }

    public String getApplicationInstanceId() {
        return applicationInstanceId;
    }

    public OpBankConfigurationEntity setApplicationInstanceId(String applicationInstanceId) {
        this.applicationInstanceId = applicationInstanceId;
        return this;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public OpBankConfigurationEntity setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
        return this;
    }

    public String getUpdated() {
        return updated;
    }

    public OpBankConfigurationEntity setUpdated(String updated) {
        this.updated = updated;
        return this;
    }
}
