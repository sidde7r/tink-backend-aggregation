package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageValues.DECOUPLED_APPROACH;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class CommerzBankPaymentAuthenticator {

    private final Credentials credentials;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    private final ThirdPartyAppAuthenticationController redirectAuthenticator;
    private final CommerzBankDecoupledPaymentAuthenticator decoupledAuthenticator;

    public void authorizePayment(Payment payment) {
        String username = credentials.getField(Key.USERNAME);

        if (username != null && isDecoupledAuthenticationPossible()) {
            decoupledAuthenticator.authenticate();
        } else {
            // OauthHelper used by the redirectAuthenticator is expecting this paymentId in storage.
            persistentStorage.put(StorageKeys.PAYMENT_ID, payment.getUniqueId());
            redirectAuthenticator.authenticate(credentials);
        }
    }

    boolean isDecoupledAuthenticationPossible() {
        String scaApproach = sessionStorage.get(StorageKeys.SCA_APPROACH);
        return DECOUPLED_APPROACH.equals(scaApproach);
    }
}
