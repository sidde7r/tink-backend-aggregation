package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;

public class InnloggetRequest extends MultivaluedMapImpl {

    private InnloggetRequest(String wresult) {
        add(
                SparebankenVestConstants.InnloggetForm.WA,
                SparebankenVestConstants.InnloggetForm.WA_VALUE);
        add(
                SparebankenVestConstants.InnloggetForm.W_CTX,
                SparebankenVestConstants.InnloggetForm.W_CTX_VALUE);
        add(SparebankenVestConstants.InnloggetForm.W_RESULT, wresult);
    }

    public static InnloggetRequest build(String wresult) {
        return new InnloggetRequest(wresult);
    }
}
