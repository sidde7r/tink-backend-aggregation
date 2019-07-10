package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
public class EntryEntity {

    private AmountEntity amount;

    @JsonProperty("counterparty_alias")
    private LabelMonetaryAccountEntity counterpartyAlias;

    @JsonProperty("alias")
    @JsonInclude(Include.NON_NULL)
    private LabelMonetaryAccountEntity alias;

    @JsonProperty private String description;

    @JsonProperty("merchant_reference")
    private String merchantReference;

    public EntryEntity() {}

    public EntryEntity(
            AmountEntity amount,
            LabelMonetaryAccountEntity counterpartyAlias,
            String description,
            String merchantReference) {
        this.amount = amount;
        this.counterpartyAlias = counterpartyAlias;
        this.description = description;
        this.merchantReference = merchantReference;
    }

    public static EntryEntity of(PaymentRequest paymentRequest) throws PaymentException {
        LabelMonetaryAccountEntity counterpartyAlias =
                LabelMonetaryAccountEntity.of(paymentRequest.getPayment().getCreditor());
        AmountEntity amountInquired = AmountEntity.of(paymentRequest.getPayment().getAmount());
        String description = "test";
        String merchantReference = null;
        if (paymentRequest.getPayment().getReference() != null) {
            if (Strings.isNullOrEmpty(paymentRequest.getPayment().getReference().getType())) {
                description = paymentRequest.getPayment().getReference().getValue();
            } else {
                merchantReference = paymentRequest.getPayment().getReference().toString();
            }
        }
        return new EntryEntity(amountInquired, counterpartyAlias, description, merchantReference);
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public Creditor toTinkCreditor() {
        if (counterpartyAlias == null || counterpartyAlias.getIban() == null) {
            return null;
        }
        return new Creditor(
                new IbanIdentifier(counterpartyAlias.getIban()),
                counterpartyAlias.getDisplayName());
    }

    public Debtor toTinkDebtor() {
        if (alias == null || alias.getIban() == null) {
            return null;
        }
        return new Debtor(new IbanIdentifier(alias.getIban()));
    }
}
