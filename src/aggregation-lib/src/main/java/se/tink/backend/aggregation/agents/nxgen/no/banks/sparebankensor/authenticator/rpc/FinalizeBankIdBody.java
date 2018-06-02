package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;

public class FinalizeBankIdBody extends MultivaluedMapImpl {

    private FinalizeBankIdBody() {
        add("bidmobStep2Form", "bidmobStep2Form");
        add("completeBtn", "");
        add("javax.faces.ViewState", SparebankenSorConstants.StaticUrlValues.E1S2);
    }

    public static FinalizeBankIdBody build() {
        return new FinalizeBankIdBody();
    }
}
