package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AccountIdentifierDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AmountDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RemittanceInformationStructuredDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.entity.PaymentStatusDto;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankDtoMappers {
    public AccountIdentifierDto createDebtorAccount(Payment payment) {
        final Debtor debtor = payment.getDebtor();

        return AccountIdentifierDto.builder()
                .accountId(debtor.getAccountNumber())
                .identifier(debtor.getAccountIdentifier().getIdentifier())
                .type(debtor.getAccountIdentifierType())
                .build();
    }

    public AccountIdentifierDto createCreditorAccount(Payment payment) {
        final Creditor creditor = payment.getCreditor();

        return AccountIdentifierDto.builder()
                .accountId(creditor.getAccountNumber())
                .identifier(creditor.getAccountIdentifier().getIdentifier())
                .type(creditor.getAccountIdentifierType())
                .build();
    }

    public AmountDto createAmountDto(Payment payment) {
        return AmountDto.builder()
                .amountValue(payment.getExactCurrencyAmount().getExactValue().toString())
                .currency(payment.getExactCurrencyAmount().getCurrencyCode())
                .build();
    }

    public String createUnstructuredRemittanceInfo(Payment payment) {
        return (payment.getRemittanceInformation().getType()
                        == RemittanceInformationType.UNSTRUCTURED)
                ? payment.getRemittanceInformation().getValue()
                : null;
    }

    public RemittanceInformationStructuredDto createStructuredRemittanceInfo(Payment payment) {
        return (payment.getRemittanceInformation().getType() == RemittanceInformationType.REFERENCE)
                ? RemittanceInformationStructuredDto.builder()
                        .reference(payment.getRemittanceInformation().getValue())
                        .referenceType(RemittanceInformationType.REFERENCE.name())
                        .build()
                : null;
    }

    public RemittanceInformation createRemittanceInformation(
            String remittanceInformationUnstructured,
            RemittanceInformationStructuredDto remittanceInformationStructured) {
        final RemittanceInformation remittanceInformation = new RemittanceInformation();

        if (StringUtils.isNotBlank(remittanceInformationUnstructured)) {
            remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
            remittanceInformation.setValue(remittanceInformationUnstructured);
        } else {
            remittanceInformation.setType(RemittanceInformationType.REFERENCE);
            remittanceInformation.setValue(remittanceInformationStructured.getReference());
        }

        return remittanceInformation;
    }

    public ExactCurrencyAmount convertAmountDtoToExactCurrencyAmount(AmountDto amountDto) {
        return new ExactCurrencyAmount(
                new BigDecimal(amountDto.getAmountValue()), amountDto.getCurrency());
    }

    public Debtor convertDebtorAccountToDebtor(AccountIdentifierDto accountIdentifier) {
        return Optional.ofNullable(accountIdentifier)
                .map(this::createAccountIdentifier)
                .map(Debtor::new)
                .orElse(null);
    }

    public Creditor convertCreditorAccountToCreditor(
            AccountIdentifierDto accountIdentifier, String creditorName) {
        return new Creditor(createAccountIdentifier(accountIdentifier), creditorName);
    }

    public AccountIdentifier createAccountIdentifier(AccountIdentifierDto accountIdentifier) {
        return new IbanIdentifier(accountIdentifier.getIdentifier());
    }

    public PaymentStatus convertPaymentStatus(String paymentStatus) {
        final PaymentStatusDto paymentStatusDto =
                PaymentStatusDto.createFromFullName(paymentStatus);

        return convertPaymentStatus(paymentStatusDto);
    }

    public PaymentStatus convertPaymentStatus(PaymentStatusDto paymentStatus) {
        switch (paymentStatus) {
            case RCVD:
                return PaymentStatus.CREATED;
            case ACTC:
                return PaymentStatus.SIGNED;
            case ACSC:
                return PaymentStatus.PAID;
            case CANC:
                return PaymentStatus.CANCELLED;
            case RJCT:
                return PaymentStatus.REJECTED;
            default:
                return PaymentStatus.UNDEFINED;
        }
    }
}
