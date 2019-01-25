package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.system.rpc.Portfolio;

public abstract class CrossKeyConfiguration {

    public abstract String getBaseUrl();
    public LogTag getLoanLogTag() {
        return CrossKeyConstants.Fetcher.LOAN_LOGGING;
    }
    public LogTag getInvestmentPortfolioLogTag() {
        return CrossKeyConstants.Fetcher.INVESTMENT_PORTFOLIO_LOGGING;
    }
    public LogTag getInvestmentInstrumentLogTag() {
        return CrossKeyConstants.Fetcher.INVESTMENT_INSTRUMENT_LOGGING;
    }

    public abstract TransactionalAccount parseTransactionalAccount(CrossKeyAccount crossKeyAccount);

    public abstract Transaction parseTinkTransaction(CrossKeyTransaction crossKeyTransaction);

    public abstract LoanAccount parseLoanAccount(CrossKeyAccount account, LoanDetailsEntity loanDetailsEntity);

    public abstract InvestmentAccount parseInvestmentAccount(CrossKeyAccount account, Portfolio portfolio);

    protected boolean noContent(String s) {
        return Strings.nullToEmpty(s).trim().isEmpty();
    }

    protected boolean hasContent(String s) {
        return !noContent(s);
    }
}
