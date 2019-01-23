package se.tink.backend.aggregation.agents.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.TransferDestinationsResponse;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.bankid.CredentialsSignicatBankIdAuthenticationHandler;
import se.tink.backend.aggregation.agents.demo.generators.DemoTransactionsGenerator;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.demo.DemoDataUtils;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.SignicatBankIdAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.core.SwedishGiroType;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionPayloadTypes;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.credentials.demo.DemoCredentials.DemoUserFeature;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DemoAgent extends AbstractAgent implements RefreshableItemExecutor, TransferExecutor {
    private static final String BASE_PATH = "data/demo";
    private static final Integer NUMBER_OF_TRANSACTIONS_TO_RANDOMIZE = 3;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DemoCredentials demoCredentials;
    private String userPath;
    private File accountsFile;

    private List<Account> accounts = null;

    public DemoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        demoCredentials = DemoCredentials.byUsername(request.getCredentials().getUsername());

        if (demoCredentials != null) {
            userPath = BASE_PATH + File.separator + demoCredentials.getUsername();
        } else {
            userPath = BASE_PATH + File.separator + request.getCredentials().getUsername();
        }
        accountsFile = new File(userPath + File.separator + "accounts.txt");
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
            log.error("Could not authenticate demo credentials (fields: " + credentials.getFieldsSerialized() + ")");

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        if (demoCredentials != null && demoCredentials.hasFeature(DemoUserFeature.REQUIRES_BANK_ID)) {
            for (int i = 0; i < 10; i++) {
                credentials.setStatusPayload(null);
                credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

                context.requestSupplementalInformation(credentials, false);

                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
        } else if (demoCredentials != null && demoCredentials
                .hasFeature(DemoUserFeature.REQUIRES_SUPPLEMENTAL_INFORMATION)) {
            String response = requestChallengeResponse(request.getCredentials(), "code1");

            if (Strings.isNullOrEmpty(response) || !response.equals("code1")) {
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
            accounts = DemoDataUtils.readAggregationAccounts(accountsFile, request.getCredentials());
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
                if (demoCredentials != null && demoCredentials.hasFeature(DemoUserFeature.RANDOMIZE_TRANSACTIONS)) {
                    transactions = DemoDataUtils
                            .readTransactionsWithRandomization(demoCredentials, accountFile, account,
                                    NUMBER_OF_TRANSACTIONS_TO_RANDOMIZE);
                } else {
                    transactions = DemoDataUtils.readTransactions(demoCredentials, accountFile, account);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            return transactions;
        }
        return Collections.emptyList();
    }

    private void updateAccountsPerType(RefreshableItem type) {
        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(context::cacheAccount);
    }

    private void updateTransactionsPerType(RefreshableItem type) {
        getAccounts().stream()
                .filter(account -> type.isAccountType(account.getType()))
                .forEach(account -> context.updateTransactions(account, getTransactions(account)));
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case TRANSFER_DESTINATIONS:
            TransferDestinationsResponse response = new TransferDestinationsResponse();

            for (Account account : context.getUpdatedAccounts()) {
                response.addDestination(account, TransferDestinationPattern.createForMultiMatch(
                        AccountIdentifier.Type.SE, TransferDestinationPattern.ALL));
                response.addDestination(account, TransferDestinationPattern.createForMultiMatch(
                        AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL));
                response.addDestination(account, TransferDestinationPattern.createForMultiMatch(
                        AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL));
            }
            context.updateTransferDestinationPatterns(response.getDestinations());
            break;

        case EINVOICES:
            context.updateEinvoices(getEInvoices());
            break;

        case CHECKING_ACCOUNTS:
        case SAVING_ACCOUNTS:
        case CREDITCARD_ACCOUNTS:
            updateAccountsPerType(item);
            break;

        case CHECKING_TRANSACTIONS:
        case SAVING_TRANSACTIONS:
        case CREDITCARD_TRANSACTIONS:
            updateTransactionsPerType(item);
            break;

        case LOAN_ACCOUNTS:
            getAccounts().stream()
                    .filter(account -> RefreshableItem.LOAN_ACCOUNTS.isAccountType(account.getType()))
                    .forEach(account -> context.cacheAccount(account, createLoanAsset(account)));
            break;

        case INVESTMENT_ACCOUNTS:
            getAccounts().stream()
                    .filter(account -> RefreshableItem.INVESTMENT_ACCOUNTS.isAccountType(account.getType()))
                    .forEach(account -> context.cacheAccount(account,
                            AccountFeatures.createForPortfolios(generateFakePortolio(account))));
            break;
        }
    }

    /**
     * Create a mortgage asset if the type is mortgage or the name "Bolån". Rest of the assets are considered to be
     * blanco loans assets.
     */
    private AccountFeatures createLoanAsset(Account account) {
        Loan loan = new Loan();

        if (Objects.equal(account.getType(), AccountTypes.MORTGAGE) ||
                account.getName().toLowerCase().contains("bolån")) {
            loan.setInterest(0.019);
            loan.setName("Bolån");
            loan.setBalance(-2300000D);
            loan.setNumMonthsBound(1);
            loan.setType(se.tink.backend.system.rpc.Loan.Type.MORTGAGE);
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
    public void execute(Transfer transfer) throws Exception,
            TransferExecutionException {

        if (!Objects.equal(demoCredentials.getUsername(), "201212121212")) {
            String response = requestChallengeResponse(request.getCredentials(), "code1");
            if (Strings.isNullOrEmpty(response) || !response.equals("code1")) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage("Kod nr 1 var felaktig").build();
            }

            response = requestChallengeResponse(request.getCredentials(), "code2");
            if (Strings.isNullOrEmpty(response) || !response.equals("code2")) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage("Kod nr 2 var felaktig").build();
            }
        }

        // Fake a upcoming transaction and it's transfer for a payed e-invoices.

        if (transfer.getType() == TransferType.EINVOICE) {

            transfer.setId(UUID.randomUUID());
            transfer.setType(TransferType.PAYMENT);

            List<Transaction> transactions = Lists.newArrayList();
            Transaction t = transferToTransaction(transfer);
            transactions.add(t);

            context.updateStatus(CredentialsStatus.UPDATING);
            context.updateTransactions(findAccountForIdentifier(transfer.getSource()), transactions);
            context.processTransactions();
        }
    }

    @Override
    public void update(Transfer transfer) throws Exception, TransferExecutionException {

    }

    private Account findAccountForIdentifier(AccountIdentifier transferAccountIdentifier) throws IOException {
        for (Account account : DemoDataUtils.readAggregationAccounts(accountsFile, request.getCredentials())) {
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
        Transaction t = new Transaction();
        t.setDescription(transfer.getSourceMessage());
        t.setDate(se.tink.libraries.date.DateUtils.flattenTime(transfer.getDueDate()));
        t.setAmount(transfer.getAmount().getValue());
        t.setPending(true);
        t.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER, MAPPER.writeValueAsString(transfer));
        t.setPayload(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID, UUIDUtils.toTinkUUID(transfer.getId()));

        return t;
    }

    private String requestChallengeResponse(Credentials credentials, String code) {
        List<Field> fields = createChallengeAndResponse(code);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation = context.requestSupplementalInformation(credentials, true);

        log.info("Supplemental Information response is: " + supplementalInformation);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return null;
        }

        Map<String, String> answers = SerializationUtils.deserializeFromString(supplementalInformation,
                new TypeReference<HashMap<String, String>>() {
                });

        return answers.get("response");
    }

    private static List<Field> createChallengeAndResponse(String code) {
        Field challengeField = new Field();

        challengeField.setImmutable(true);
        challengeField.setDescription("Kod");
        challengeField.setValue("Kod");
        challengeField.setName("code");
        challengeField.setHelpText("Koden är: " + code);

        Field responseField = new Field();

        responseField.setDescription("Sändkod");
        responseField.setName("response");
        responseField.setNumeric(false);
        responseField.setHint("NNNNN");
        responseField.setMaxLength(code.length());
        responseField.setMinLength(code.length());
        responseField.setPattern("([a-zA-Z0-9]{" + code.length() + "})");

        return Lists.newArrayList(challengeField, responseField);
    }

    public List<Transfer> getEInvoices() {
        // Generate some e-invoices

        List<Transfer> einvoices = Lists.newArrayList();

        einvoices.add(DemoDataUtils.createFakeTransfer("Centrala Studiestödsnämnd", "4027809501", 2406, "55803084",
                SwedishGiroType.BG, 14, TransferType.EINVOICE));
        einvoices.add(DemoDataUtils
                .createFakeTransferInComingSunday("Centrala Studiestödsnämnd 2", "4027809502", 3500, "55803084",
                        SwedishGiroType.BG, TransferType.EINVOICE));

        // pendingEinvoices.add(createEinvoice("American Express 3757", "37578468440200734", 144.69, "7308596",
        // SwedishGiroType.BG, 15, TransferType.EINVOICE));
        // pendingEinvoices.add(createEinvoice("Sunfleet Carsharing AB", "5165022766476", 685, "7074198",
        // SwedishGiroType.BG, 20, TransferType.EINVOICE));

        return einvoices;
    }

    private void loginWithBankId(Credentials credentials) throws BankIdException {
        boolean authenticated = authenticateWithBankId(credentials);
        context.updateStatus(credentials.getStatus(), credentials.getStatusPayload());
        if (!authenticated) {
            throw BankIdError.CANCELLED.exception();
        }
    }

    private boolean authenticateWithBankId(final Credentials credentials) {
        // Create the authenticator

        SignicatBankIdAuthenticator authenticator = new SignicatBankIdAuthenticator(
                credentials.getField(Field.Key.USERNAME),
                credentials.getUserId(),
                credentials.getId(),
                context.getCatalog(),
                new CredentialsSignicatBankIdAuthenticationHandler(credentials, context));

        // Run the authenticator synchronously.

        authenticator.run();

        return Objects.equal(CredentialsStatus.UPDATING, credentials.getStatus());
    }

    private Portfolio generateFakePortolio(Account account) {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.ISK);
        portfolio.setCashValue(231d);
        portfolio.setUniqueIdentifier(account.getAccountNumber());
        List<Instrument> instruments = new ArrayList<>();
        instruments.add(createFakeInstrument("SE0009778954", "OMXS", "SEK", "XACT högutdelande", "XACTHDIV",
                Instrument.Type.OTHER, "ETF"));
        instruments.add(createFakeInstrument("IE00BZ0PKV06", "LONDON STOCK EXCHANGE", "EUR",
                "iShares Edge MSCI Europe Multifactor UCITS ETF EUR", "IFSE", Instrument.Type.OTHER, "ETF"));
        instruments.add(createFakeInstrument("NO0010257801", "OSLO BORS", "NOK",
                "DNB OBX", "OBXEDNB", Instrument.Type.OTHER, "ETF"));
        instruments.add(createFakeInstrument("DK0060830644", "OMX NORDIC EQUITIES", "DKK",
                "BEAR SX5E X15 NORDNET", "BEAR SX5E X15 NORDNET", Instrument.Type.OTHER, "ETN"));
        instruments.add(createFakeInstrument("US06742W4309", "NYSE ARCA", "USD",
                "Barclays Women in Leadership ETN", "WIL", Instrument.Type.OTHER, "ETN"));
        instruments.add(createFakeInstrument("SE0000869646", "OMXS", "SEK",
                "Boliden", "BOL", Instrument.Type.STOCK, "Aktie"));
        instruments.add(createFakeInstrument("FI4000074984", "OMX Helsinki", "EUR",
                "Valmet", "VALMT", Instrument.Type.STOCK, "Aktie"));
        instruments.add(createFakeInstrument("NO0010096985", "OBX Top 25", "NOK",
                "Statoil", "STL", Instrument.Type.STOCK, "Aktie"));
        instruments.add(createFakeInstrument("DK0010181759", "OMXC", "DKK",
                "Carlsberg B", "CARL B", Instrument.Type.STOCK, "Aktie"));
        instruments.add(createFakeInstrument("US0378331005", "NASDAQ", "USD",
                "Apple", "AAPL", Instrument.Type.STOCK, "Aktie"));
        instruments.add(createFakeInstrument("SE0005798329", null, "SEK",
                "Spiltan Högräntefond", null, Instrument.Type.FUND, "Fond"));
        instruments.add(createFakeInstrument("SE0001718388", null, "SEK",
                "AVANZA ZERO", null, Instrument.Type.FUND, "Fond"));
        portfolio.setInstruments(instruments);
        return portfolio;
    }

    private Instrument createFakeInstrument(String ISIN, String marketplace, String currency, String name,
            String ticker, Instrument.Type type, String rawType) {
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

}
