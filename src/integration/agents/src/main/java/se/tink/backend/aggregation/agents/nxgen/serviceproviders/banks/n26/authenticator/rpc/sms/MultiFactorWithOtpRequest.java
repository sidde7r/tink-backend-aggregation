package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.sms;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class MultiFactorWithOtpRequest extends AbstractForm {

    public MultiFactorWithOtpRequest(String challengeType, String mfaToken, String otp) {
        super.put(N26Constants.Body.GRANT_TYPE, challengeType);
        super.put(N26Constants.Body.MultiFactor.MFA_TOKEN, mfaToken);
        super.put(N26Constants.Body.MultiFactor.SMS, otp);
    }
}
