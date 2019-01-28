package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingPis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.amount.Amount;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.i18n.Catalog;

public class UkOpenBankingBankTransferExecutor implements BankTransferExecutor {
    private static final int MAXIMUM_REFERENCE_LENGTH = 15;

    private final Catalog catalog;
    private final Credentials credentials;
    private final UkOpenBankingApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;
    private final UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> ukOpenBankingAccountFetcher;
    private final UkOpenBankingPis ukOpenBankingPis;

    public UkOpenBankingBankTransferExecutor(
            Catalog catalog,
            Credentials credentials,
            SupplementalInformationHelper supplementalInformationHelper,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration,
            TinkHttpClient httpClient,
            UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> ukOpenBankingAccountFetcher,
            UkOpenBankingPis ukOpenBankingPis) {
        this.catalog = catalog;
        this.credentials = credentials;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;
        this.ukOpenBankingAccountFetcher = ukOpenBankingAccountFetcher;
        this.ukOpenBankingPis = ukOpenBankingPis;

        this.apiClient = new UkOpenBankingApiClient(
                httpClient,
                softwareStatement,
                providerConfiguration,
                OpenIdConstants.ClientMode.PAYMENTS
        );
    }

    private List<TransactionalAccount> getTransactionalAccounts() {
        return ukOpenBankingAccountFetcher.fetchAccounts();
    }

    private boolean matchingAccount(TransactionalAccount account, AccountIdentifier accountIdentifier) {
        return account.getIdentifiers().stream().anyMatch(identifier -> identifier.equals(accountIdentifier));
    }

    private boolean hasAccountIdentifier(AccountIdentifier accountIdentifier) {
        List<TransactionalAccount> tinkAccounts = getTransactionalAccounts();

        return tinkAccounts.stream()
                .anyMatch(account -> matchingAccount(account, accountIdentifier));
    }

    private boolean isImmediateTransfer(Transfer transfer) {
        return Objects.isNull(transfer.getDueDate());
    }

    private String sanitizeReferenceText(String referenceText) {
        if (Objects.isNull(referenceText)) {
            return "";
        }

        // Only allow [a-z0-9\\s]
        referenceText = referenceText.replaceAll("[^a-zA-Z0-9\\s]", "");

        // Limit the length to MAXIMUM_REFERENCE_LENGTH
        return referenceText.substring(0, Math.min(MAXIMUM_REFERENCE_LENGTH, referenceText.length()));
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        if (!isImmediateTransfer(transfer)) {
            return Optional.of(catalog.getString("Cannot schedule future transfers."));
        }

        AccountIdentifier sourceIdentifier = ukOpenBankingPis.mustNotHaveSourceAccountSpecified() ?
                null : transfer.getSource();
        AccountIdentifier destinationIdentifier = transfer.getDestination();
        Amount amount = transfer.getAmount();
        String referenceText = sanitizeReferenceText(transfer.getDestinationMessage());

        if (!ukOpenBankingPis.mustNotHaveSourceAccountSpecified() && !hasAccountIdentifier(sourceIdentifier)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(TransferExecutionException.EndUserMessage.INVALID_SOURCE.getKey().get())
                    .build();
        }

        UkOpenBankingPaymentsAuthenticator paymentAuthenticator = new UkOpenBankingPaymentsAuthenticator(
                apiClient,
                softwareStatement,
                providerConfiguration,
                ukOpenBankingPis,
                sourceIdentifier,
                destinationIdentifier,
                amount,
                referenceText
        );

        // Do not use the real PersistentStorage because we don't want to overwrite the AIS auth token.
        PersistentStorage dummyStorage = new PersistentStorage();

        OpenIdAuthenticationController openIdAuthenticationController = new OpenIdAuthenticationController(
                dummyStorage,
                supplementalInformationHelper,
                apiClient,
                paymentAuthenticator
        );

        ThirdPartyAppAuthenticationController<String> thirdPartyAppAuthenticationController =
                new ThirdPartyAppAuthenticationController<>(openIdAuthenticationController,
                        supplementalInformationHelper
                );

        try {
            thirdPartyAppAuthenticationController.authenticate(credentials);
        } catch (AuthenticationException | AuthorizationException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Authentication error.")
                    .build();
        }

        String intentId = paymentAuthenticator.getIntentId()
                .orElseThrow(() ->
                        TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                .setMessage("No intentId.")
                                .build()
                );

        ukOpenBankingPis.executeBankTransfer(
                apiClient,
                intentId,
                sourceIdentifier,
                destinationIdentifier,
                amount,
                referenceText
        );

        return Optional.empty();
    }
}
