package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;

public class BankverlagPaymentAuthenticator extends BankverlagAuthenticator
        implements PaymentAuthenticator {

    public BankverlagPaymentAuthenticator(
            BankverlagApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            BankverlagStorage storage,
            Credentials credentials,
            Catalog catalog,
            String aspspId,
            String aspspName) {
        super(
                apiClient,
                supplementalInformationController,
                storage,
                credentials,
                catalog,
                aspspId,
                aspspName);
    }

    @Override
    public void authenticatePayment(LinksEntity scaLinks) {
        validateInput(credentials);
        initializeAuthorization(scaLinks, credentials);
    }
}
