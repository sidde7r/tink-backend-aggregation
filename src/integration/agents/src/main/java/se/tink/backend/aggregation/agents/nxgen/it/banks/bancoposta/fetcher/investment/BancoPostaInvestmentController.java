package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.entity.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.rpc.InvestmentResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
@Slf4j
public class BancoPostaInvestmentController implements AccountFetcher<InvestmentAccount> {
    private final BancoPostaApiClient apiClient;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        InvestmentResponse investmentResponse = apiClient.fetchInvestments();

        if (investmentResponse.isInvestmentAvailable()) {
            // current imlpementation of investments is poor due to not sufficent enough ambassador
            // data, ticket ITE-1561 created to monitor situation on prod
            log.info("Investments are available for that user");

            return investmentResponse.getBody().getInvestmentAccounts().stream()
                    .flatMap(
                            portfolio ->
                                    portfolio.getInvestments().stream()
                                            .map(investment -> toTinkInvestmentAccount(investment)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public InvestmentAccount toTinkInvestmentAccount(InvestmentAccountEntity accountEntity) {
        IdModule idModule = getIdModule(accountEntity.getId(), accountEntity.getDescription());
        BigDecimal currentBalance = accountEntity.getCurrentValue().movePointLeft(2);
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(ExactCurrencyAmount.of(currentBalance, "EUR"))
                .withId(idModule)
                .build();
    }

    private IdModule getIdModule(String id, String description) {
        return IdModule.builder()
                .withUniqueIdentifier(id)
                .withAccountNumber(id)
                .withAccountName(description)
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.COUNTRY_SPECIFIC, id))
                .build();
    }
}
