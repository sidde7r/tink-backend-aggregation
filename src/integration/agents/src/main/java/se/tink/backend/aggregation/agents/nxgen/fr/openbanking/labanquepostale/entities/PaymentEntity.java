package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.enums.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class PaymentEntity {

    private String paymentInformationStatus;
    private AccountEntity debtorAccount;
    private String resourceId;
    private BeneficiaryEntity beneficiary;
    private List<CreditTransferTransactionEntity> creditTransferTransaction;
    private SupplementaryDataEntity supplementaryData;

    public PaymentStatus getPaymentStatus() {

        BerlinGroupPaymentStatus berlinPaymentStatus =
                BerlinGroupPaymentStatus.fromString(paymentInformationStatus);

        return berlinPaymentStatus.getTinkPaymentStatus().equals(PaymentStatus.PAID)
                ? PaymentStatus.SIGNED
                : berlinPaymentStatus.getTinkPaymentStatus();
    }

    public AmountEntity getAmountFromResponse() {
        return creditTransferTransaction.stream()
                .findFirst()
                .map(CreditTransferTransactionEntity::getAmount)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Amount cannot be extracted from the response"));
    }
}
