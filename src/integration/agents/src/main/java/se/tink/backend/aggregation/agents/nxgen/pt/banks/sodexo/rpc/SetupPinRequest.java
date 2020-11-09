package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class SetupPinRequest extends AbstractForm {

    public SetupPinRequest(String pinNew) {
        put(RequestBodyValues.PIN_NEW, pinNew);
    }
}
