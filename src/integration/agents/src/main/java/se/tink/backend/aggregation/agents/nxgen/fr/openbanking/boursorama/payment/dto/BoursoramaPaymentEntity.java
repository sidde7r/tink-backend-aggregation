package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BoursoramaPaymentEntity {

    private String paymentInformationStatus;
    private String statusReasonInformation;
    private PaymentTypeInformationEntity paymentTypeInformation;
    private AccountEntity debtorAccount;
    private BeneficiaryEntity beneficiary;
    private List<BoursoramaCreditTransferTransactionEntity> creditTransferTransaction;
}
