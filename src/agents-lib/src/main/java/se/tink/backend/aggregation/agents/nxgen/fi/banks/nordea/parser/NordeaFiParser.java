package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.parser;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CardBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities.HoldingsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan.entities.LoanData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.NordeaV21Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.TransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

public class NordeaFiParser extends NordeaV21Parser {
    public NordeaFiParser(TransactionParser parser, Credentials credentials) {
        super(parser, credentials, NordeaFiConstants.CURRENCY);
    }

    private Optional<String> getTinkAccountName(ProductEntity productEntity) {
        Optional<String> nickName = productEntity.getNickName();
        if (nickName.isPresent()) {
            return nickName;
        }
        String accountName = productEntity.getProductName();

        if (Strings.isNullOrEmpty(accountName)) {
            // productName can be null
            String accountTypeCode = productEntity.getNordeaProductTypeExtension();
            accountName = NordeaFiConstants.AccountType.getAccountNameFromCode(accountTypeCode);
        }

        return Optional.ofNullable(accountName);
    }

    @Override
    public LoanAccount parseLoanAccount(ProductEntity productEntity, LoanDetailsResponse loanDetailsResponse) {
        LoanAccount.Builder<?, ?> accountBuilder = LoanAccount.builder(productEntity.getAccountNumber(),
                new Amount(productEntity.getCurrency(), productEntity.getBalance()))
                .setAccountNumber(productEntity.getAccountNumber())
                .setName(getTinkAccountName(productEntity).orElse(productEntity.getAccountNumber()))
                .setBankIdentifier(productEntity.getNordeaAccountIdV2());

        LoanData loanData = loanDetailsResponse.getLoanData();
        if (loanData != null) {
            accountBuilder.setInterestRate(loanData.getInterest())
                    .setDetails(LoanDetails.builder()
                            .setLoanNumber(loanData.getLocalNumber())
                            .setNextDayOfTermsChange(loanData.getInterestTermEnds())
                            .setMonthlyAmortization(new Amount(loanData.getCurrency(),
                                    loanDetailsResponse.getFollowingPayment().getAmortization()))
                            .setInitialBalance(new Amount(loanData.getCurrency(), loanData.getGranted()))
                            .setType(NordeaFiConstants.AccountType.getLoanTypeFromCode(
                                    productEntity.getNordeaProductTypeExtension()))
                            .build());
        }

        return accountBuilder.build();
    }

    @Override
    public TransactionalAccount parseAccount(ProductEntity productEntity) {
        return TransactionalAccount.builder(getTinkAccountType(productEntity), productEntity.getAccountNumber(),
                new Amount(productEntity.getCurrency(), productEntity.getBalance()))
                .setAccountNumber(productEntity.getAccountNumber())
                .setName(getTinkAccountName(productEntity).orElse(productEntity.getAccountNumber()))
                .setBankIdentifier(productEntity.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public AccountTypes getTinkAccountType(ProductEntity productEntity) {
        return NordeaFiConstants.AccountType.getAccountTypeFromCode(productEntity.getNordeaProductTypeExtension());
    }

    @Override
    public CreditCardAccount parseCreditCardAccount(ProductEntity productEntity, CardBalanceEntity cardBalance) {
        return CreditCardAccount.builder(cardBalance.getUniqueIdentifier(), cardBalance.getBalance(),
                cardBalance.getAvailableCredit())
                .setAccountNumber(cardBalance.getCardNumber())
                .setName(getTinkAccountName(productEntity).orElse(cardBalance.getCardNumber()))
                .setBankIdentifier(productEntity.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public InvestmentAccount parseInvestmentAccount(ProductEntity productEntity) {
        return InvestmentAccount.builder(productEntity.getAccountNumber(),
                new Amount(productEntity.getCurrency(), productEntity.getBalance()))
                .setAccountNumber(productEntity.getAccountNumber())
                .setName(getTinkAccountName(productEntity).orElse(productEntity.getAccountNumber()))
                .setBankIdentifier(productEntity.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount) {
        return InvestmentAccount.builder(custodyAccount.getAccountId(),
                new Amount(custodyAccount.getCurrency(), custodyAccount.getMarketValue()))
                .setAccountNumber(custodyAccount.getAccountNumber())
                .setName(custodyAccount.getName())
                .setPortfolios(Collections.singletonList(parsePortfolio(custodyAccount)))
                .build();
    }

    private Portfolio parsePortfolio(CustodyAccount custodyAccount) {
        Portfolio portfolio = new Portfolio();

        portfolio.setTotalProfit(custodyAccount.getProfit());
        portfolio.setType(custodyAccount.getPortfolioType());
        portfolio.setRawType(custodyAccount.getName());
        portfolio.setTotalValue(custodyAccount.getMarketValue());
        portfolio.setUniqueIdentifier(custodyAccount.getAccountId());
        portfolio.setInstruments(fetchInstrumentsFor(custodyAccount));

        return portfolio;
    }

    private List<Instrument> fetchInstrumentsFor(CustodyAccount custodyAccount) {
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
        instrument.setCurrency(NordeaFiConstants.CURRENCY);
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
    public GeneralAccountEntity parseGeneralAccount(ProductEntity productEntity) {
        return null;
    }
}
