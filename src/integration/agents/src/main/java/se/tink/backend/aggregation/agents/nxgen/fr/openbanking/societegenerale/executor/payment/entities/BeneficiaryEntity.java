package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BeneficiaryEntity {

    private PartyIdentificationEntity creditor;
    private CreditorAccountEntity creditorAccount;
    private CreditorAgentEntity creditorAgent;

    @JsonIgnore
    public static BeneficiaryEntity of(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        return BeneficiaryEntity.builder()
                .creditor(new PartyIdentificationEntity(payment.getCreditor().getName()))
                .creditorAccount(
                        new CreditorAccountEntity(payment.getCreditor().getAccountNumber()))
                .creditorAgent(
                        new CreditorAgentEntity(
                                SocieteGeneraleConstants.FormValues.BANK_BICFI_CODE))
                .build();
    }
}
