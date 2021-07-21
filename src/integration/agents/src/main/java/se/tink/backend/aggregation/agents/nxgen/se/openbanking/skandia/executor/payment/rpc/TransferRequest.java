package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import static se.tink.libraries.transfer.enums.RemittanceInformationType.UNSTRUCTURED;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

@JsonObject
@Getter
final class TransferRequest extends BasePaymentRequest {

    private final CreditorAccountEntity creditorAccount;
    private final List<RemittanceInformationStructuredEntity> remittanceInformationStructuredArray;

    TransferRequest(Payment payment) {
        super(payment);
        this.creditorAccount = new CreditorAccountEntity(payment.getCreditor().getAccountNumber());

        final RemittanceInformationType remittanceInformationType =
                mapRemittanceInformationType(payment);

        if (UNSTRUCTURED.equals(remittanceInformationType)) {
            this.remittanceInformationStructuredArray =
                    RemittanceInformationStructuredEntity.singleFrom(payment);
        } else {
            throw new IllegalStateException(ErrorMessages.UNSUPPORTED_REMITTANCE_INFORMATION);
        }
    }
}
