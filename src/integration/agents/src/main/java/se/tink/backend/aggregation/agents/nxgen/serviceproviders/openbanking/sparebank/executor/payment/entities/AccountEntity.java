package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountEntity {
    private String iban;
    private String bban;

    public AccountEntity() {}

    public AccountEntity(
            Type accountType, String accountNumber, SparebankPaymentProduct paymentProduct) {
        switch (paymentProduct) {
            case SEPA_CREDIT_TRANSFER:
                setUpIban(accountNumber);
                break;
            default:
                setUpBban(accountType, accountNumber);
        }
    }

    public static AccountEntity ofCreditor(
            PaymentRequest paymentRequest, SparebankPaymentProduct paymentProduct) {
        Creditor creditor = paymentRequest.getPayment().getCreditor();
        return new AccountEntity(
                creditor.getAccountIdentifierType(), creditor.getAccountNumber(), paymentProduct);
    }

    public static AccountEntity ofDebtor(
            PaymentRequest paymentRequest, SparebankPaymentProduct paymentProduct) {
        Debtor debtor = paymentRequest.getPayment().getDebtor();
        return new AccountEntity(
                debtor.getAccountIdentifierType(), debtor.getAccountNumber(), paymentProduct);
    }

    public Debtor toTinkDebtor() {
        return iban != null
                ? new Debtor(new IbanIdentifier(iban))
                : new Debtor(AccountIdentifier.create(Type.NO, bban));
    }

    public Creditor toTinkCreditor() {
        return iban != null
                ? new Creditor(AccountIdentifier.create(Type.IBAN, iban))
                : new Creditor(AccountIdentifier.create(Type.NO, bban));
    }

    private void setUpBban(Type accountIdentifier, String accountNumber) {
        if (accountIdentifier == Type.IBAN) {
            this.bban = accountNumber.substring(4);
        } else {
            this.bban = accountNumber;
        }
    }

    private void setUpIban(String accountNumber) {
        this.iban = accountNumber;
    }
}
