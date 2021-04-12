package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.PaymentTypeInformation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities.RemittanceInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.InitiatingPartyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payments.common.model.PaymentScheme;

@Getter
@JsonObject
public class CreatePaymentRequest {

    private String paymentInformationId;

    private String creationDateTime;

    private String requestedExecutionDate;

    private Integer numberOfTransactions;

    private InitiatingPartyEntity initiatingParty;

    private PaymentTypeInformationEntity paymentTypeInformation;

    private BeneficiaryEntity beneficiary;

    private List<CreditTransferTransactionEntity> creditTransferTransaction;

    private String chargeBearer;

    private SupplementaryDataEntity supplementaryData;

    private CreatePaymentRequest(Builder builder) {
        paymentInformationId = RandomStringUtils.random(35, true, true);
        creationDateTime = builder.creationDateTime;
        requestedExecutionDate = builder.requestedExecutionDate;
        numberOfTransactions = 1;
        initiatingParty = new InitiatingPartyEntity(LaBanquePostaleConstants.DEVICE_NAME);
        paymentTypeInformation =
                new PaymentTypeInformationEntity(
                        builder.paymentType.toString().toUpperCase(),
                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == builder.paymentScheme
                                ? PaymentTypeInformation.SEPA_INSTANT_CREDIT_TRANSFER
                                : PaymentTypeInformation.SEPA_STANDARD_CREDIT_TRANSFER,
                        PaymentTypeInformation.CATEGORY_PURPOSE);
        beneficiary =
                new BeneficiaryEntity(
                        builder.creditorAgentEntity,
                        builder.creditorEntity,
                        builder.creditorAccount);
        creditTransferTransaction =
                Collections.singletonList(
                        new CreditTransferTransactionEntity(
                                new PaymentIdEntity(),
                                builder.requestedExecutionDate,
                                builder.amount,
                                builder.remittanceInformation,
                                beneficiary));
        chargeBearer = LaBanquePostaleConstants.CHANGE_BEARER;
        supplementaryData = new SupplementaryDataEntity(builder.redirectUrl);
    }

    public static class Builder {
        private static final DateTimeFormatter DATE_TIME_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        private String creationDateTime;
        private String requestedExecutionDate;
        private CreditorAgentEntity creditorAgentEntity;
        private CreditorEntity creditorEntity;
        private AccountEntity creditorAccount;
        private AmountEntity amount;
        private PaymentType paymentType;
        private String redirectUrl;
        private RemittanceInformationEntity remittanceInformation;
        private PaymentScheme paymentScheme;

        public Builder withPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public Builder withAmount(AmountEntity amount) {
            this.amount = amount;
            return this;
        }

        public Builder withCreditorAgentEntity(CreditorAgentEntity creditorAgentEntity) {
            this.creditorAgentEntity = creditorAgentEntity;
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

        public Builder withCreationDateTime(LocalDateTime dateTime) {
            this.creationDateTime = dateTime.atZone(ZoneId.of("CET")).format(DATE_TIME_FORMATTER);
            return this;
        }

        public Builder withExecutionDate(LocalDate date) {
            // some banks don't accept date at start day, adding one minute solves the issue
            requestedExecutionDate =
                    date.atStartOfDay(ZoneId.of("CET")).plusMinutes(1).format(DATE_TIME_FORMATTER);
            return this;
        }

        public Builder withRedirectUrl(URL redirectUrl) {
            this.redirectUrl = redirectUrl.toString();
            return this;
        }

        public Builder withRemittanceInformation(
                RemittanceInformationEntity remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
            return this;
        }

        public Builder withPaymentScheme(PaymentScheme paymentScheme) {
            this.paymentScheme = paymentScheme;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }
}
