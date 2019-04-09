package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public abstract class NordeaV17Parser {
    private final TransactionParser transactionParser;

    public NordeaV17Parser(TransactionParser transactionParser) {
        this.transactionParser = transactionParser;
    }

    public Transaction parseTransaction(TransactionEntity te) {
        return Transaction.builder()
                .setAmount(transactionParser.getAmount(te))
                .setDate(transactionParser.getDate(te))
                .setDescription(transactionParser.getDescription(te))
                .setPending(transactionParser.isPending(te))
                .build();
    }

    public CreditCardTransaction.Builder parseTransaction(CreditCardTransactionEntity cte) {
        return CreditCardTransaction.builder()
                .setAmount(transactionParser.getAmount(cte))
                .setDate(transactionParser.getDate(cte))
                .setDescription(transactionParser.getDescription(cte))
                .setPending(transactionParser.isPending(cte));
    }

    public UpcomingTransaction parseTransaction(PaymentEntity pe) {
        return UpcomingTransaction.builder()
                .setAmount(transactionParser.getAmount(pe))
                .setDate(transactionParser.getDate(pe))
                .setDescription(transactionParser.getDescription(pe))
                .build();
    }

    public abstract AccountTypes getTinkAccountType(ProductEntity pe);

    public abstract TransactionalAccount parseTransactionalAccount(ProductEntity pe);

    public abstract LoanAccount parseLoanAccount(
            ProductEntity pe, LoanDetailsEntity loanDetailsResponse);

    public abstract CreditCardAccount parseCreditCardAccount(
            ProductEntity pe, CardsEntity cardsEntity);

    public abstract InvestmentAccount parseInvestmentAccount(CustodyAccount custodyAccount);

    public abstract GeneralAccountEntity parseGeneralAccount(ProductEntity pe);
}
