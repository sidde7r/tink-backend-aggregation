package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.rpc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.InitiatingPartyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.PaymentIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.enums.BnpParibasPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

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
        numberOfTransactions = 1;
        initiatingParty = new InitiatingPartyEntity("TINK");
        paymentTypeInformation = new PaymentTypeInformationEntity(builder.paymentType.toString());
        debtorAccount = builder.debtorAccount;
        beneficiary = new BeneficiaryEntity(builder.creditorEntity, builder.creditorAccount);
        creditTransferTransaction =
                Collections.singletonList(
                        new CreditTransferTransactionEntity(
                                new PaymentIdEntity(),
                                builder.amount,
                                Collections.singletonList(builder.remittanceInformation)));
        supplementaryData = new SupplementaryDataEntity(builder.redirectUrl);
    }

    public String getPaymentId() {
        return paymentInformationId;
    }

    public static class Builder {
        private String creationDateTime;
        private String requestedExecutionDate;
        private CreditorEntity creditorEntity;
        private AccountEntity creditorAccount;
        private AccountEntity debtorAccount;
        private AmountEntity amount;
        private BnpParibasPaymentType paymentType;
        private String redirectUrl;
        private String remittanceInformation;

        public Builder withPaymentType(BnpParibasPaymentType paymentType) {
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

        public Builder withCreditorName(CreditorEntity creditorEntity) {
            this.creditorEntity = creditorEntity;
            return this;
        }

        public Builder withDebtorAccount(AccountEntity debtorAccount) {
            this.debtorAccount = debtorAccount;
            return this;
        }

        public Builder withCreationDateTime(LocalDateTime dateTime) {
            this.creationDateTime =
                    dateTime.atZone(ZoneId.of("CET"))
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return this;
        }

        public Builder withExecutionDate(LocalDate date) {
            requestedExecutionDate =
                    date.atStartOfDay(ZoneId.of("CET"))
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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
