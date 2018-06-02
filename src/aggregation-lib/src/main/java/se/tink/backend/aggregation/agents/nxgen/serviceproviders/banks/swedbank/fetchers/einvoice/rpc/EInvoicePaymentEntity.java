package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class EInvoicePaymentEntity {
    private ReferenceEntity reference;
    private String amount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;
    private EInvoicePayeeEntity payee;
    private String einvoiceReference;

    public ReferenceEntity getReference() {
        return reference;
    }

    public String getAmount() {
        return amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public EInvoicePayeeEntity getPayee() {
        return payee;
    }

    public String getEinvoiceReference() {
        return einvoiceReference;
    }

    @JsonIgnore
    private Optional<Amount> getTinkAmount(String currency) {
        if (currency == null || amount == null) {
            return Optional.empty();
        }

        return Optional.of(
                new Amount(currency, AgentParsingUtils.parseAmount(amount)));
    }

    public Optional<Transfer> toTinkTransfer(String currency) {
        Optional<Amount> tinkAmount = getTinkAmount(currency);
        Optional<AccountIdentifier.Type> tinkType = Optional.ofNullable(this.payee).flatMap(EInvoicePayeeEntity::getTinkType);
        Optional<String> referenceValue = Optional.ofNullable(this.reference).map(ReferenceEntity::getValue);

        if (!tinkAmount.isPresent() || !tinkType.isPresent() || !referenceValue.isPresent()) {
            return Optional.empty();
        }

        Transfer transfer = new Transfer();
        transfer.setAmount(tinkAmount.get());
        transfer.setType(TransferType.EINVOICE);
        transfer.setDestination(AccountIdentifier.create(tinkType.get(), this.payee.getAccountNumber(),
                this.payee.getName()));
        transfer.setDueDate(dueDate);
        transfer.setDestinationMessage(referenceValue.get());
        transfer.setSourceMessage(this.payee.getName());

        return transfer.getDestination().isValid() ? Optional.of(transfer) : Optional.empty();
    }
}
