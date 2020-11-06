package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.Errorable;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.PewCodeVerifier;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EBankingBusinessMessageBulkEntity implements Errorable {
    private Object globalIndicator;
    private List<Object> messages;
    private Object text;
    private Object pewCode;

    @Override
    public void checkError() {
        PewCodeVerifier.checkPewCode(pewCode);
    }
}
