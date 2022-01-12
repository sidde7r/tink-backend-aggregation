package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageValues.DECOUPLED_APPROACH;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
// This class should be in xs2aDevelopers package, after we split n26 off of it totally
// this is pretty much the same as normal authenticator, but different context, but it should not
// matter here, hm hm.
public class CommerzBankPaymentAuthenticator {

    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

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
        String scaApproach = persistentStorage.get(StorageKeys.SCA_APPROACH);
        return DECOUPLED_APPROACH.equals(scaApproach);
    }
}
