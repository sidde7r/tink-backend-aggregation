package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers;

import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CardBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;

public abstract class NordeaV21Parser {

    private final TransactionParser transactionParser;
    protected final Credentials credentials;
    private final String currency;

    public NordeaV21Parser(TransactionParser transactionParser, Credentials credentials, String currency) {
        this.transactionParser = transactionParser;
        this.credentials = credentials;
        this.currency = currency;
    }

    public Transaction parseTransaction(TransactionEntity transactionEntity) {
        return Transaction.builder()
                .setAmount(transactionParser.getAmount(transactionEntity))
                .setDate(transactionParser.getDate(transactionEntity))
                .setDescription(transactionParser.getDescription(transactionEntity))
                .setPending(transactionParser.isPending(transactionEntity))
                .build();
    }

    public CreditCardTransaction.Builder parseTransaction(CreditCardTransactionEntity cte) {
        return CreditCardTransaction.builder()
                .setAmount(transactionParser.getAmount(cte))
                .setDate(transactionParser.getDate(cte))
                .setDescription(transactionParser.getDescription(cte))
                .setPending(transactionParser.isPending(cte));
    }

    public UpcomingTransaction parseTransaction(PaymentEntity paymentEntity) {
        return UpcomingTransaction.builder()
                .setAmount(transactionParser.getAmount(paymentEntity))
                .setDate(transactionParser.getDate(paymentEntity))
                .setDescription(transactionParser.getDescription(paymentEntity))
                .build();
    }

    public abstract AccountTypes getTinkAccountType(ProductEntity productEntity);
    public abstract TransactionalAccount parseAccount(ProductEntity productEntity);
    public abstract LoanAccount parseLoanAccount(ProductEntity productEntity, LoanDetailsResponse loanDetailsResponse);
    public abstract CreditCardAccount parseCreditCardAccount(ProductEntity productEntity, CardBalanceEntity cardBalance);

    public abstract InvestmentAccount parseInvestmentAccount(ProductEntity productEntity);
    public abstract InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount);

    public abstract GeneralAccountEntity parseGeneralAccount(ProductEntity productEntity);
}
