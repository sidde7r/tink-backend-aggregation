package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.InitiatingPartyEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentRequest {
    private String paymentInformationId;
    private String creationDateTime;
    private double numberOfTransactions;
    private InitiatingPartyEntity initiatingParty;
    private PaymentTypeInformationEntity paymentTypeInformation;
    private DebtorEntity debtor;
    private CreditorEntity creditor;
    private CreditorAccountEntity creditorAccount;
    private CreditorEntity ultimateCreditor;
    private String purpose;
    private String chargeBearer;
    private List<CreditTransferTransactionEntity> creditTransferTransaction;
    private SupplementaryDataEntity supplementaryData;

    private CreatePaymentRequest(Builder builder) {
        this.paymentInformationId = builder.paymentInformationId;
        this.creationDateTime = builder.creationDateTime;
        this.numberOfTransactions = builder.numberOfTransactions;
        this.initiatingParty = builder.initiatingParty;
        this.paymentTypeInformation = builder.paymentTypeInformation;
        this.debtor = builder.debtor;
        this.creditor = builder.creditor;
        this.creditorAccount = builder.creditorAccount;
        this.ultimateCreditor = builder.ultimateCreditor;
        this.purpose = builder.purpose;
        this.chargeBearer = builder.chargeBearer;
        this.creditTransferTransaction = builder.creditTransferTransaction;
        this.supplementaryData = builder.supplementaryData;
    }

    public CreatePaymentRequest() {}

    // TODO: Responses doesn't return status so it must be hardcoded
    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(boolean paid) {
        CreditTransferTransactionEntity creditTransferTransactionEntity = getTransactionFromList();

        LocalDate executionDate =
                LocalDate.parse(
                        creditTransferTransactionEntity
                                .getRequestedExecutionDate()
                                .substring(0, 10));

        InstructedAmountEntity instructedAmount =
                creditTransferTransactionEntity.getInstructedAmount();

        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        instructedAmount.getAmount(), instructedAmount.getCurrency());

        return new PaymentResponse(
                new Payment.Builder()
                        .withExecutionDate(executionDate)
                        .withCreditor(new Creditor(new IbanIdentifier(creditorAccount.getIban())))
                        .withExactCurrencyAmount(amount)
                        .withCurrency(amount.getCurrencyCode())
                        .withStatus(paid ? PaymentStatus.PAID : PaymentStatus.PENDING)
                        .build());
    }

    @JsonIgnore
    private CreditTransferTransactionEntity getTransactionFromList() {
        return Optional.ofNullable(creditTransferTransaction).orElse(Lists.newArrayList()).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.PAYMENT_NOT_FOUND));
    }

    public static class Builder {
        private String paymentInformationId;
        private String creationDateTime;
        private double numberOfTransactions;
        private InitiatingPartyEntity initiatingParty;
        private PaymentTypeInformationEntity paymentTypeInformation;
        private DebtorEntity debtor;
        private CreditorEntity creditor;
        private CreditorAccountEntity creditorAccount;
        private CreditorEntity ultimateCreditor;
        private String purpose;
        private String chargeBearer;
        private List<CreditTransferTransactionEntity> creditTransferTransaction;
        private SupplementaryDataEntity supplementaryData;

        public Builder withPaymentInformationId(String paymentInformationId) {
            this.paymentInformationId = paymentInformationId;
            return this;
        }

        public Builder withCreationDateTime(String creationDateTime) {
            this.creationDateTime = creationDateTime;
            return this;
        }

        public Builder withNumberOfTransactions(double numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
            return this;
        }

        public Builder withInitiatingParty(InitiatingPartyEntity initiatingParty) {
            this.initiatingParty = initiatingParty;
            return this;
        }

        public Builder withPaymentTypeInformation(
                PaymentTypeInformationEntity paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
            return this;
        }

        public Builder withDebtor(DebtorEntity debtor) {
            this.debtor = debtor;
            return this;
        }

        public Builder withCreditor(CreditorEntity creditor) {
            this.creditor = creditor;
            return this;
        }

        public Builder withCreditorAccount(CreditorAccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withUltimateCreditor(CreditorEntity ultimateCreditor) {
            this.ultimateCreditor = ultimateCreditor;
            return this;
        }

        public Builder withPurpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder withChargeBearer(String chargeBearer) {
            this.chargeBearer = chargeBearer;
            return this;
        }

        public Builder withCreditTransferTransaction(
                List<CreditTransferTransactionEntity> creditTransferTransaction) {
            this.creditTransferTransaction = creditTransferTransaction;
            return this;
        }

        public Builder withSupplementaryData(SupplementaryDataEntity supplementaryData) {
            this.supplementaryData = supplementaryData;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
