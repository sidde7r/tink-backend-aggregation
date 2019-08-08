package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class InvestmentAccountEntity {

    @JsonProperty("contrato")
    private ContractEntity contract;

    @JsonProperty("saldoInformado")
    private Boolean informedBalance;

    @JsonProperty("saldoDisponible")
    private AmountEntity availableBalance;

    @JsonIgnore
    private Predicate<ContractEntity> hasInternalProductIdentifier() {
        return c -> !Strings.isNullOrEmpty(c.getIdentifierProductContractInternal());
    }

    @JsonIgnore
    public boolean isAccountTypeInvestment() {
        return Optional.ofNullable(contract)
                .filter(hasInternalProductIdentifier())
                .map(ContractEntity::getProductCode)
                .flatMap(ACCOUNT_TYPE_MAPPER::translate)
                .filter(InvestmentAccount.ALLOWED_ACCOUNT_TYPES::contains)
                .isPresent();
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
