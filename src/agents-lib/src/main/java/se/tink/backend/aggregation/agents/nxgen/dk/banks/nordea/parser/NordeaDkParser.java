package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.AccountType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.ProductType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities.CardDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.HoldingsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.entities.LoanData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.TransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.libraries.amount.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

public class NordeaDkParser extends NordeaV20Parser {
    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    private final Credentials credentials;

    public NordeaDkParser(TransactionParser parser, Credentials credentials) {
        super(parser, credentials, NordeaDkConstants.CURRENCY);
        this.credentials = credentials;
    }

    private Optional<String> getTinkAccountName(ProductEntity pe) {
        if (Objects.equals(pe.getProductCode(), ProductType.MORTGAGE) && pe.getMtgLoanName() != null) {
            return Optional.of(pe.getMtgLoanName());
        }

        Optional<String> nickName = pe.getNickName();
        if (nickName.isPresent()) {
            return nickName;
        }
        String accountName = pe.getProductName();

        if (Strings.isNullOrEmpty(accountName)) {
            // Just in case productName would be null
            String accountTypeCode = pe.getProductTypeExtension();
            accountName = AccountType.getAccountNameForCode(accountTypeCode);
        }

        return Optional.ofNullable(accountName);
    }

    @Override
    public AccountTypes getTinkAccountType(ProductEntity pe) {
        String accountTypeCode = pe.getProductTypeExtension();
        return AccountType.getAccountTypeForCode(accountTypeCode);
    }

    @Override
    public LoanAccount parseMortgage(ProductEntity pe, LoanDetailsResponse loanDetailsResponse) {
        LoanAccount.Builder<?, ?> accountBuilder = LoanAccount.builder(pe.getAccountNumber(false),
                new Amount(pe.getCurrency(), pe.getBalance()))
                .setAccountNumber(pe.getAccountNumber(true))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setBankIdentifier(pe.getNordeaAccountIdV2());

        LoanData loanData = loanDetailsResponse.getLoanData();
        if (loanData != null) {
            accountBuilder.setInterestRate(loanData.getInterest())
                    .setDetails(LoanDetails.builder(AccountType.getLoanTypeForCode(pe.getProductTypeExtension()))
                            .setLoanNumber(loanData.getLocalNumber())
                            .setNextDayOfTermsChange(loanData.getInterestTermEnds())
                            .setMonthlyAmortization(new Amount(loanData.getCurrency(),
                                    loanDetailsResponse.getFollowingPayment().getAmortization()))
                            .setInitialBalance(new Amount(loanData.getCurrency(), loanData.getGranted()))
                            .build());
        }

        return accountBuilder.build();
    }

    public LoanAccount parseBlancoLoan(ProductEntity pe) {
        return LoanAccount.builder(pe.getAccountNumber(false), new Amount(pe.getCurrency(), pe.getBalance()))
                .setAccountNumber(pe.getAccountNumber(true))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .setDetails(LoanDetails.builder(AccountType.getLoanTypeForCode(pe.getProductTypeExtension()))
                        .setLoanNumber(Optional.ofNullable(pe.getLoanId()).orElse(pe.getProductNumber()))
                        .build())
                .build();
    }

    @Override
    public TransactionalAccount parseAccount(ProductEntity pe) {
        return TransactionalAccount.builder(getTinkAccountType(pe), pe.getAccountNumber(false),
                new Amount(pe.getCurrency(), pe.getBalance()))
                .setAccountNumber(pe.getAccountNumber(true))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public CreditCardAccount parseCreditCardAccount(ProductEntity pe, CardDetailsEntity cardDetails) {
        return CreditCardAccount.builder(pe.getAccountNumber(false),
                new Amount(pe.getCurrency(), cardDetails.getCurrentBalance()),
                new Amount(pe.getCurrency(), cardDetails.constructAvailableFunds()))
                .setAccountNumber(pe.getAccountNumber(true))
                .setName(getTinkAccountName(pe).orElse(pe.getAccountNumber(true)))
                .setBankIdentifier(pe.getNordeaAccountIdV2())
                .build();
    }

    @Override
    public InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount) {
        return InvestmentAccount
                .builder(custodyAccount.getAccountId())
                .setAccountNumber(custodyAccount.getAccountNumber())
                .setName(custodyAccount.getName())
                .setCashBalance(Amount.inDKK(0))
                .setPortfolios(Collections.singletonList(parsePortfolio(custodyAccount)))
                .build();
    }

    private Portfolio parsePortfolio(CustodyAccount custodyAccount) {
        Portfolio portfolio = new Portfolio();

        portfolio.setType(custodyAccount.getPortfolioType(credentials)); // credentials used for logging
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
        instrument.setCurrency(NordeaDkConstants.CURRENCY);
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
