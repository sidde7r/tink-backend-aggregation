package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import io.vavr.control.Option;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BbvaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final BbvaApiClient apiClient;

    public BbvaCreditCardFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient
                .fetchFinancialDashboard()
                .getPositions()
                .map(PositionEntity::getContract)
                .map(ContractEntity::getCreditCard)
                .filter(Option::isDefined)
                .map(Option::get)
                .filter(CreditCardEntity::isNotComplementaryCard)
                .map(CreditCardEntity::toTinkCreditCard)
                .asJava();
    }
}
