package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.AccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.entities.HoldingsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.NordeaV17Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.TransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.LoanModuleBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NordeaNoParser extends NordeaV17Parser {
    public NordeaNoParser(TransactionParser parser) {
        super(parser);
    }

    private Optional<String> getTinkAccountName(ProductEntity pe) {
        Optional<String> nickName = pe.getNickName();
        if (nickName.isPresent()) {
            return nickName;
        }
        String accountName = pe.getProductName();

        if (Strings.isNullOrEmpty(accountName)) {
            // Just in case productName would be null
            String accountTypeCode = pe.getNordeaProductTypeExtension();
            accountName = AccountType.getAccountNameForCode(accountTypeCode);
        }

        return Optional.ofNullable(accountName);
    }

    @Override
    public AccountTypes getTinkAccountType(ProductEntity pe) {
        String accountTypeCode = pe.getNordeaProductTypeExtension();
        return AccountType.getAccountTypeForCode(accountTypeCode);
    }

    @Override
    public LoanAccount parseLoanAccount(
            ProductEntity productEntity, LoanDetailsEntity loanDetails) {
        LoanModuleBuildStep loanModuleBuildStep =
                LoanModule.builder()
                        .withType(
                                AccountType.getLoanTypeForCode(
                                        productEntity.getNordeaProductTypeExtension()))
                        .withBalance(
                                ExactCurrencyAmount.of(
                                        loanDetails.getBalance(), NordeaNoConstants.CURRENCY))
                        .withInterestRate(loanDetails.getInterestRate());
        loanDetails
                .getLoanData()
                .ifPresent(
                        loanData ->
                                loanModuleBuildStep
                                        .setLoanNumber(loanData.getLocalNumber())
                                        .setNextDayOfTermsChange(
                                                loanData.getInterestTermEnds()
                                                        .toInstant()
                                                        .atZone(NordeaNoConstants.DEFAULT_ZONE_ID)
                                                        .toLocalDate())
                                        .setMonthlyAmortization(
                                                ExactCurrencyAmount.of(
                                                        loanDetails
                                                                .getFollowingPayment()
                                                                .getAmortization(),
                                                        NordeaNoConstants.CURRENCY))
                                        .setInitialBalance(
                                                ExactCurrencyAmount.of(
                                                        loanData.getGranted(),
                                                        NordeaNoConstants.CURRENCY)));
        LoanModule loanModule = loanModuleBuildStep.build();

        String accountNumber = productEntity.getAccountNumber(false);
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(accountNumber)
                        .withAccountNumber(accountNumber)
                        .withAccountName(getTinkAccountName(productEntity).orElse(accountNumber))
                        .addIdentifier(new NorwegianIdentifier(accountNumber))
                        .build();

        return LoanAccount.nxBuilder()
                .withLoanDetails(loanModule)
                .withId(idModule)
                .setApiIdentifier(productEntity.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public TransactionalAccount parseTransactionalAccount(ProductEntity pe) {
        return TransactionalAccount.builder(
                        getTinkAccountType(pe),
                        pe.getAccountNumber(false),
                        pe.getBalanceAmount().orElse(ExactCurrencyAmount.inNOK(pe.getBalance())))
                .setAccountNumber(pe.getAccountNumber(false))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(false)))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public CreditCardAccount parseCreditCardAccount(ProductEntity pe, CardsEntity cardsEntity) {
        return CreditCardAccount.builder(
                        pe.getAccountNumber(false),
                        pe.getNegativeBalanceAmount()
                                .orElse(ExactCurrencyAmount.inNOK(-1 * pe.getBalance())),
                        pe.getCurrency()
                                .map(
                                        c ->
                                                ExactCurrencyAmount.of(
                                                        cardsEntity.getFundsAvailable(), c))
                                .orElse(ExactCurrencyAmount.inNOK(cardsEntity.getFundsAvailable())))
                .setAccountNumber(pe.getAccountNumber(false))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(false)))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount) {
        return InvestmentAccount.builder(custodyAccount.getAccountId())
                .setAccountNumber(custodyAccount.getAccountNumber())
                .setName(custodyAccount.getName())
                .setCashBalance(ExactCurrencyAmount.inNOK(0d))
                .setPortfolios(Collections.singletonList(parsePortfolio(custodyAccount)))
                .build();
    }

    private Portfolio parsePortfolio(CustodyAccount custodyAccount) {
        Portfolio portfolio = new Portfolio();

        portfolio.setType(custodyAccount.getPortfolioType());
        portfolio.setRawType(custodyAccount.getName());
        portfolio.setTotalProfit(custodyAccount.getProfit());
        portfolio.setTotalValue(custodyAccount.getMarketValue());
        portfolio.setUniqueIdentifier(custodyAccount.getAccountId());
        portfolio.setInstruments(getInstrumentsFor(custodyAccount));

        return portfolio;
    }

    private List<Instrument> getInstrumentsFor(CustodyAccount custodyAccount) {
        return custodyAccount.getHoldings().stream()
                .filter(holding -> holding.getQuantity() != null && holding.getQuantity() > 0)
                .map(this::toInstrument)
                .collect(Collectors.toList());
    }

    private Instrument toInstrument(HoldingsEntity holdingsEntity) {
        Instrument instrument = new Instrument();

        InstrumentEntity thisInstrument = holdingsEntity.getInstrument();
        String isin = thisInstrument.getInstrumentId().getIsin();
        String market = thisInstrument.getInstrumentId().getMarket();

        instrument.setAverageAcquisitionPrice(holdingsEntity.getAvgPurchasePrice());
        instrument.setCurrency(NordeaNoConstants.CURRENCY);
        instrument.setIsin(isin);
        instrument.setMarketPlace(market);
        instrument.setMarketValue(holdingsEntity.getMarketValue());
        instrument.setName(thisInstrument.getInstrumentName());
        instrument.setPrice(thisInstrument.getPrice());
        instrument.setProfit(holdingsEntity.getProfit());
        instrument.setQuantity(holdingsEntity.getQuantity());
        instrument.setRawType(thisInstrument.getInstrumentType());
        instrument.setType(thisInstrument.getTinkInstrumentType());
        instrument.setUniqueIdentifier(isin + market);

        return instrument;
    }

    @Override
    public GeneralAccountEntity parseGeneralAccount(ProductEntity pe) {
        return null;
    }
}
