package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FabricPaymentRequest {

    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;

    public static FabricPaymentRequest createFrom(PaymentRequest paymentRequest) {
        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        FabricPaymentRequestBuilder createPaymentRequest =
                FabricPaymentRequest.builder()
                        .debtorAccount(debtorEntity)
                        .instructedAmount(instructedAmountEntity)
                        .creditorAccount(creditorEntity)
                        .creditorName(paymentRequest.getPayment().getCreditor().getName())
                        .remittanceInformationUnstructured(remittanceInformation.getValue());
        return createPaymentRequest.build();
    }
}
