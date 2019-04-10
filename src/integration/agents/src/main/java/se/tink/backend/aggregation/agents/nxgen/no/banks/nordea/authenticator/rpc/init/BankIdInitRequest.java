package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.init;

import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.BankIdOperation;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class BankIdInitRequest extends HttpRequestImpl {
    public BankIdInitRequest(String dob, String mobileNumber) {
        super(
                HttpMethod.POST,
                NordeaNoConstants.Url.BANKID_INIT.get(),
                new BankIdInitRequestBody(dob, mobileNumber, BankIdOperation.AUTHENTICATION));
    }
}
