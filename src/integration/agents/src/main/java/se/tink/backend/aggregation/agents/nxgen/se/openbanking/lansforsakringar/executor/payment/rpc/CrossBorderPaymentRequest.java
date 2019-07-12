package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CrossBorderPaymentRequest {
    private AccountIbanEntity creditorAccountIban;
    private CreditorAddressEntity creditorAddress;
    private String creditorName;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String paymentType;
    private String requestedExecutionDate;

    public CrossBorderPaymentRequest(
            AccountIbanEntity creditorAccountIban,
            CreditorAddressEntity creditorAddress,
            String creditorName,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount,
            String paymentType,
            String requestedExecutionDate) {
        this.creditorAccountIban = creditorAccountIban;
        this.creditorAddress = creditorAddress;
        this.creditorName = creditorName;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
        this.paymentType = paymentType;
        this.requestedExecutionDate = requestedExecutionDate;
    }
}
