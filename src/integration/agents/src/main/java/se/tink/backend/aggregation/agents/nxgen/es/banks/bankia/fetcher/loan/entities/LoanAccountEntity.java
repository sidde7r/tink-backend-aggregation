package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

@JsonObject
public class LoanAccountEntity {
    @JsonProperty("contrato")
    private ContractEntity contract;
    @JsonProperty("saldoInformado")
    private boolean informedBalance;
    @JsonProperty("capitalConcedido")
    private AmountEntity grantedAmount;
    @JsonProperty("deudaPendiente")
    private AmountEntity pendingDebt;

    @JsonIgnore
    public String getProductCode() {
        return contract.getProductCode();
    }

    @JsonIgnore
    public String getLoanIdentifier() {
        return contract.getIdentifierProductContract();
    }

    public AmountEntity getGrantedAmount() {
        return grantedAmount;
    }

    public AmountEntity getPendingDebt() {
        return pendingDebt;
    }

    @JsonIgnore
    public LoanAccount toTinkLoanAccount(LoanDetailsResponse loanDetailsResponse) {
        LoanDetails details = loanDetailsResponse.toLoanDetails(this);
        String contractNumber = contract.getIdentifierProductContract();

        return LoanAccount.builder(contractNumber)
                .setAccountNumber(contractNumber)
                .setName(loanDetailsResponse.getLoanName())
                .setBalance(pendingDebt.toTinkAmount().negate())
                // NB! interest rates are hard to understand in Spain, we have 4 of them in the detailed response
                .setInterestRate(loanDetailsResponse.getInterestRate())
                .setHolderName(loanDetailsResponse.getHolderName())
                .setDetails(details)
                .build();
    }
}
