package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.parser;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
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
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

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
    public LoanAccount parseLoanAccount(ProductEntity pe, LoanDetailsEntity loanDetails) {
        LoanAccount.Builder accountBuilder = LoanAccount.builder(pe.getAccountNumber(true),
                pe.getBalanceAmount().orElse(loanDetails.getBalanceAmount()))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setUniqueIdentifier(pe.getAccountNumber(false))
                .setBankIdentifier(pe.getNordeaAccountIdV2());

        loanDetails.getLoanData().ifPresent(loanData -> accountBuilder.setInterestRate(loanData.getInterest())
                .setDetails(LoanDetails.builder()
                        .setLoanNumber(loanData.getLocalNumber())
                        .setNextDayOfTermsChange(loanData.getInterestTermEnds())
                        .setMonthlyAmortization(new Amount(loanData.getCurrency(),
                                loanDetails.getFollowingPayment().getAmortization()))
                        .setInitialBalance(new Amount(loanData.getCurrency(), loanData.getGranted()))
                        .setType(AccountType.getLoanTypeForCode(pe.getNordeaProductTypeExtension()))
                        .build()));

        return accountBuilder.build();
    }

    @Override
    public TransactionalAccount parseTransactionalAccount(ProductEntity pe) {
        return TransactionalAccount.builder(getTinkAccountType(pe), pe.getAccountNumber(true),
                pe.getBalanceAmount().orElse(Amount.inNOK(pe.getBalance())))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setUniqueIdentifier(pe.getAccountNumber(false))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public CreditCardAccount parseCreditCardAccount(ProductEntity pe, CardsEntity cardsEntity) {
        return CreditCardAccount.builder(pe.getAccountNumber(true),
                pe.getNegativeBalanceAmount().orElse(Amount.inNOK(-1 * pe.getBalance())),
                pe.getCurrency().map(c -> new Amount(c, cardsEntity.getFundsAvailable()))
                        .orElse(Amount.inNOK(cardsEntity.getFundsAvailable())))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setUniqueIdentifier(pe.getAccountNumber(false))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount) {
        return InvestmentAccount
                .builder(custodyAccount.getAccountNumber(), Amount.inNOK(custodyAccount.getMarketValue()))
                .setName(custodyAccount.getName())
                .setUniqueIdentifier(custodyAccount.getAccountId())
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
