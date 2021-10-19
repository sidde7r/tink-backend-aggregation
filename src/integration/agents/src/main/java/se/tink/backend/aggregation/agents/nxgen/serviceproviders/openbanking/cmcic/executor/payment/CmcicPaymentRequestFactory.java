package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.PaymentTypeInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.ServiceLevelCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.SupplementaryDataEntity.AcceptedAuthenticationApproachEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingDateUtil;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.uuid.UUIDUtils;

public class CmcicPaymentRequestFactory {

    public PaymentRequestResourceEntity buildPaymentRequest(Payment payment, String callbackUrl) {

        PartyIdentificationEntity initiatingParty =
                new PartyIdentificationEntity(FormValues.PAYMENT_INITIATOR, null, null, null);

        return PaymentRequestResourceEntity.builder()
                .paymentInformationId(UUIDUtils.generateUUID())
                .creationDateTime(FrOpenBankingDateUtil.getCreationDate().toString())
                .numberOfTransactions(FormValues.NUMBER_OF_TRANSACTIONS)
                .requestedExecutionDate(
                        FrOpenBankingDateUtil.getExecutionDate(payment.getExecutionDate())
                                .toString())
                .initiatingParty(initiatingParty)
                .paymentTypeInformation(createPaymentTypeInformation(payment))
                .debtorAccount(createDebtorAccount(payment))
                .beneficiary(createBeneficiary(payment))
                .creditTransferTransaction(createCreditTransferTransaction(payment))
                .supplementaryData(createSupplementaryData(callbackUrl))
                .build();
    }

    private AccountIdentificationEntity createDebtorAccount(Payment payment) {
        return Optional.ofNullable(payment.getDebtor())
                .map(debtor -> new AccountIdentificationEntity(debtor.getAccountNumber(), null))
                .orElse(null);
    }

    private PaymentTypeInformationEntity createPaymentTypeInformation(Payment payment) {
        return new PaymentTypeInformationEntity(
                null,
                ServiceLevelCodeEntity.SEPA,
                PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                        ? PaymentTypeInformation.SEPA_INSTANT_CREDIT_TRANSFER
                        : null,
                null);
    }

    private BeneficiaryEntity createBeneficiary(Payment payment) {
        PartyIdentificationEntity creditor =
                new PartyIdentificationEntity(
                        getPresetOrDefaultCreditorName(payment), null, null, null);

        AccountIdentificationEntity creditorAccount =
                new AccountIdentificationEntity(payment.getCreditor().getAccountNumber(), null);

        return BeneficiaryEntity.builder()
                .creditor(creditor)
                .creditorAccount(creditorAccount)
                .build();
    }

    private List<CreditTransferTransactionEntity> createCreditTransferTransaction(Payment payment) {
        PaymentIdentificationEntity paymentId =
                new PaymentIdentificationEntity(
                        payment.getUniqueId(), UUIDUtils.generateUUID(), null);

        ExactCurrencyAmount exactCurrencyAmount = payment.getExactCurrencyAmount();
        AmountTypeEntity instructedAmount =
                new AmountTypeEntity(
                        exactCurrencyAmount.getCurrencyCode(),
                        exactCurrencyAmount.getExactValue().toString());

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                payment.getRemittanceInformation(), null, RemittanceInformationType.UNSTRUCTURED);

        RemittanceInformationEntity remittanceInformation = new RemittanceInformationEntity();
        remittanceInformation.setUnstructured(
                Collections.singletonList(payment.getRemittanceInformation().getValue()));

        return Collections.singletonList(
                CreditTransferTransactionEntity.builder()
                        .paymentId(paymentId)
                        .instructedAmount(instructedAmount)
                        .remittanceInformation(remittanceInformation)
                        .build());
    }

    private SupplementaryDataEntity createSupplementaryData(String callbackUrl) {
        List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach =
                Collections.singletonList(AcceptedAuthenticationApproachEnum.REDIRECT);
        return SupplementaryDataEntity.builder()
                .acceptedAuthenticationApproach(acceptedAuthenticationApproach)
                .successfulReportUrl(callbackUrl)
                .unsuccessfulReportUrl(callbackUrl)
                .build();
    }

    private String getPresetOrDefaultCreditorName(Payment payment) {
        String creditorName = payment.getCreditor().getName();
        return Strings.isNullOrEmpty(creditorName) ? FormValues.CREDITOR_NAME : creditorName;
    }
}
