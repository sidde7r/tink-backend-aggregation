package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class DebtorEntity {
    private String bban;
    private String iban;

    public DebtorEntity() {}

    public DebtorEntity(Debtor debtor, PaymentType type) {
        switch (type) {
            case SEPA:
                setUpIban(debtor);
                break;
            default:
                setUpBban(debtor);
        }
    }

    @JsonIgnore
    private void setUpIban(Debtor debtor) {
        this.iban = debtor.getAccountNumber();
    }

    @JsonIgnore
    private void setUpBban(Debtor debtor) {
        if (debtor.getAccountIdentifierType() == AccountIdentifierType.IBAN) {
            this.bban = debtor.getAccountNumber().substring(4);
        } else {
            this.bban = debtor.getAccountNumber();
        }
    }

    @JsonIgnore
    public static DebtorEntity of(PaymentRequest paymentRequest, PaymentType type) {
        return new DebtorEntity(paymentRequest.getPayment().getDebtor(), type);
    }

    @JsonIgnore
    public Debtor toTinkDebtor(PaymentType type) {
        if (type == PaymentType.SEPA) {
            return new Debtor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
        }

        return new Debtor(AccountIdentifier.create(AccountIdentifierType.DK, bban));
    }
}
