package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.app;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class AppPollRequest extends AbstractForm {

    public AppPollRequest(String mfaToken) {
        super.put(N26Constants.Body.GRANT_TYPE, N26Constants.Body.MultiFactor.MFA_OOB);
        super.put(N26Constants.Body.MultiFactor.MFA_TOKEN, mfaToken);
    }
}
