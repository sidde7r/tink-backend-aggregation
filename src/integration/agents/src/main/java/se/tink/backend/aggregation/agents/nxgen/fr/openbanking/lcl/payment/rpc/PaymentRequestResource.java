package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util.CreditTransferTransactionUtil;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util.DateUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.InitiatingPartyEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
public class PaymentRequestResource {

    private String paymentInformationId;

    private String creationDateTime;

    private String requestedExecutionDate;

    private Integer numberOfTransactions;

    private PaymentTypeInformationEntity paymentTypeInformation;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = AccountEntity.class)
    private AccountEntity debtorAccount;

    private BeneficiaryEntity beneficiary;

    private List<LclCreditTransferTransactionEntity> creditTransferTransaction;

    private String chargeBearer;

    private SupplementaryDataEntity supplementaryData;

    private InitiatingPartyEntity initiatingParty;

    private String paymentInformationStatus;

    public PaymentRequestResource(CreatePaymentRequest paymentRequest) {
        this.paymentInformationId = paymentRequest.getPaymentInformationId();
        this.creationDateTime = paymentRequest.getCreationDateTime();
        this.numberOfTransactions = paymentRequest.getNumberOfTransactions();
        this.paymentTypeInformation = paymentRequest.getPaymentTypeInformation();
        this.debtorAccount = paymentRequest.getDebtorAccount();
        this.beneficiary = paymentRequest.getBeneficiary();
        this.creditTransferTransaction =
                CreditTransferTransactionUtil.convertList(
                        paymentRequest.getCreditTransferTransaction());
        this.chargeBearer = paymentRequest.getChargeBearer();
        this.supplementaryData = paymentRequest.getSupplementaryData();
        this.initiatingParty = paymentRequest.getInitiatingParty();
        this.requestedExecutionDate = DateUtil.getExecutionDate(paymentRequest);
    }

    public GetPaymentResponse toPaymentResponse() {
        PaymentEntity paymentEntity =
                new PaymentEntity(
                        this.paymentInformationStatus,
                        this.paymentTypeInformation,
                        this.debtorAccount,
                        this.beneficiary,
                        CreditTransferTransactionUtil.toBaseList(this.creditTransferTransaction));
        return new GetPaymentResponse(paymentEntity);
    }
}
