package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.rpc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.BnpParibasFortisConstants.PaymentRequestValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.InitiatingPartyEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums.BnpParibasFortisPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class CreatePaymentRequest {
    private String paymentInformationId;
    private String creationDateTime;
    private String requestedExecutionDate;
    private Integer numberOfTransactions;
    private InitiatingPartyEntity initiatingParty;
    private PaymentTypeInformationEntity paymentTypeInformation;
    private AccountEntity debtorAccount;
    private BeneficiaryEntity beneficiary;
    private List<CreditTransferTransactionEntity> creditTransferTransaction;
    private SupplementaryDataEntity supplementaryData;

    private CreatePaymentRequest(Builder builder) {
        paymentInformationId = RandomStringUtils.random(35, true, true);
        creationDateTime = builder.creationDateTime;
        requestedExecutionDate = builder.requestedExecutionDate;
        numberOfTransactions = PaymentRequestValues.NUMBER_OF_TRANSACTIONS;
        initiatingParty = new InitiatingPartyEntity(PaymentRequestValues.INITIATING_PARTY);
        paymentTypeInformation = new PaymentTypeInformationEntity(builder.paymentType.toString());
        debtorAccount = builder.debtorAccount;
        beneficiary = new BeneficiaryEntity(builder.creditorName, builder.creditorAccount);
        creditTransferTransaction =
                Arrays.asList(
                        new CreditTransferTransactionEntity(
                                builder.amount, builder.remittanceInformation));
        supplementaryData = new SupplementaryDataEntity(builder.redirectUrl);
    }

    public String getPaymentId() {
        return paymentInformationId;
    }

    public static class Builder {
        private String creationDateTime;
        private String requestedExecutionDate;
        private String creditorName;
        private AccountEntity creditorAccount;
        private AccountEntity debtorAccount;
        private AmountEntity amount;
        private BnpParibasFortisPaymentType paymentType;
        private String redirectUrl;
        private String remittanceInformation;

        public Builder withPaymentType(BnpParibasFortisPaymentType paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public Builder withAmount(AmountEntity amount) {
            this.amount = amount;
            return this;
        }

        public Builder withCreditorAccount(AccountEntity creditorAccount) {
            this.creditorAccount = creditorAccount;
            return this;
        }

        public Builder withCreditorName(String creditorName) {
            this.creditorName = creditorName;
            return this;
        }

        public Builder withDebtorAccount(AccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withCreationDateTime(LocalDateTime dateTime) {
            this.creationDateTime = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            return this;
        }

        public Builder withExecutionDate(LocalDate date) {
            requestedExecutionDate = date.format(DateTimeFormatter.ISO_DATE);
            return this;
        }

        public Builder withRedirectUrl(URL redirectUrl) {
            this.redirectUrl = redirectUrl.toString();
            return this;
        }

        public Builder withRemittanceInformation(String remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
