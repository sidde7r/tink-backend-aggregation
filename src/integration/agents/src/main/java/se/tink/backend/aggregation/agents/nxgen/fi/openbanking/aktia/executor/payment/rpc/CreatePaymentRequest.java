package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.PaymentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {

    private PaymentAccountEntity creditorAccount;
    private String creditorName;
    private PaymentAccountEntity debtorAccount;
    private String endToEndIdentification;
    private AmountEntity instructedAmount;
    private String remittanceInformationUnstructured;
    private String requestedExecutionDate;

    public CreatePaymentRequest(
            PaymentAccountEntity creditorAccount,
            String creditorName,
            PaymentAccountEntity debtorAccount,
            String endToEndIdentification,
            AmountEntity instructedAmount,
            String remittanceInformationUnstructured,
            String requestedExecutionDate) {
        this.creditorAccount = creditorAccount;
        this.creditorName = creditorName;
        this.debtorAccount = debtorAccount;
        this.endToEndIdentification = endToEndIdentification;
        this.instructedAmount = instructedAmount;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.requestedExecutionDate = requestedExecutionDate;
    }
}
