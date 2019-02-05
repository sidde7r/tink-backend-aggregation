package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class InitLoginBody extends MultivaluedMapImpl {

    public InitLoginBody(String nationalId, String viewStateValues) {
        add("login", "login");
        add("ssn", nationalId);
        add("ksd", "undefined");
        add("neste", "Neste");
        add("javax.faces.ViewState", viewStateValues);
    }
}
