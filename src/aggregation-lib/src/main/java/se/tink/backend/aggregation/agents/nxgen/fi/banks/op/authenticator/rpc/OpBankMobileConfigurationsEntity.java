package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankMobileConfigurationsEntity extends OpBankResponseEntity {
    private OpBankConfigurationEntity[] mobileConfigurations;

    private boolean isFull(OpBankConfigurationEntity[] mobileConfigurations) {
        return mobileConfigurations.length > 1;
    }

    public OpBankConfigurationEntity[] getMobileConfigurations() {
        return mobileConfigurations;
    }

    public OpBankMobileConfigurationsEntity setMobileConfigurations(
            OpBankConfigurationEntity[] mobileConfigurations) {
        this.mobileConfigurations = mobileConfigurations;
        return this;
    }

    public int getStatus(){
        return status;
    }
}
