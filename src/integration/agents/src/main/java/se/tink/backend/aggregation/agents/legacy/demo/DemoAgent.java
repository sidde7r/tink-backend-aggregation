package se.tink.backend.aggregation.agents.demo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.bankid.CredentialsSignicatBankIdAuthenticationHandler;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.demo.generators.DemoTransactionsGenerator;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdAuthenticator;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.filter.factory.ClientFilterFactory;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.credentials.demo.DemoCredentials;
import se.tink.libraries.credentials.demo.DemoCredentials.DemoUserFeature;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.enums.SwedishGiroType;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    PAYMENTS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    TRANSFERS,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
public final class DemoAgent extends AbstractAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshEInvoiceExecutor,
                RefreshTransferDestinationExecutor,
                TransferExecutor {
    private static final String BASE_PATH = "data/demo";
    private static final Integer NUMBER_OF_TRANSACTIONS_TO_RANDOMIZE = 3;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);
    private static final String CODE_1 = "code1";
    private static final String CODE_2 = "code2";

    private final DemoCredentials demoCredentials;
    private final String userPath;
    private final File accountsFile;
    private List<Account> accounts = null;

    public DemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        demoCredentials = DemoCredentials.byUsername(request.getCredentials().getUsername());

        if (demoCredentials != null) {
            userPath = BASE_PATH + File.separator + demoCredentials.getUsername();
        } else {
            userPath = BASE_PATH + File.separator + request.getCredentials().getUsername();
        }
        accountsFile = new File(userPath + File.separator + "accounts.txt");
    }

    private static Field[] createChallengeAndResponse(String code) {
        Field challengeField =
                Field.builder()
                        .immutable(true)
                        .description("Kod")
                        .value("Kod")
                        .name("code")
                        .helpText("Koden är: " + code)
                        .build();

        Field responseField =
                Field.builder()
                        .description("Sändkod")
                        .name("response")
                        .numeric(false)
                        .hint("NNNNN")
                        .maxLength(code.length())
                        .minLength(code.length())
                        .pattern("([a-zA-Z0-9]{" + code.length() + "})")
                        .build();

        return new Field[] {challengeField, responseField};
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        Credentials credentials = request.getCredentials();

        log.info("Parsing accounts: " + accountsFile.getAbsolutePath());

        if (!accountsFile.exists()) {
            log.warn("No accounts description found");
            throw LoginError.NOT_CUSTOMER.exception();
        }

        if (Objects.equal(credentials.getProviderName(), "demo-bankid")) {
            if (Objects.equal(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
                loginWithBankId(credentials);
                return true;
            } else {
                throw LoginError.NOT_SUPPORTED.exception();
            }
        }

        if (!Objects.equal(credentials.getPassword(), "demo")) {
            log.error(
                    "Could not authenticate demo credentials (fields: "
                            + credentials.getFieldsSerialized()
                            + ")");

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (demoCredentials != null
                && demoCredentials.hasFeature(DemoUserFeature.REQUIRES_BANK_ID)) {
            for (int i = 0; i < 10; i++) {
                supplementalInformationController.openMobileBankIdAsync(null);

                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
        } else if (demoCredentials != null
                && demoCredentials.hasFeature(DemoUserFeature.REQUIRES_SUPPLEMENTAL_INFORMATION)) {
            String response = requestChallengeResponse(CODE_1);

            if (Strings.isNullOrEmpty(response) || !response.equals(CODE_1)) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
            }
        }

        return true;
    }

    private List<Account> getAccounts() {
        if (accounts != null) {
            return accounts;
        }
        try {
            accounts =
                    DemoDataUtils.readAggregationAccounts(accountsFile, request.getCredentials());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return accounts;
    }

    private List<Transaction> getTransactions(Account account) {
        if (demoCredentials.hasFeature(DemoUserFeature.GENERATE_TRANSACTIONS)) {
            return DemoTransactionsGenerator.generateTransactions(demoCredentials, account);
        } else if (account.getType() != AccountTypes.LOAN) {
            File accountFile = new File(userPath + File.separator + account.getBankId() + ".txt");

            List<Transaction> transactions;
            try {
                if (demoCredentials != null
                        && demoCredentials.hasFeature(DemoUserFeature.RANDOMIZE_TRANSACTIONS)) {
                    transactions =
                            DemoDataUtils.readTransactionsWithRandomization(
                                    demoCredentials,
                                    accountFile,
                                    account,
                                    NUMBER_OF_TRANSACTIONS_TO_RANDOMIZE);
                } else {
                    transactions =
                            DemoDataUtils.readTransactions(demoCredentials, accountFile, account);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            return transactions;
        }
        return Collections.emptyList();
    }

    /**
     * Create a mortgage asset if the type is mortgage or the name "Bolån". Rest of the assets are
     * considered to be blanco loans assets.
     */
    private AccountFeatures createLoanAsset(Account account) {
        Loan loan = new Loan();

        if (Objects.equal(account.getType(), AccountTypes.MORTGAGE)
                || account.getName().toLowerCase().contains("bolån")) {
            loan.setInterest(0.019);
            loan.setName("Bolån");
            loan.setBalance(-2300000D);
            loan.setNumMonthsBound(1);
            loan.setType(Loan.Type.MORTGAGE);
        } else {
            loan.setInterest(0.9);
            loan.setName("Blanco");
            loan.setBalance(-50000D);
            loan.setType(Loan.Type.BLANCO);
        }

        return AccountFeatures.createForLoan(loan);
    }

    @Override
    public void logout() throws Exception {
        // do nothing
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        // Nothing to filter in demo
    }

    @Override
    public void execute(Transfer transfer) throws Exception, TransferExecutionException {
        if (!Objects.equal(demoCredentials.getUsername(), "201212121212")) {
            String response = requestChallengeResponse(CODE_1);
            if (Strings.isNullOrEmpty(response) || !response.equals(CODE_1)) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage("Kod nr 1 var felaktig")
                        .build();
            }

            response = requestChallengeResponse(CODE_2);
            if (Strings.isNullOrEmpty(response) || !response.equals(CODE_2)) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage("Kod nr 2 var felaktig")
                        .build();
            }
        }

        // Fake a upcoming transaction and it's transfer for a payed e-invoices.

        if (transfer.getType() == TransferType.EINVOICE) {

            transfer.setId(UUID.randomUUID());
            transfer.setType(TransferType.PAYMENT);

            List<Transaction> transactions = Lists.newArrayList();
            Transaction t = transferToTransaction(transfer);
            transactions.add(t);

            statusUpdater.updateStatus(CredentialsStatus.UPDATING);
            financialDataCacher.updateTransactions(
                    findAccountForIdentifier(transfer.getSource()), transactions);
            systemUpdater.processTransactions();
        }
    }

    private Account findAccountForIdentifier(AccountIdentifier transferAccountIdentifier)
            throws IOException {
        for (Account account :
                DemoDataUtils.readAggregationAccounts(accountsFile, request.getCredentials())) {
            for (AccountIdentifier accountAccountIdentifier : account.getIdentifiers()) {
                if (Objects.equal(
                        accountAccountIdentifier.getIdentifier(),
                        transferAccountIdentifier.getIdentifier())) {
                    return account;
                }
            }
        }
        return null;
    }

    private Transaction transferToTransaction(Transfer transfer) throws JsonProcessingException {
        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (transfer.getDueDate() == null) {
            transfer.setDueDate(dateHelper.getNowAsDate());
        }

        Transaction t = new Transaction();
        t.setDescription(transfer.getSourceMessage());
        t.setDate(getDueDate(transfer.getDueDate()));
        t.setAmount(transfer.getAmount().getValue());
        t.setPending(true);
        t.setPayload(
                TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER,
                MAPPER.writeValueAsString(transfer));
        t.setPayload(
                TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID,
                UUIDUtils.toTinkUUID(transfer.getId()));

        return t;
    }

    private Date getDueDate(Date currentDueDate) {
        CountryDateHelper countryDateHelper =
                new CountryDateHelper(
                        new Locale("sv", "SE"), TimeZone.getTimeZone("Europe/Stockholm"));
        return countryDateHelper.flattenTime(
                countryDateHelper.getProvidedDateOrCurrentDate(currentDueDate));
    }

    private String requestChallengeResponse(String code) {
        Field[] fields = createChallengeAndResponse(code);

        Map<String, String> answers =
                supplementalInformationController.askSupplementalInformationSync(fields);

        return answers.get("response");
    }

    public List<Transfer> getEInvoices() {
        // Generate some e-invoices

        List<Transfer> einvoices = Lists.newArrayList();

        einvoices.add(
                DemoDataUtils.createFakeTransfer(
                        "Centrala Studiestödsnämnd",
                        "4027809501",
                        2406,
                        "55803084",
                        SwedishGiroType.BG,
                        14,
                        TransferType.EINVOICE));
        einvoices.add(
                DemoDataUtils.createFakeTransferInComingSunday(
                        "Centrala Studiestödsnämnd 2",
                        "4027809502",
                        3500,
                        "55803084",
                        SwedishGiroType.BG,
                        TransferType.EINVOICE));

        // pendingEinvoices.add(createEinvoice("American Express 3757", "37578468440200734", 144.69,
        // "7308596",
        // SwedishGiroType.BG, 15, TransferType.EINVOICE));
        // pendingEinvoices.add(createEinvoice("Sunfleet Carsharing AB", "5165022766476", 685,
        // "7074198",
        // SwedishGiroType.BG, 20, TransferType.EINVOICE));

        return einvoices;
    }

    private void loginWithBankId(Credentials credentials) throws BankIdException {
        boolean authenticated = authenticateWithBankId(credentials);
        statusUpdater.updateStatus(credentials.getStatus(), credentials.getStatusPayload());
        if (!authenticated) {
            throw BankIdError.CANCELLED.exception();
        }
    }

    private boolean authenticateWithBankId(final Credentials credentials) {
        // Create the authenticator

        SignicatBankIdAuthenticator authenticator =
                new SignicatBankIdAuthenticator(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getUserId(),
                        credentials.getId(),
                        new CredentialsSignicatBankIdAuthenticationHandler(
                                credentials, supplementalInformationController));

        // Run the authenticator synchronously.

        authenticator.run();

        return Objects.equal(CredentialsStatus.UPDATING, credentials.getStatus());
    }
    /////////////// Refresh Executor Refactor /////////////////

    private Portfolio generateFakePortolio(Account account) {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.ISK);
        portfolio.setCashValue(231d);
        portfolio.setUniqueIdentifier(account.getAccountNumber());
        List<Instrument> instruments = new ArrayList<>();
        instruments.add(
                createFakeInstrument(
                        "SE0009778954",
                        "OMXS",
                        "SEK",
                        "XACT högutdelande",
                        "XACTHDIV",
                        Instrument.Type.OTHER,
                        "ETF"));
        instruments.add(
                createFakeInstrument(
                        "IE00BZ0PKV06",
                        "LONDON STOCK EXCHANGE",
                        "EUR",
                        "iShares Edge MSCI Europe Multifactor UCITS ETF EUR",
                        "IFSE",
                        Instrument.Type.OTHER,
                        "ETF"));
        instruments.add(
                createFakeInstrument(
                        "NO0010257801",
                        "OSLO BORS",
                        "NOK",
                        "DNB OBX",
                        "OBXEDNB",
                        Instrument.Type.OTHER,
                        "ETF"));
        instruments.add(
                createFakeInstrument(
                        "DK0060830644",
                        "OMX NORDIC EQUITIES",
                        "DKK",
                        "BEAR SX5E X15 NORDNET",
                        "BEAR SX5E X15 NORDNET",
                        Instrument.Type.OTHER,
                        "ETN"));
        instruments.add(
                createFakeInstrument(
                        "US06742W4309",
                        "NYSE ARCA",
                        "USD",
                        "Barclays Women in Leadership ETN",
                        "WIL",
                        Instrument.Type.OTHER,
                        "ETN"));
        instruments.add(
                createFakeInstrument(
                        "SE0000869646",
                        "OMXS",
                        "SEK",
                        "Boliden",
                        "BOL",
                        Instrument.Type.STOCK,
                        "Aktie"));
        instruments.add(
                createFakeInstrument(
                        "FI4000074984",
                        "OMX Helsinki",
                        "EUR",
                        "Valmet",
                        "VALMT",
                        Instrument.Type.STOCK,
                        "Aktie"));
        instruments.add(
                createFakeInstrument(
                        "NO0010096985",
                        "OBX Top 25",
                        "NOK",
                        "Statoil",
                        "STL",
                        Instrument.Type.STOCK,
                        "Aktie"));
        instruments.add(
                createFakeInstrument(
                        "DK0010181759",
                        "OMXC",
                        "DKK",
                        "Carlsberg B",
                        "CARL B",
                        Instrument.Type.STOCK,
                        "Aktie"));
        instruments.add(
                createFakeInstrument(
                        "US0378331005",
                        "NASDAQ",
                        "USD",
                        "Apple",
                        "AAPL",
                        Instrument.Type.STOCK,
                        "Aktie"));
        instruments.add(
                createFakeInstrument(
                        "SE0005798329",
                        null,
                        "SEK",
                        "Spiltan Högräntefond",
                        null,
                        Instrument.Type.FUND,
                        "Fond"));
        instruments.add(
                createFakeInstrument(
                        "SE0001718388",
                        null,
                        "SEK",
                        "AVANZA ZERO",
                        null,
                        Instrument.Type.FUND,
                        "Fond"));
        portfolio.setInstruments(instruments);
        return portfolio;
    }

    private Instrument createFakeInstrument(
            String ISIN,
            String marketplace,
            String currency,
            String name,
            String ticker,
            Instrument.Type type,
            String rawType) {
        Instrument instrument = new Instrument();
        instrument.setCurrency(currency);
        instrument.setIsin(ISIN);
        instrument.setMarketPlace(marketplace);
        instrument.setUniqueIdentifier(ISIN + marketplace);
        instrument.setAverageAcquisitionPrice(123.45);
        instrument.setQuantity(2.00);
        // Value of instrument has increased by 100% since purchase
        instrument.setPrice(123.45 * 2);
        instrument.setMarketValue(instrument.getQuantity() * instrument.getPrice());
        instrument.setName(name);
        instrument.setProfit(instrument.getMarketValue() - instrument.getAverageAcquisitionPrice());
        instrument.setTicker(ticker);
        instrument.setType(type);
        instrument.setRawType(rawType);
        return instrument;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return fetchAccountsPerType(RefreshableItem.CHECKING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return fetchTransactionsPerType(RefreshableItem.CHECKING_TRANSACTIONS);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return fetchAccountsPerType(RefreshableItem.CREDITCARD_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return fetchTransactionsPerType(RefreshableItem.CREDITCARD_TRANSACTIONS);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return fetchAccountsPerType(RefreshableItem.SAVING_ACCOUNTS);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return fetchTransactionsPerType(RefreshableItem.SAVING_TRANSACTIONS);
    }

    private FetchAccountsResponse fetchAccountsPerType(RefreshableItem type) {
        List<Account> accounts = new ArrayList<>();
        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(accounts::add);
        return new FetchAccountsResponse(accounts);
    }

    private FetchTransactionsResponse fetchTransactionsPerType(RefreshableItem type) {
        Map<Account, List<Transaction>> transactionsMap = new HashMap<>();
        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(account -> transactionsMap.put(account, getTransactions(account)));
        return new FetchTransactionsResponse(transactionsMap);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        getAccounts().stream()
                .filter(
                        account ->
                                RefreshableItem.INVESTMENT_ACCOUNTS.isAccountType(
                                        account.getType()))
                .forEach(
                        account ->
                                accounts.put(
                                        account,
                                        AccountFeatures.createForPortfolios(
                                                generateFakePortolio(account))));
        return new FetchInvestmentAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        Map<Account, AccountFeatures> accounts = new HashMap<>();
        getAccounts().stream()
                .filter(account -> RefreshableItem.LOAN_ACCOUNTS.isAccountType(account.getType()))
                .forEach(account -> accounts.put(account, createLoanAsset(account)));
        return new FetchLoanAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    ///////////////////////////////////////////////////////////

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        return new FetchEInvoicesResponse(getEInvoices());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();
        for (Account account : accounts) {
            List<TransferDestinationPattern> destinations = new ArrayList<>();
            destinations.add(
                    TransferDestinationPattern.createForMultiMatch(
                            AccountIdentifierType.SE, TransferDestinationPattern.ALL));
            destinations.add(
                    TransferDestinationPattern.createForMultiMatch(
                            AccountIdentifierType.SE_BG, TransferDestinationPattern.ALL));
            destinations.add(
                    TransferDestinationPattern.createForMultiMatch(
                            AccountIdentifierType.SE_PG, TransferDestinationPattern.ALL));
            transferDestinations.put(account, destinations);
        }
        return new FetchTransferDestinationsResponse(transferDestinations);
    }
}
