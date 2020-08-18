package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa;

import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaixaConstants {

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter YEARMONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MM-yyyy");

    public static final String TIMEZONE_ID = "Portugal";

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "DepositAccount")
                    .put(TransactionalAccountType.SAVINGS, "TermSavingsAccount")
                    .build();

    public static final GenericTypeMapper<InstrumentType, String> INSTRUMENT_TYPE_MAPPER =
            GenericTypeMapper.<InstrumentType, String>genericBuilder()
                    .put(InstrumentType.OTHER, "O10", "Bonds") // O10 - bonds
                    .build();

    public static class Urls {
        public static final String BASE = "https://app.cgd.pt/pceApi/rest/v1";

        public static final URL AUTH = new URL(BASE.concat(Endpoints.AUTH));
        public static final URL FETCH_ACCOUNTS = new URL(BASE.concat(Endpoints.FETCH_ACCOUNTS));
        public static final URL FETCH_ACCOUNT_DETAILS =
                new URL(BASE.concat(Endpoints.FETCH_ACCOUNT_DETAILS));
        public static final URL FETCH_INVESTMENT_DETAILS =
                new URL(BASE.concat(Endpoints.FETCH_INVESTMENTS_DETAILS));
        public static final URL FETCH_MARKET_DETAILS =
                new URL(BASE.concat(Endpoints.FETCH_MARKET_DETAILS));
        public static final URL FETCH_CARD_ACCOUNTS =
                new URL(BASE.concat(Endpoints.FETCH_CARD_ACCOUNTS));
        public static final URL FETCH_CARD_ACCOUNT_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.FETCH_CARD_ACCOUNT_TRANSACTIONS));
        public static final URL FETCH_MORTGAGE_DETAILS =
                new URL(BASE.concat(Endpoints.FETCH_MORTGAGE_DETAILS));
        public static final URL FETCH_MORTGAGE_INSTALLMENTS =
                new URL(BASE.concat(Endpoints.FETCH_MORTGAGE_INSTALLMENTS));
    }

    public static class Endpoints {
        public static final String AUTH = "/system/security/authentications/basic";
        public static final String FETCH_ACCOUNTS = "/business/accounts";
        public static final String FETCH_ACCOUNT_DETAILS = "/business/accounts/{accountKey}";
        public static final String FETCH_INVESTMENTS_DETAILS =
                "/business/investments/portfolio/byassettypes";
        public static final String FETCH_MARKET_DETAILS = "/business/investments/quotes";
        public static final String FETCH_CARD_ACCOUNTS = "/business/cards/customercarddata";
        public static final String FETCH_CARD_ACCOUNT_TRANSACTIONS =
                "/business/cards/accounts/{cardAccountId}/transactions";
        public static final String FETCH_MORTGAGE_DETAILS = "/business/loans/mortgage/{accountKey}";
        public static final String FETCH_MORTGAGE_INSTALLMENTS =
                "/business/loans/mortgage/{accountKey}/installments";
    }

    public static class Parameters {
        public static final String ACCOUNT_KEY = "accountKey";
        public static final String CARD_ACCOUNT_ID = "cardAccountId";
    }

    public static class QueryParams {
        public static final String USER = "u";
        public static final String TARGET_OPERATION_TYPE = "targetOperationType";
        public static final String INCLUDE_BALANCES = "includeBalancesInResponse";
        public static final String INCLUDE_TRANSACTIONS = "includeTransactionsInResponse";
        public static final String FROM = "fromBookDate";
        public static final String TO = "toBookDate";
        public static final String ASSET_TYPE_ID = "assetTypeId";
        public static final String QUOTES_SEARCH_TYPE_ID = "quotesSearchTypeId";
        public static final String FULL_ACCOUNT_KEY = "fullAccountKey";
        public static final String TARGET_CARD_OPERATION_TYPE = "targetCardOperationType";
        public static final String STATEMENT_DATE = "statementDate";
        public static final String CARD_ACCOUNT_ID = "cardAccountId";
        public static final String PAGE_KEY = "pageKey";
    }

    public static class QueryValues {
        public static final String BALANCES_AND_TRANSACTIONS_OPERATION =
                "BALANCES_AND_TRANSACTIONS";
        public static final String INVESTMENT_PORTFOLIO_OPERATION = "INVESTMENT_PORTFOLIO";
        public static final String PORTFOLIO_MARKET_DETAILS_OPERATION = "PORTFOLIO";
        public static final String CONSUMER_LOAN = "CONSUMER_LOAN";
        public static final String MORTGAGE_LOAN = "MORTGAGE_LOAN";
    }

    public static class HeaderKeys {
        public static final String X_CGD_APP_DEVICE = "X-CGD-APP-Device";
        public static final String X_CGD_APP_NAME = "X-CGD-APP-Name";
        public static final String X_CGD_APP_VERSION = "X-CGD-APP-Version";
    }

    public static class HeaderValues {
        public static final String ACCEPT = "*/*";
        public static final String DEVICE_NAME = "is2";
        public static final String APP_NAME = "APP_CAIXADIRECTA";
        public static final String APP_VERSION = "1.11.1";
    }

    public static class STORAGE {
        public static final String ACCOUNT_CURRENCY = "accountCurrency";
    }
}
