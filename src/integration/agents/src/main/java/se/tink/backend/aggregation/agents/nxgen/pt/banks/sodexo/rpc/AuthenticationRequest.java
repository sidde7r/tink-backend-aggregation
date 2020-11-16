package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class AuthenticationRequest extends AbstractForm {

    public AuthenticationRequest(String nif, String password) {
        put(RequestBodyValues.NIF, nif);
        put(RequestBodyValues.PASSWORD, password);
    }
}
