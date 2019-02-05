package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.entities;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;

public class SecurityParamsRequestBody extends MultivaluedMapImpl {

    public SecurityParamsRequestBody(String wa, String wresult, String wctx) {
        add(SparebankenVestConstants.SecurityParameters.WA, wa);
        add(SparebankenVestConstants.SecurityParameters.WRESULT, wresult);
        add(SparebankenVestConstants.SecurityParameters.WCTX, wctx);
    }
}
