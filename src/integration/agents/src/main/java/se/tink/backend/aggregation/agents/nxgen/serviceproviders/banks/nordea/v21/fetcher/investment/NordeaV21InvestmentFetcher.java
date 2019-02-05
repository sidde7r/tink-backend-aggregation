package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.NordeaV21Parser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.agents.rpc.AccountTypes;

public class NordeaV21InvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final NordeaV21ApiClient client;
    private final NordeaV21Parser parser;

    public NordeaV21InvestmentFetcher(NordeaV21ApiClient client, NordeaV21Parser parser) {
        this.client = client;
        this.parser = parser;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> accounts = client.getAccountProductsOfTypes(NordeaV21Constants.ProductType.ACCOUNT).stream()
                .filter(pe -> {
                    AccountTypes accountType = parser.getTinkAccountType(pe);
                    return InvestmentAccount.ALLOWED_ACCOUNT_TYPES.contains(accountType);
                }).map(parser::parseInvestmentAccount)
                .collect(Collectors.toList());

        List<CustodyAccount> custodyAccounts = client.fetchCustodyAccounts();

        if (custodyAccounts == null) {
            return Collections.emptyList();
        }

        accounts.addAll(custodyAccounts.stream()
                .filter(Objects::nonNull)
                .map(parser::parseInvestmentAccount)
                .collect(Collectors.toList()));

        return accounts;
    }
}
