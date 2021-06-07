package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.AccountIdentifier.BANK_GIRO_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.AccountIdentifier.PLUS_GIRO_TYPE;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.REFERENCE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.TinkCreditorConstructor;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
@JsonObject
@Getter
public class DomesticGirosPaymentRequest {

    private final String endToEndIdentification;
    private final AccountEntity debtorAccount;
    private final GiroCreditorAccount creditorAccount;
    private final AmountEntity instructedAmount;
    private final String requestedExecutionDate;
    private final List<String> remittanceInformationUnstructuredArray;
    private final List<RemittanceInformationStructuredEntity> remittanceInformationStructuredArray;

    public DomesticGirosPaymentRequest(
            AccountEntity debtorAccount, AmountEntity instructedAmount, Payment payment) {
        endToEndIdentification = RandomUtils.generateRandomAlphabeticString(35);
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
        this.requestedExecutionDate = payment.getExecutionDate().toString();
        this.creditorAccount = new GiroCreditorAccount(payment.getCreditor());

        switch (payment.getRemittanceInformation().getType()) {
            case REFERENCE:
                throw new IllegalStateException(
                        REFERENCE + " remittance information not implemented.");
            case OCR:
                this.remittanceInformationUnstructuredArray = null;
                this.remittanceInformationStructuredArray =
                        RemittanceInformationStructuredEntity.singleFrom(payment);
                break;
            case UNSTRUCTURED:
                this.remittanceInformationUnstructuredArray =
                        remittanceInformationUnstructuredSingleFrom(payment);
                this.remittanceInformationStructuredArray = null;
                break;
            default:
                this.remittanceInformationUnstructuredArray = null;
                this.remittanceInformationStructuredArray = null;
        }
    }

    private static List<String> remittanceInformationUnstructuredSingleFrom(Payment payment) {
        return Collections.singletonList(payment.getRemittanceInformation().getValue());
    }

    @JsonObject
    @Getter
    static class GiroCreditorAccount implements TinkCreditorConstructor {

        private final AccountReferenceGiroCreditor accountReferenceOther;

        public GiroCreditorAccount(Creditor creditor) {
            this.accountReferenceOther = new AccountReferenceGiroCreditor(creditor);
        }

        @Override
        public Creditor toTinkCreditor() {
            return new Creditor(
                    AccountIdentifier.create(
                            accountReferenceOther.identificationType.accountIdentifierType,
                            accountReferenceOther.identification));
        }

        @JsonObject
        static class AccountReferenceGiroCreditor {

            private final String identification;
            private final IdentificationType identificationType;

            public AccountReferenceGiroCreditor(Creditor creditor) {
                identification = creditor.getAccountIdentifier().getIdentifier();
                identificationType = IdentificationType.from(creditor.getAccountIdentifierType());
            }

            @RequiredArgsConstructor
            public enum IdentificationType {
                BANK_GIRO(AccountIdentifierType.SE_BG, BANK_GIRO_TYPE),
                PLUS_GIRO(AccountIdentifierType.SE_PG, PLUS_GIRO_TYPE);

                private static final GenericTypeMapper<IdentificationType, AccountIdentifierType>
                        IDENTIFIER_TYPE_MAPPER =
                                GenericTypeMapper
                                        .<IdentificationType, AccountIdentifierType>genericBuilder()
                                        .put(BANK_GIRO, AccountIdentifierType.SE_BG)
                                        .put(PLUS_GIRO, AccountIdentifierType.SE_PG)
                                        .build();
                private final AccountIdentifierType accountIdentifierType;
                private final String value;

                public static IdentificationType from(AccountIdentifierType accountIdentifierType) {
                    return IDENTIFIER_TYPE_MAPPER
                            .translate(accountIdentifierType)
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    ErrorMessages.UNSUPPORTED_PAYMENT_TYPE));
                }

                @JsonValue
                public String toValue() {
                    return this.value;
                }
            }
        }
    }
}
