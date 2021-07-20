package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect.payment;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment.CommerzBankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class ComdirectPaymentExecutor extends CommerzBankPaymentExecutor {

    public ComdirectPaymentExecutor(
            Xs2aDevelopersApiClient apiClient,
            ThirdPartyAppAuthenticationController controller,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            CommerzBankPaymentAuthenticator paymentAuthenticator) {
        super(
                apiClient,
                controller,
                credentials,
                persistentStorage,
                sessionStorage,
                paymentAuthenticator);
        sleepTimeSecond = 5;
    }
}
