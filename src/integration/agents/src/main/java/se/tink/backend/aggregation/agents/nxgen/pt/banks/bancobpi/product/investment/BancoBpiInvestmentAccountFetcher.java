package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoBpiInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private BancoBpiClientApi clientApi;

    public BancoBpiInvestmentAccountFetcher(BancoBpiClientApi clientApi) {
        this.clientApi = clientApi;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            List<BancoBpiProductData> investments =
                    clientApi.getProductsByType(BancoBpiProductType.DEPOSIT);
            return mapToInvestmentAccount(investments);
        } catch (RequestException e) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private List<InvestmentAccount> mapToInvestmentAccount(List<BancoBpiProductData> investments) {
        List<InvestmentAccount> accounts = new ArrayList<>(investments.size());
        for (BancoBpiProductData p : investments) {
            accounts.add(
                    InvestmentAccount.nxBuilder()
                            .withPortfolios(buildDepositPortfolio(p))
                            .withCashBalance(
                                    ExactCurrencyAmount.of(p.getBalance(), p.getCurrencyCode()))
                            .withId(
                                    IdModule.builder()
                                            .withUniqueIdentifier(p.getNumber())
                                            .withAccountNumber(p.getNumber())
                                            .withAccountName(p.getName())
                                            .addIdentifier(
                                                    AccountIdentifier.create(
                                                            AccountIdentifierType.PT_BPI,
                                                            p.getNumber()))
                                            .build())
                            .addHolderName(p.getOwner())
                            .build());
        }
        return accounts;
    }

    private PortfolioModule buildDepositPortfolio(BancoBpiProductData deposit) {
        return PortfolioModule.builder()
                .withType(PortfolioType.DEPOT)
                .withUniqueIdentifier(deposit.getNumber())
                .withCashValue(deposit.getBalance().doubleValue())
                .withTotalProfit(0.0)
                .withTotalValue(0)
                .withoutInstruments()
                .build();
    }
}
