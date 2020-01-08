package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.entities.VerificationChannelEntity;

public class VerificationOptionsResponse extends ArrayList<VerificationChannelEntity> {

    public boolean hasCallOption() {
        return this.stream().anyMatch(VerificationChannelEntity::isCallChannel);
    }
}
