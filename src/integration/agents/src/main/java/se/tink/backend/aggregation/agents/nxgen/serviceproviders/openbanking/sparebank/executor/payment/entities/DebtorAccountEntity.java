package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class DebtorAccountEntity {
    private String iban;
    private String bban;

    public DebtorAccountEntity() {}

    public DebtorAccountEntity(Debtor debtor, SparebankPaymentProduct paymentProduct) {
        switch (paymentProduct) {
            case SEPA_CREDIT_TRANSFER:
                setUpIban(debtor);
                break;
            default:
                setUpBban(debtor);
        }
    }

    public static DebtorAccountEntity of(
            PaymentRequest paymentRequest, SparebankPaymentProduct paymentProduct) {
        Debtor debtor = paymentRequest.getPayment().getDebtor();
        return new DebtorAccountEntity(debtor, paymentProduct);
    }

    public Debtor toTinkDebtor() {
        return iban != null
                ? new Debtor(new IbanIdentifier(iban))
                : new Debtor(AccountIdentifier.create(Type.NO, bban));
    }

    private void setUpBban(Debtor debtor) {
        if (debtor.getAccountIdentifierType() == Type.IBAN) {
            this.bban = debtor.getAccountNumber().substring(4);
        } else {
            this.bban = debtor.getAccountNumber();
        }
    }

    private void setUpIban(Debtor debtor) {
        this.iban = debtor.getAccountNumber();
    }
}
