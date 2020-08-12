package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class NumpadRequest extends AbstractForm {

    public NumpadRequest(String gridType) {
        this.put(BnpParibasConstants.Auth.GRID_TYPE, gridType);
    }
}
