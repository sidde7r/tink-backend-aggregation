package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentResponseEntity;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class CmcicPaymentResponseMapper {

    public PaymentResponse map(PaymentResponseEntity payment) {
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(payment.getResourceId())
                        .withStatus(payment.getPaymentInformationStatusCode().getPaymentStatus())
                        .withCreditor(createCreditor(payment.getBeneficiary()))
                        .withDebtor(createDebtor(payment))
                        .build());
    }

    public PaymentResponse map(PaymentRequestResourceEntity payment) {
        AmountTypeEntity amountTypeEntity =
                payment.getCreditTransferTransaction().get(0).getInstructedAmount();
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(payment.getResourceId())
                        .withExactCurrencyAmount(
                                ExactCurrencyAmount.of(
                                        amountTypeEntity.getAmount(),
                                        amountTypeEntity.getCurrency()))
                        .withStatus(PaymentStatus.PENDING)
                        .withCreditor(createCreditor(payment.getBeneficiary()))
                        .withDebtor(createDebtor(payment))
                        .withExecutionDate(LocalDate.parse(payment.getRequestedExecutionDate()))
                        .build());
    }

    private Debtor createDebtor(PaymentResponseEntity payment) {
        return new Debtor(new IbanIdentifier(payment.getDebtorAccount().getIban()));
    }

    private Debtor createDebtor(PaymentRequestResourceEntity payment) {
        return new Debtor(
                Optional.ofNullable(payment.getDebtorAccount())
                        .map(
                                accountIdentificationEntity ->
                                        new IbanIdentifier(accountIdentificationEntity.getIban()))
                        .orElse(null));
    }

    private Creditor createCreditor(BeneficiaryEntity beneficiary) {
        return new Creditor(
                new IbanIdentifier(beneficiary.getCreditorAccount().getIban()),
                beneficiary.getCreditor().getName());
    }
}
