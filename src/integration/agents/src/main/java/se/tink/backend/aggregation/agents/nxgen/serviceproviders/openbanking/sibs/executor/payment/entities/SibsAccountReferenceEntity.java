package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsAccountReferenceEntity {

    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public static SibsAccountReferenceEntity of(
            Supplier<AccountIdentifierType> accountTypeSupplier,
            Supplier<String> accountNumberSupplier)
            throws PaymentValidationException {
        SibsAccountReferenceEntity are = new SibsAccountReferenceEntity();
        switch (accountTypeSupplier.get()) {
            case IBAN:
                are.setIban(accountNumberSupplier.get());
                break;
            case PAYMENT_CARD_NUMBER:
                are.setPan(accountNumberSupplier.get());
                break;
            case PAYM_PHONE_NUMBER:
                are.setMsisdn(accountNumberSupplier.get());
                break;
            default:
                throw new PaymentValidationException(
                        String.format("Not supported payment type: %s", accountTypeSupplier.get()),
                        "");
        }

        return are;
    }

    public Creditor toTinkCreditor() throws PaymentException {
        Creditor creditor;
        if (StringUtils.isNotEmpty(iban)) {
            creditor = new Creditor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
        } else if (StringUtils.isNotEmpty(msisdn)) {
            creditor =
                    new Creditor(
                            AccountIdentifier.create(
                                    AccountIdentifierType.PAYM_PHONE_NUMBER, iban));
        } else if (StringUtils.isNotEmpty(pan)) {
            creditor =
                    new Creditor(
                            AccountIdentifier.create(
                                    AccountIdentifierType.PAYMENT_CARD_NUMBER, iban));
        } else {
            throw new PaymentException("Unsupported payment type returned");
        }
        return creditor;
    }

    public Debtor toTinkDebtor() throws PaymentException {
        Debtor debtor;
        if (StringUtils.isNotEmpty(iban)) {
            debtor = new Debtor(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
        } else if (StringUtils.isNotEmpty(msisdn)) {
            debtor =
                    new Debtor(
                            AccountIdentifier.create(
                                    AccountIdentifierType.PAYM_PHONE_NUMBER, iban));
        } else if (StringUtils.isNotEmpty(pan)) {
            debtor =
                    new Debtor(
                            AccountIdentifier.create(
                                    AccountIdentifierType.PAYMENT_CARD_NUMBER, iban));
        } else {
            throw new PaymentException("Unsupported payment type returned");
        }
        return debtor;
    }

    public static SibsAccountReferenceEntity fromCreditor(Payment payment)
            throws PaymentValidationException {
        return SibsAccountReferenceEntity.of(
                () -> payment.getCreditor().getAccountIdentifierType(),
                () -> payment.getCreditor().getAccountNumber());
    }

    public static SibsAccountReferenceEntity fromDebtor(Payment payment)
            throws PaymentValidationException {
        if (payment.getDebtor() == null) {
            return null;
        }
        return SibsAccountReferenceEntity.of(
                () -> payment.getDebtor().getAccountIdentifierType(),
                () -> payment.getDebtor().getAccountNumber());
    }
}
