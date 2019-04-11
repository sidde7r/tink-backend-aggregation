package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {

    @JsonProperty("Productos")
    private List<AccountEntity> accounts;

    public List<TransactionalAccount> getAccounts() {
        return accounts.stream()
                .filter(
                        entity ->
                                IberCajaConstants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(
                                        entity.getType()))
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    public List<InvestmentAccount> getInvestmentAccounts() {
        return accounts.stream()
                .filter(
                        entity ->
                                IberCajaConstants.ACCOUNT_TYPE_MAPPER.isInvestmentAccount(
                                        entity.getType()))
                .map(AccountEntity::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }

    public List<CreditCardAccount> getCreditCardAccounts() {
        return accounts.stream()
                .filter(
                        entity ->
                                IberCajaConstants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(
                                        entity.getType()))
                .filter(
                        entity ->
                                IberCajaConstants.CARD_TYPE_MAPPER.isCreditCardAccount(
                                        entity.getTypeCard()))
                .map(AccountEntity::toTinkCreditCardAccount)
                .collect(Collectors.toList());
    }
}
