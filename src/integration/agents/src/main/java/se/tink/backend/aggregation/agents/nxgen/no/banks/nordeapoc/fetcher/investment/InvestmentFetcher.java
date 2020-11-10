package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.entity.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.entity.InvestmentEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.entity.InvestmentHoldingEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
public class InvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private FetcherClient fetcherClient;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return fetcherClient.fetchInvestments().getAccounts().stream()
                .map(this::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }

    private InvestmentAccount toTinkInvestmentAccount(InvestmentEntity account) {

        List<InstrumentModule> instruments =
                account.getHoldings().stream()
                        .map(this::toTinkInstrument)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(account.getId())
                        .withAccountNumber(account.getAccountNumber())
                        .withAccountName(account.getName())
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.COUNTRY_SPECIFIC, account.getId()))
                        .build();
        PortfolioModule portfolioModule =
                PortfolioModule.builder()
                        .withType(PortfolioModule.PortfolioType.DEPOT)
                        .withUniqueIdentifier(account.getId())
                        .withCashValue(account.getCashAmount())
                        .withTotalProfit(account.getProfit())
                        .withTotalValue(account.getMarketValue())
                        .withInstruments(instruments)
                        .build();

        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolioModule)
                .withCashBalance(
                        ExactCurrencyAmount.of(account.getCashAmount(), account.getCurrency()))
                .withId(idModule)
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.NO)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.NO)
                .build();
    }

    private Optional<InstrumentModule> toTinkInstrument(InvestmentHoldingEntity holdingEntity) {
        InstrumentEntity instrument = holdingEntity.getInstrument();
        if (instrument.isCash()) {
            return Optional.empty();
        }
        InstrumentIdBuildStep builder =
                InstrumentIdModule.builder()
                        .withUniqueIdentifier(holdingEntity.getId())
                        .withName(holdingEntity.getName());
        if (instrument.isFund()) {
            builder = builder.setIsin(instrument.getIsin());
        }
        InstrumentIdModule idModule = builder.build();
        return Optional.of(
                InstrumentModule.builder()
                        .withType(getType(instrument))
                        .withId(idModule)
                        .withMarketPrice(instrument.getPrice())
                        .withMarketValue(holdingEntity.getMarketValue())
                        .withAverageAcquisitionPrice(holdingEntity.getAvgPurchasePrice())
                        .withCurrency(holdingEntity.getCurrency())
                        .withQuantity(holdingEntity.getQuantity())
                        .withProfit(holdingEntity.getProfit())
                        .setRawType(instrument.getInstrumentType())
                        .build());
    }

    private InstrumentModule.InstrumentType getType(InstrumentEntity instrument) {
        if (instrument.isFund()) {
            return InstrumentModule.InstrumentType.FUND;
        }
        if (instrument.isDerivative() || instrument.isEquity()) {
            return InstrumentModule.InstrumentType.STOCK;
        }
        return InstrumentModule.InstrumentType.OTHER;
    }
}
