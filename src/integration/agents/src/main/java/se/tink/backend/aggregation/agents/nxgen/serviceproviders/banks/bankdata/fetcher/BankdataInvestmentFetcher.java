package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataMapperUtils.getAccountNumberToDisplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAssetEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataDepositEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataPoolAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.AssetDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.DepositsContentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BankdataInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final BankdataApiClient bankClient;

    public BankdataInvestmentFetcher(BankdataApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> accounts = new ArrayList<>();
        accounts.addAll(fetchDeposits());
        accounts.addAll(fetchPoolAccounts());
        return accounts;
    }

    private List<InvestmentAccount> fetchPoolAccounts() {
        return this.bankClient.fetchPoolAccounts().stream()
                .filter(BankdataPoolAccountEntity::isMarketCurrency)
                .filter(BankdataPoolAccountEntity::isKnownType)
                .map(BankdataPoolAccountEntity::toTinkInvestment)
                .collect(Collectors.toList());
    }

    private List<InvestmentAccount> fetchDeposits() {
        List<BankdataDepositEntity> depositEntities = this.bankClient.fetchDeposits();
        return depositEntities.stream()
                .map(
                        deposit -> {
                            DepositsContentListResponse depositContents =
                                    this.bankClient.fetchDepositContents(
                                            deposit.getRegNo(), deposit.getDepositNo());

                            // One deposit maps to one Portfolio
                            Portfolio portfolio = deposit.toTinkPortfolio();
                            portfolio.setInstruments(
                                    collectDepositInstruments(deposit, depositContents));

                            return InvestmentAccount.builder(deposit.getBban())
                                    .setCashBalance(ExactCurrencyAmount.inDKK(0))
                                    .setAccountNumber(
                                            getAccountNumberToDisplay(
                                                    deposit.getRegNo(), deposit.getDepositNo()))
                                    .setBankIdentifier(deposit.getBban())
                                    .setName(deposit.getName())
                                    .setPortfolios(Collections.singletonList(portfolio))
                                    .build();
                        })
                .collect(Collectors.toList());
    }

    private List<Instrument> collectDepositInstruments(
            BankdataDepositEntity deposit, DepositsContentListResponse depositContents) {
        List<Instrument> instruments = new ArrayList<>();
        instruments.addAll(assetListToInstruments(deposit, depositContents.getDanishStocks()));
        instruments.addAll(assetListToInstruments(deposit, depositContents.getDanishBonds()));
        instruments.addAll(assetListToInstruments(deposit, depositContents.getForeignStocks()));
        instruments.addAll(assetListToInstruments(deposit, depositContents.getForeignBonds()));
        return instruments;
    }

    private List<Instrument> assetListToInstruments(
            BankdataDepositEntity deposit, List<BankdataAssetEntity> assetList) {
        return assetList.stream()
                .filter(BankdataAssetEntity::isKnownType)
                .map(
                        instrument -> {
                            AssetDetailsResponse assetDetails =
                                    this.bankClient.fetchAssetDetails(
                                            instrument.getAssetType(),
                                            deposit.getRegNo(),
                                            deposit.getDepositNo(),
                                            instrument.getSecurityId());

                            return instrument.toTinkInstrument(assetDetails);
                        })
                .collect(Collectors.toList());
    }
}
