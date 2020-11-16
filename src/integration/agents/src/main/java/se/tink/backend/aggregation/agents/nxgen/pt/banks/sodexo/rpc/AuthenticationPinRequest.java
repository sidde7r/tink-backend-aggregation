package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class AuthenticationPinRequest extends AbstractForm {

    public AuthenticationPinRequest(String pin) {
        put(RequestBodyValues.PIN, pin);
    }
}
