package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import static se.tink.libraries.transfer.enums.RemittanceInformationType.OCR;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.REFERENCE;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@EqualsAndHashCode
@JsonObject
@Getter
public class DomesticPaymentRequest {

    public DomesticPaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount,
            Payment payment) {
        endToEndIdentification = RandomUtils.generateRandomAlphabeticString(35);
        this.debtorAccount = debtorAccount;
        this.creditorAccount = creditorAccount;
        this.instructedAmount = instructedAmount;
        this.requestedExecutionDate = payment.getExecutionDate().toString();

        switch (payment.getRemittanceInformation().getType()) {
            case REFERENCE:
                throw new IllegalStateException(
                        REFERENCE + " remittance information not implemented.");
            case OCR:
                throw new IllegalStateException(OCR + " remittance information not implemented.");
            case UNSTRUCTURED:
                this.remittanceInformationStructuredArray =
                        RemittanceInformationStructuredEntity.singleFrom(payment);
                break;
            default:
                this.remittanceInformationStructuredArray = null;
        }
    }

    private final String endToEndIdentification;
    private final AccountEntity debtorAccount;
    private final AccountEntity creditorAccount;
    private final AmountEntity instructedAmount;
    private final String requestedExecutionDate;
    private final List<RemittanceInformationStructuredEntity> remittanceInformationStructuredArray;
}
