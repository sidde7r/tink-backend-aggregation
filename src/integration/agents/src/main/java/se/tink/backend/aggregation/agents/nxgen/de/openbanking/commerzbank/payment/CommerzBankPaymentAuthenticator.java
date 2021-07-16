package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankApiClient;

@RequiredArgsConstructor
public class CommerzBankPaymentAuthenticator {

    private final CommerzBankApiClient apiClient;
    private final Credentials credentials;

    public void authenticatePayment(LinksEntity scaLinks) {
        apiClient.authenticate(scaLinks.getScaOAuth(), credentials.getField(Key.USERNAME));
    }
}
