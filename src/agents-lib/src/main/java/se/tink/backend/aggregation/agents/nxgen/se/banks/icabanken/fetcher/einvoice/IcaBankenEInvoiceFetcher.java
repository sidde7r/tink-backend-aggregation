package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities.OwnAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.ValidateEInvoiceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class IcaBankenEInvoiceFetcher implements EInvoiceFetcher {
    public final Catalog catalog;
    private final IcaBankenApiClient apiClient;
    private final AgentContext context;
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();

    public IcaBankenEInvoiceFetcher(IcaBankenApiClient apiClient, Catalog catalog, AgentContext context) {
        this.apiClient = apiClient;
        this.catalog = catalog;
        this.context = context;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {

        return apiClient.fetchEInvoices().toTinkTransfers(catalog);
    }

    public String getInvoiceIdFrom(Transfer transfer) {
        final Optional<String> invoiceId = transfer.getPayloadValue(TransferPayloadType.PROVIDER_UNIQUE_ID);

        if (!invoiceId.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Missing PROVIDER_UNIQUE_ID on transfer payload")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return invoiceId.get();
    }

    public Transfer getOriginalTransfer(Transfer transfer) {
        return transfer.getOriginalTransfer()
                .orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setMessage("No original transfer on payload to compare with.")
                        .build());
    }

    public OwnAccountsEntity fetchSourceAccountFor(Transfer transfer) {
        List<OwnAccountsEntity> accounts = apiClient.requestAccountsBody().getAccounts().getOwnAccounts();
        return findSourceAccount(transfer.getSource(), accounts);
    }

    public OwnAccountsEntity findSourceAccount(final AccountIdentifier source, List<OwnAccountsEntity> accounts) {
        Optional<OwnAccountsEntity> fromAccount = accounts.stream()
                .filter(ae -> (Objects.equal(source.getIdentifier(DEFAULT_FORMATTER),
                        ae.getAccountNumber().replace(" ", "").replace("-", ""))))
                .findFirst();

        return fromAccount.orElseThrow(() -> TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(
                        context.getCatalog().getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE))
                .build());
    }

    public EInvoiceEntity findEInvoice(String invoiceId) {
        EInvoiceBody response = apiClient.fetchEInvoices();
        Optional<EInvoiceEntity> invoiceEntity = response.getInvoiceById(invoiceId);

        if (!invoiceEntity.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not find the selected invoice.")
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return invoiceEntity.get();
    }

    public EInvoiceEntity findMatchingEInvoice(String invoiceId, Transfer originalTransfer) {
        final EInvoiceEntity eInvoice = findEInvoice(invoiceId);
        Transfer transferAtBank = eInvoice.toTinkTransfer(catalog);

        if (!Objects.equal(originalTransfer.getHash(), transferAtBank.getHash())) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage(UserMessage.EINVOICE_MODIFIED_IN_BANK_APP.getKey().get())
                    .build();
        }

        return eInvoice;
    }

    public enum UserMessage implements LocalizableEnum {
        KNOW_YOUR_CUSTOMER(new LocalizableKey(
                "To be able to refresh your accounts in Tink you need to updated your customer info in the ICA bank app.")),
        EINVOICE_MODIFIED_IN_BANK_APP(new LocalizableKey(
                "If the e-invoice has been modified in the ICA Banken app, please refresh you credentials."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }

    /**
     * Attempts to get a detailed error message from the bank. If not present, it takes the more general alternative.
     */
    public String getEndUserMessage(ValidateEInvoiceResponse errorResponse,
            TransferExecutionException.EndUserMessage generalErrorMessage) {

        return context.getCatalog().getString(generalErrorMessage);
    }
}
