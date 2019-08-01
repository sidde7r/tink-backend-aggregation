package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers;

import java.util.Date;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities.CardDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

public abstract class NordeaV20Parser {

    private final TransactionParser transactionParser;
    private final Credentials credentials;
    private final String currency;

    public NordeaV20Parser(
            TransactionParser transactionParser, Credentials credentials, String currency) {
        this.transactionParser = transactionParser;
        this.credentials = credentials;
        this.currency = currency;
    }

    public Transaction parseTransaction(TransactionEntity te) {
        return Transaction.builder()
                .setAmount(transactionParser.getAmount(te))
                .setDate(transactionParser.getDate(te))
                .setDescription(transactionParser.getDescription(te))
                .setPending(transactionParser.isPending(te))
                .build();
    }

    public boolean isTransactionDateSane(TransactionEntity te) {
        return transactionParser.getDate(te).before(DateUtils.addYears(new Date(), 10));
    }

    public CreditCardTransaction.Builder parseTransaction(CreditCardTransactionEntity cte) {
        return CreditCardTransaction.builder()
                .setAmount(new Amount(cte.getAmount().getCurrency(), cte.getAmount().getValue()))
                .setDate(cte.getDate())
                .setDescription(cte.getText());
    }

    public UpcomingTransaction parseTransaction(PaymentEntity pe) {
        return UpcomingTransaction.builder()
                .setAmount(transactionParser.getAmount(pe))
                .setDate(transactionParser.getDate(pe))
                .setDescription(transactionParser.getDescription(pe))
                .build();
    }

    public abstract AccountTypes getTinkAccountType(ProductEntity pe);

    public abstract TransactionalAccount parseAccount(ProductEntity pe);

    public abstract LoanAccount parseBlancoLoan(ProductEntity pe);

    public abstract LoanAccount parseMortgage(
            ProductEntity pe, LoanDetailsResponse loanDetailsResponse);

    public abstract CreditCardAccount parseCreditCardAccount(
            ProductEntity pe, CardDetailsEntity cardDetails);

    public abstract InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount);

    public abstract GeneralAccountEntity parseGeneralAccount(ProductEntity pe);
}
