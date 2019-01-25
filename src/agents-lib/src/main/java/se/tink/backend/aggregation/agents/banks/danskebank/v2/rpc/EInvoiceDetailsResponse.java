package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers.GiroParser;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceDetailsResponse extends AbstractResponse {
    @JsonProperty("FromAccounts")
    private List<AccountEntity> fromAccounts;
    @JsonProperty("Invoice")
    private URI invoice;
    @JsonProperty("Transaction")
    private EInvoiceDetailsTransactionEntity transaction;

    public List<AccountEntity> getFromAccounts() {
        if (fromAccounts == null) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(FluentIterable
                .from(fromAccounts)
                .filter(Predicates.notNull()));
    }

    public void setFromAccounts(List<AccountEntity> fromAccounts) {
        this.fromAccounts = fromAccounts;
    }

    public URI getInvoice() {
        return invoice;
    }

    public void setInvoice(URI invoice) {
        this.invoice = invoice;
    }

    public EInvoiceDetailsTransactionEntity getTransaction() {
        return transaction;
    }

    public void setTransaction(
            EInvoiceDetailsTransactionEntity transaction) {
        this.transaction = transaction;
    }

    @JsonIgnore
    public Transfer toEInvoiceTransfer(String providerUniqueId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(providerUniqueId));
        Preconditions.checkState(getFromAccounts().size() > 0);
        Preconditions.checkNotNull(transaction);
        Preconditions.checkNotNull(transaction.getAmount());
        Preconditions.checkNotNull(transaction.getReference());
        Preconditions.checkNotNull(transaction.getTime());
        Preconditions.checkNotNull(transaction.getToAccountId());

        Transfer transfer = new Transfer();
        transfer.setType(TransferType.EINVOICE);

        transfer.setAmount(Amount.inSEK(transaction.getAmount()));
        transfer.setDestination(getDestination(transaction.getReceiver()));
        transfer.setDestinationMessage(transaction.getReference());
        transfer.setDueDate(transaction.getDateFromTime());
        transfer.setSource(null); // Since Danske doesn't set the fromaccount, it's selected by the user
        transfer.setSourceMessage(transaction.getReceiver());

        transfer.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, providerUniqueId);

        return transfer;
    }

    @JsonIgnore
    public static AccountIdentifier toAccountIdentifier(AccountEntity fromAccount) {
        return AccountIdentifier.create(AccountIdentifier.Type.SE, fromAccount.getAccountNumber(),
                fromAccount.getAccountName());
    }

    @JsonIgnore
    private AccountIdentifier getDestination(String name) {
        Map<AccountIdentifier.Type, AccountIdentifier> possibleDestinations = GiroParser
                .createPossibleIdentifiersFor(transaction.getToAccountId());
        Preconditions.checkState(Objects.equal(possibleDestinations.size(), 1), "Non-exclusive toAccountId");

        AccountIdentifier destination = FluentIterable.from(possibleDestinations.values()).get(0);
        Preconditions.checkState(destination.isValid(), "Non-valid destination identifier: " + destination.toUriAsString());

        destination.setName(name);

        return destination;
    }
}
