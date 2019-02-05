package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class NumpadRequest extends AbstractForm {

    private NumpadRequest() {
        this.put(BnpParibasConstants.Auth.GRID_TYPE, BnpParibasConstants.Auth.GRID_TYPE_V4iOS);
    }

    public static NumpadRequest create() {
        return new NumpadRequest();
    }
}
