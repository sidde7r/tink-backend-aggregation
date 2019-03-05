package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

import java.util.Optional;

@JsonObject
public class InvestmentAccountEntity {
    @JsonProperty("contrato")
    private ContractEntity contract;

    @JsonProperty("saldoInformado")
    private Boolean informedBalance;

    @JsonProperty("saldoDisponible")
    private AmountEntity availableBalance;

    @JsonIgnore
    public boolean isAccountTypeInvestment() {
        Optional<AccountTypes> accountType =
                BankiaConstants.AccountType.translateType(contract.getProductCode());
        boolean isInvestmentAccount =
                accountType.map(InvestmentAccount.ALLOWED_ACCOUNT_TYPES::contains).orElse(false);

        boolean hasInternalProductIdentifier =
                !Strings.isNullOrEmpty(contract.getIdentifierProductContractInternal());

        return isInvestmentAccount && hasInternalProductIdentifier;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public Boolean isInformedBalance() {
        return informedBalance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }
}
