package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAuthenticatorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.AuthorizeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

public class UkOpenBankingPaymentsAuthenticator implements OpenIdAuthenticator {
    private final UkOpenBankingApiClient apiClient;
    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;
    private final UkOpenBankingPis ukOpenBankingPis;

    private final AccountIdentifier sourceIdentifier;
    private final AccountIdentifier destinationIdentifier;
    private final Amount amount;
    private final String referenceText;

    private String intentId;

    public UkOpenBankingPaymentsAuthenticator(
            UkOpenBankingApiClient apiClient,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            UkOpenBankingPis ukOpenBankingPis,
            AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            Amount amount,
            String referenceText) {
        this.apiClient = apiClient;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;
        this.ukOpenBankingPis = ukOpenBankingPis;
        this.sourceIdentifier = sourceIdentifier;
        this.destinationIdentifier = destinationIdentifier;
        this.amount = amount;
        this.referenceText = referenceText;
    }

    public Optional<String> getIntentId() {
        return Optional.ofNullable(intentId);
    }

    @Override
    public URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce) {

        intentId =
                ukOpenBankingPis.getBankTransferIntentId(
                        apiClient, sourceIdentifier, destinationIdentifier, amount, referenceText);

        WellKnownResponse wellKnownConfiguration = apiClient.getWellKnownConfiguration();

        return authorizeUrl.queryParam(
                UkOpenBankingAuthenticatorConstants.Params.REQUEST,
                AuthorizeRequest.create()
                        .withClientInfo(providerConfiguration.getClientInfo())
                        .withPaymentsScope()
                        .withSoftwareStatement(softwareStatement)
                        .withState(state)
                        .withNonce(nonce)
                        .withWellknownConfiguration(wellKnownConfiguration)
                        .withIntentId(intentId)
                        .build());
    }
}
