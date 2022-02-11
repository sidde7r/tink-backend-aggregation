package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.InitiatingPartyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@JsonObject
public class BoursoramaCreatePaymentRequest {

    private final String paymentInformationId;
    private final String creationDateTime;
    private final String requestedExecutionDate;
    private final Integer numberOfTransactions;
    private final InitiatingPartyEntity initiatingParty;
    private final PaymentTypeInformationEntity paymentTypeInformation;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = AccountEntity.class)
    private final AccountEntity debtorAccount;

    private final BeneficiaryEntity beneficiary;
    private final List<BoursoramaCreditTransferTransactionEntity> creditTransferTransaction;
    private final String chargeBearer;
    private final SupplementaryDataEntity supplementaryData;
}
