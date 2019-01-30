package se.tink.backend.aggregation.agents.banks.seb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.banks.seb.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.DepotEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.EInvoiceListEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.ExternalAccount;
import se.tink.backend.aggregation.agents.banks.seb.model.FundAccountEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.GenericRequest;
import se.tink.backend.aggregation.agents.banks.seb.model.GiroEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.HoldingEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.InitiateBankIdRequest;
import se.tink.backend.aggregation.agents.banks.seb.model.InitiateBankIdResponse;
import se.tink.backend.aggregation.agents.banks.seb.model.InitiateRequest;
import se.tink.backend.aggregation.agents.banks.seb.model.InsuranceEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.InsuranceHoldingEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.InvestmentDataRequest;
import se.tink.backend.aggregation.agents.banks.seb.model.MatchableTransferRequestEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.PCBW2581;
import se.tink.backend.aggregation.agents.banks.seb.model.PCBW2582;
import se.tink.backend.aggregation.agents.banks.seb.model.PCBW431Z;
import se.tink.backend.aggregation.agents.banks.seb.model.PCBW4341;
import se.tink.backend.aggregation.agents.banks.seb.model.PortfolioAccountMapperEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.RequestWrappingEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.ResultInfoMessage;
import se.tink.backend.aggregation.agents.banks.seb.model.SebBankTransferRequestEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCard;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCardAccount;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCardTransaction;
import se.tink.backend.aggregation.agents.banks.seb.model.SebGiroRequest;
import se.tink.backend.aggregation.agents.banks.seb.model.SebInvoiceTransferRequestEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.SebRequest;
import se.tink.backend.aggregation.agents.banks.seb.model.SebResponse;
import se.tink.backend.aggregation.agents.banks.seb.model.SebTransaction;
import se.tink.backend.aggregation.agents.banks.seb.model.SebTransferRequestEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.ServiceInput;
import se.tink.backend.aggregation.agents.banks.seb.model.Session;
import se.tink.backend.aggregation.agents.banks.seb.model.TransferListEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.USRINF01;
import se.tink.backend.aggregation.agents.banks.seb.model.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.UserCredentials;
import se.tink.backend.aggregation.agents.banks.seb.model.UserCredentialsRequestEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.VODB;
import se.tink.backend.aggregation.agents.banks.seb.utilities.SEBBankIdLoginUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.general.GeneralUtils;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.transfer.StringNormalizerSwedish;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.backend.aggregation.utils.transfer.TransferMessageLengthConfig;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.social.security.SocialSecurityNumber;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class SEBApiAgent extends AbstractAgent implements
        RefreshTransferDestinationExecutor,
        RefreshEInvoiceExecutor,
        RefreshCheckingAccountsExecutor,
        RefreshSavingsAccountsExecutor,
        RefreshCreditCardAccountsExecutor,
        RefreshLoanAccountsExecutor,
        RefreshInvestmentAccountsExecutor,
        PersistentLogin,
        TransferExecutor {

    private static final String BASE_URL = "https://mp.seb.se";
    private static final String API_URL = "/1000/ServiceFactory/PC_BANK/";
    private static final String AUTH_URL = "/nauth2/Authentication/api/v1/bid/";

    private static final String INTERNAL_TRANSFER_URL = API_URL + "PC_BankKolla_Ny02Overfor03.asmx/Execute";
    private static final String EXTERNAL_BANK_TRANSFER_URL = API_URL + "PC_BankKolla_ny01Overfor02.asmx/Execute";
    private static final String EXTERNAL_INVOICE_TRANSFER_URL = API_URL + "PC_BankKollany01Betal_privat01.asmx/Execute";
    private static final String EXTERNAL_TRANSFER_VERIFICATION_URL = API_URL + "PC_BankKolla11Bunt01.asmx/Execute";
    private static final String EXTERNAL_TRANSFER_SIGN_URL = API_URL + "PC_BankSkapa11Signera01.asmx/Execute";
    private static final String EXTERNAL_TRANSFER_SIGN_VERIFICATION_URL = API_URL + "PC_BankUtfor11Bunt01.asmx/Execute";
    private static final String EXTERNAL_TRANSFER_UNSIGNED = API_URL + "PC_BankLista01Uppdrag01.asmx/Execute";
    private static final String EXTERNAL_TRANSFER_UPDATE = API_URL + "PC_BankUppdatera01Bunt03.asmx/Execute";

    private static final String EINVOICES_URL = API_URL + "PC_BankLista11Egfaktura01.asmx/Execute";
    private static final String EINVOICES_CHANGE_UNSIGNED_URL = API_URL + "PC_BankAndraEgfaktura01.asmx/Execute";
    private static final String EINVOICES_ADD_UNSIGNED_URL = API_URL + "PC_BankSkickaEgfaktura01.asmx/Execute";

    private static final String LOAN_URL = API_URL + "PC_BankLista01Laninfo_privat03.asmx/Execute";
    private static final String ACTIVATE_URL = API_URL + "PC_BankAktivera01Session01.asmx/Execute";
    private static final String PENDING_TRANSACTIONS_URL = API_URL + "PC_BankLista01Skydd01.asmx/Execute";
    private static final String UPCOMING_TRANSACTIONS_URL = API_URL + "PC_BankLista11Komm_uppdrag02.asmx/Execute";
    private static final String LOGOUT_URL = API_URL + "PC_BankTabort01Session01.asmx/Execute";
    private static final String ACCOUNTS_URL = API_URL + "PC_BankLista01Konton_privat01.asmx/Execute";
    private static final String ACCOUNT_TRANSACTION_URL = API_URL + "PC_BankLista01Rorelse_ftg03.asmx/Execute";
    private static final String DEPOT_URL = API_URL + "PC_BankLista11Depainnehav02.asmx/Execute";
    private static final String FUND_HOLDING_URL = API_URL + "PC_BankLista01Fond_innehav02.asmx/Execute";
    private static final String INSURANCE_LIST_URL = API_URL + "Tl_forsakringLista11Enga01.asmx/Execute";
    private static final String INSURANCE_DETAIL = API_URL + "PC_BankHamta11Savingsvarde01.asmx/Execute";

    private static final String INITIATE_BANKID_URL = AUTH_URL + "auth";
    private static final String COLLECT_BANKID_STRING_FORMAT_URL = AUTH_URL + "%s";
    private static final String INITIATE_SESSION_URL = API_URL + "PC_BankInit11Session01.asmx/Execute";

    private static final String CREDIT_CARD_ACCOUNTS_URL = API_URL + "PC_BankLista11Kortkonto01.asmx/Execute";
    private static final String CREDIT_CARD_TRANSACTIONS_URL = API_URL + "PC_BankLista11Fakt_korttran02.asmx/Execute";
    private static final String CREDIT_CARD_TRANSACTIONS_OUTSTANDING_URL =
            API_URL + "PC_BankLista11Ofakt_korttran02.asmx/Execute";

    private static final String EXTERNAL_ACCOUNTS_URL = API_URL + "PC_BankSkapa01Overfun_privat01.asmx/Execute";
    private static final String EXTERNAL_PAYMENT_SOURCE_ACCOUNTS_URL =
            API_URL + "PC_BankSkapa01Betalun_privat01.asmx/Execute";

    private static final String FIND_BG_URL = API_URL + "PC_BankHamta01BG_nummer01.asmx/Execute";
    private static final String FIND_PG_URL = API_URL + "PC_BankHamta01VerPG01.asmx/Execute";

    private static final String SEB_REFERER_BANKID_LOGIN = "/masp/mbid";

    private static final int KTO_FUNK_KOD_FOR_DEPOT_ID_MAPPING_TO_ACCOUNT_NUMBER = 3;

    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    private static final Predicate<ExternalAccount> EXTERNALACCOUNT_IS_BG_OR_PG = externalAccount ->
            externalAccount.isBankGiro() || externalAccount.isPostGiro();
    private static ObjectMapper mapper = new ObjectMapper();
    // private static final String USER_AGENT =
    // "Dalvik/1.6.0 (Linux; U; Android 4.3; Nexus 4 Build/JWR66Y) SEBapp/1.0 (os=android/4.3; app=se.seb.privatkund/5.2.1)";
    private static final String PASSWORD = "markeryd";
    private static final int MAX_ATTEMPTS = 90;

    private static final Predicate<ResultInfoMessage> ERROR_IS_BANKID_TRANSFER_SIGN_CANCELLED = input -> {
        // This is the error we get when we cancel. It is assumed this corresponds to a cancelled bankid signature.
        return Objects.equal(input.ErrorCode, "RFA6");
    };

    private static final Predicate<ResultInfoMessage> ERROR_IS_BANKID_TRANSFER_TIMEOUT = input -> Objects
            .equal(input.ErrorCode, "RFA8");

    // unused for the time being. Evaluating ignoring this error since we can still poll and succeed if bankId is signed
    private static final Predicate<ResultInfoMessage> ERROR_IS_BANKID_TRANSFER_ALREADY_IN_PROGRESS = input -> Objects
            .equal(input.ErrorCode, "RFA3");

    private static final TransferMessageLengthConfig TRANSFER_MESSAGE_LENGTH_CONFIG = TransferMessageLengthConfig.createWithMaxLength(12);

    private String customerId;
    private String userId;
    private final TinkApacheHttpClient4 client;
    private final Credentials credentials;
    private final Catalog catalog;
    private final TransferMessageFormatter transferMessageFormatter;

    // cache
    private Map<AccountEntity, Account> accountEntityAccountMap = null;

    public SEBApiAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        client = createClient();
        catalog = context.getCatalog();
        transferMessageFormatter = new TransferMessageFormatter(catalog,
                TRANSFER_MESSAGE_LENGTH_CONFIG,
                new StringNormalizerSwedish("#%*+=$@©£·…~_-/:;()&@\".,?!\'"));

    }

    /**
     * Pass in configuration of the mortgage integration to the module, so that we can inject it into api configuration
     *
     * @param configuration AggregationServiceConfiguration guaranteed to contain integrations configuration for SEB Mortgage
     */
    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(updateAccountsPerType(RefreshableItem.CHECKING_ACCOUNTS));
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return new FetchAccountsResponse(updateAccountsPerType(RefreshableItem.SAVING_ACCOUNTS));
    }


    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(updateTransactionsPerAccountType(RefreshableItem.CHECKING_TRANSACTIONS, customerId));
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return new FetchTransactionsResponse(updateTransactionsPerAccountType(RefreshableItem.SAVING_TRANSACTIONS, customerId));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return new FetchAccountsResponse(updateCreditCardAccounts());
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        try {
            return new FetchTransactionsResponse(updateCreditCardAccountsAndTransactions(request, customerId));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        return new FetchEInvoicesResponse(updateEInvoices());
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        try {
            return new FetchInvestmentAccountsResponse(updateInvestmentAccounts());
        } catch (Exception e) {
            // Don't fail the whole refresh just because we failed updating investment data but log error.
            log.error("Caught exception while updating investment data", e);
            return new FetchInvestmentAccountsResponse(Collections.emptyMap());
        }
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        try {
            return new FetchLoanAccountsResponse(updateLoans(customerId));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
       return new FetchTransactionsResponse(Collections.EMPTY_MAP);
    }


    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return new FetchTransferDestinationsResponse(updateTransferDestinations(accounts));
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private Map<AccountEntity, Account> getAccounts() {
        if (accountEntityAccountMap != null) {
            return accountEntityAccountMap;
        }

        List<AccountEntity> accounts = listCheckingAccounts(customerId);
        if (accounts == null) {
            log.info("No accounts found.");
            return Collections.emptyMap();
        }

        accountEntityAccountMap = new HashMap<>();
        for (AccountEntity account : accounts) {
            // Investment accounts are refreshed in a separate method
            if (SEBAgentUtils.guessAccountType(account.KTOSLAG_KOD) == AccountTypes.INVESTMENT) {
                continue;
            }

            accountEntityAccountMap.put(account, toTinkAccount(account));
        }

        return accountEntityAccountMap;
    }

    private List<Account> updateAccountsPerType(RefreshableItem type) {
        return getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private Map<Account, AccountFeatures> updateInvestmentAccounts() {
        Map<Account, AccountFeatures> investmentAccounts = new HashMap<>();
        investmentAccounts.putAll(updateFundHoldings());
        investmentAccounts.putAll(updateDepotAccounts());
        investmentAccounts.putAll(updateInsuranceAccounts());
        return investmentAccounts;
    }

    private Map<Account, AccountFeatures> updateInsuranceAccounts() {
        Map<Account, AccountFeatures> insuranceAccounts = new HashMap<>();
        Optional<SebResponse> sebResponse = fetchInvestments(INSURANCE_LIST_URL);

        if (!sebResponse.isPresent()) {
            return Collections.emptyMap();
        }

        SebResponse insuranceResponse = sebResponse.get();

        List<InsuranceEntity> insurances = insuranceResponse.d.VODB.getInsuranceEntities();

        if (insurances.isEmpty()) {
            return Collections.emptyMap();
        }

        insurances.forEach(insuranceEntity -> {
            Account account = insuranceEntity.toAccount();

            Portfolio portfolio = insuranceEntity.toPortfolio();

            List<Instrument> instruments = getInsuranceInstruments(insuranceEntity.getDetailUrl());
            portfolio.setInstruments(instruments);
            portfolio.setTotalProfit(instruments.stream().mapToDouble(Instrument::getProfit).sum());

            insuranceAccounts.put(account, AccountFeatures.createForPortfolios(portfolio));
        });
        return insuranceAccounts;
    }

    private List<Instrument> getInsuranceInstruments(String detailUrl) {
        if (Strings.isNullOrEmpty(detailUrl)) {
            return Collections.emptyList();
        }

        RequestWrappingEntity requestWrappingEntity = new RequestWrappingEntity();
        requestWrappingEntity.setServiceInput(
                Collections.singletonList(
                        new ServiceInput("DETAIL_URL", detailUrl)));

        InvestmentDataRequest investmentDataRequest = new InvestmentDataRequest();
        investmentDataRequest.setRequest(requestWrappingEntity);

        SebResponse detailResponse = postAsJSON(INSURANCE_DETAIL, investmentDataRequest, SebResponse.class);

        List<InsuranceHoldingEntity> holdingsEntities = detailResponse.d.VODB
                .getInsuranceHoldingEntities();

        List<Instrument> instruments = new ArrayList<>();

        Optional.ofNullable(holdingsEntities).orElse(Collections.emptyList())
                .forEach(holdingsEntity -> {
                    Optional<Instrument> instrument = holdingsEntity.toInstrument();
                    instrument.ifPresent(instruments::add);
                });

        return instruments;
    }

    private Map<Account, AccountFeatures> updateFundHoldings() {
        Map<Account, AccountFeatures> fundHoldings = new HashMap<>();
        RequestWrappingEntity requestWrappingEntity = new RequestWrappingEntity();
        requestWrappingEntity.setServiceInput(
                Collections.singletonList(
                        new ServiceInput("KUND_ID", customerId)));
        InvestmentDataRequest investmentDataRequest = new InvestmentDataRequest();
        investmentDataRequest.setRequest(requestWrappingEntity);

        SebResponse sebResponse = postAsJSON(FUND_HOLDING_URL, investmentDataRequest, SebResponse.class);
        Map<String, List<FundAccountEntity>> accountsByAccountNumber = sebResponse.d.VODB.getFundAccounts().stream()
                .collect(Collectors.groupingBy(FundAccountEntity::getAccountNumber));

        accountsByAccountNumber.forEach((String key, List<FundAccountEntity> fundAccounts) -> {
            if (fundAccounts == null) {
                return;
            }

            Optional<FundAccountEntity> optionalFundAccount = fundAccounts.stream().findFirst();

            if (!optionalFundAccount.isPresent()) {
                return;
            }

            FundAccountEntity fundAccount = optionalFundAccount.get();
            Account account = fundAccount.toAccount();
            Portfolio portfolio = fundAccount.toPortfolio();

            List<Instrument> instruments = Lists.newArrayList();
            fundAccounts.forEach(fundAccountEntity -> fundAccountEntity.toInstrument().ifPresent(instruments::add));
            portfolio.setInstruments(instruments);
            portfolio.setTotalValue(instruments.stream().mapToDouble(Instrument::getMarketValue).sum());
            portfolio.setTotalProfit(instruments.stream().mapToDouble(Instrument::getProfit).sum());

            account.setBalance(portfolio.getTotalValue());

            fundHoldings.put(account, AccountFeatures.createForPortfolios(portfolio));
        });
        return fundHoldings;
    }

    private String getDepotNumberForHolding(String depotNumber) {
        return depotNumber.replaceAll("^01[0]*", "");
    }

    private Map<Account, AccountFeatures> updateDepotAccounts() {
        Map<Account, AccountFeatures> depotAccounts = new HashMap<>();
        Optional<SebResponse> sebResponse = fetchInvestments(DEPOT_URL);

        if (!sebResponse.isPresent()) {
            return Collections.emptyMap();
        }

        SebResponse response = sebResponse.get();

        List<DepotEntity> depots = response.d.VODB.getDepots();
        List<HoldingEntity> holdings = response.d.VODB.getHoldings();

        if (depots.isEmpty() || holdings.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PortfolioAccountMapperEntity> portfolioAccountMappers = response.d.VODB.getPortfolioAccountMappers();
        // Important to map a depot to the correct account number
        Map<String, String> depotIdAccountNumberMappings = portfolioAccountMappers.stream()
                .filter(pam -> Objects.equal(KTO_FUNK_KOD_FOR_DEPOT_ID_MAPPING_TO_ACCOUNT_NUMBER, pam.getAccountCode()))
                .collect(Collectors.toMap(
                        PortfolioAccountMapperEntity::getDepotId,
                        PortfolioAccountMapperEntity::getAccountNumber));

        Map<String, List<HoldingEntity>> holdingByDepotNumber = holdings.stream()
                .collect(Collectors.groupingBy(h -> String.valueOf(h.getDepotNumber())));

        depots.forEach(depot -> {
            Account account = depot.toAccount(depotIdAccountNumberMappings.get(depot.getId()));
            Portfolio portfolio = depot.toPortfolio();

            String depotNumber = getDepotNumberForHolding(depot.getId());
            if (depotNumber.isEmpty()) {
                return;
            }

            List<Instrument> instruments = Lists.newArrayList();
            // check if we have made a mistake mapping Holding
            if (holdingByDepotNumber.get(depotNumber) == null) {
                log.warn("Null_holding_seb for depot number " + depotNumber);
            } else {
                holdingByDepotNumber.get(depotNumber).forEach(holding -> {
                    // check if holding item for some reason is null
                    if (holding == null) {
                        log.warn("Null_holding_seb item for depot number " + depotNumber);
                    } else {
                        holding.toInstrument().ifPresent(instrument -> {
                            Double estimatedAverageAcquisitionPrice = instrument.getMarketValue() / instrument.getQuantity();
                            if (Math.abs(estimatedAverageAcquisitionPrice - instrument.getAverageAcquisitionPrice()) > 1) {
                                log.warn("Possibly faulty value parsing: " + SerializationUtils.serializeToString(holding));
                            }
                            instruments.add(instrument);
                        });
                    }
                });
                portfolio.setInstruments(instruments);
                portfolio.setTotalProfit(instruments.stream().mapToDouble(Instrument::getProfit).sum());

                depotAccounts.put(account, AccountFeatures.createForPortfolios(portfolio));
            }
        });
        return depotAccounts;
    }

    private static void attachCreditCardsToAccount(Account account, List<SebCreditCard> creditCards) {
        if (creditCards != null) {
            List<String> creditCardNumbers = Lists.transform(creditCards, input -> input.CARD_NO);

            Set<String> subAccounts = AccountUtils.getSubAccounts(account);
            subAccounts.addAll(creditCardNumbers);

            AccountUtils.setSubAccounts(account, subAccounts);
        }
    }

    // private static final String BASE_URL = "http://dev:9443";

    /*
     * A credit card account can have several different sub-accounts over time (e.g. when your credit card is
     * invalidated and you get a new one). Hence, using the credit card number as bank id will cause duplicate accounts.
     * Instead, the credit card number is matched towards a list of one or many sub-accounts stored in `Account.payload`
     * (`sub_accounts=[111111******1111,222222******2222]`), connected to the same credit account.
     *
     * When we refresh, we fetch the last bill and the current period (yet unbilled). If you get a new card, your last
     * bill will still be attached to your old card. That's the key to connect the new card to the existing credit
     * account: You need a refresh where information for both accounts are updated at the same time.
     *
     * Example: I have card A connected (`sa=[A]`). I get a new card (B). I make a refresh, that would include [A,B]
     * (since my last bill is for A). B won't match anything, but A will. The payload will be updated with the new card
     * (`sa=[A,B]`). Next month, I will only get information about B, so that will match [A,B] and then update the
     * payload to `sa=[B]`, and the transition to the new credit card is done.
     *
     * NB! This requires transactions on both credit cards (since the card number is attached to the transaction, and
     * not the credit account) and that the credentials are updated monthly, so that you actually get the [A,B] update.
     * After that we have no way of connecting the two cards to the same credit account (when you only get B).
     *
     * If no match is found (i.e. first update, you actually have a new credit account or if the transition with both
     * accounts at the same time was missed), we create a new random bank id. After that, the account matching relies
     * on the above routine.
     *
     * If a credit card transition is missed (i.e. new credit card number, but connected to the same credit account),
     * it has to be solved manually by the user by inactivating (NB! not excluding!) the old account.
     */
    private static Optional<String> getBankIdForCreditCardAccount(CredentialsRequest request, Account account) {
        String bankId = null;
        Set<String> subAccounts = AccountUtils.getSubAccounts(account);

        /** Without sub-accounts, the account cannot be identified.
         *  This occurs when there haven't been any transactions on the credit card account for more than 2 months.
         */
        if (subAccounts.size() > 0) {
            for (String subAccount : subAccounts) {
                String tmpBankId = AccountUtils.getBankIdBySubAccount(request.getAccounts(), subAccount);
                if (!Strings.isNullOrEmpty(tmpBankId)) {
                    bankId = tmpBankId;
                    break;
                }
            }

            // Random bank id assignment is intentional. See method comment above.
            if (Strings.isNullOrEmpty(bankId)) {
                bankId = StringUtils.generateUUID();
            }
        }

        return Optional.ofNullable(bankId);
    }

    /*
     * @return customer id from SEB
     */
    private USRINF01 activate() throws AuthorizationException {

        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("CUSTOMERTYPE", "P"));
        payload.request.VODB = new VODB();

        String sebResponseContent = postAsJSON(ACTIVATE_URL, payload, String.class);

        SebResponse response;
        try {
            response = mapper.readValue(sebResponseContent, SebResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (response.d == null || response.d.VODB == null) {
            if (response.x.systemcode == 2) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(UserMessage.MUST_AUTHORIZE_BANKID.getKey());
            } else {
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - SEB - Login failed with system code %s, "
                                        + "system message %s, first error message %s",
                                response.x.systemcode,
                                response.x.systemmessage,
                                response.getFirstErrorMessage()));
            }
        } else {
            return response.d.VODB.USRINF01;
        }
    }

    private void loginWithBankId() throws BankIdException {
        InitiateBankIdRequest initiateBankIdRequest = new InitiateBankIdRequest();
        initiateBankIdRequest.setUid(credentials.getField(Field.Key.USERNAME));
        initiateBankIdRequest.setSebReferer(SEB_REFERER_BANKID_LOGIN);
        InitiateBankIdResponse initiateBankIdResponse = resource(INITIATE_BANKID_URL)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(InitiateBankIdResponse.class, initiateBankIdRequest);


        final String rfa = initiateBankIdResponse.getRfa().toLowerCase();
        final String status = initiateBankIdResponse.getStatus().toLowerCase();

        if (Objects.equal(rfa, SEBBankIdLoginUtils.COLLECT_BANKID)) {
            // noop
        } else if (Objects.equal(rfa, SEBBankIdLoginUtils.ALREADY_IN_PROGRESS)) {
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        } else if (Objects.equal(status, SEBBankIdLoginUtils.ALREADY_IN_PROGRESS_STATUS) && rfa.isEmpty()) {
            log.warn(
                    String.format(
                            "#login-already-in-progress-empty-rfa-e0 - Rfa: %s, Status: %s, Message: %s",
                            rfa, status, initiateBankIdResponse.getMessage()));
            throw BankIdError.ALREADY_IN_PROGRESS.exception();
        } else {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Rfa: %s, Status: %s, Message: %s",
                            rfa, status, initiateBankIdResponse.getMessage()));
        }

        supplementalRequester.openBankId(null, false);

        collectBankId(initiateBankIdResponse);
    }

    private void collectBankId(InitiateBankIdResponse bankIdResponse) throws BankIdException {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            bankIdResponse = resource(String.format(COLLECT_BANKID_STRING_FORMAT_URL,
                    bankIdResponse.getNextRequest().getUri())).post(InitiateBankIdResponse.class);

            switch (bankIdResponse.getRfa().toLowerCase()) {
            case SEBBankIdLoginUtils.START_BANKID:
            case SEBBankIdLoginUtils.USER_SIGN:
                break;
            case SEBBankIdLoginUtils.ALREADY_IN_PROGRESS:
                throw BankIdError.ALREADY_IN_PROGRESS.exception();
            case SEBBankIdLoginUtils.USER_CANCELLED:
                throw BankIdError.CANCELLED.exception();
            case SEBBankIdLoginUtils.NO_CLIENT:
                throw BankIdError.NO_CLIENT.exception();
            case SEBBankIdLoginUtils.AUTHENTICATED:
                return;
            case SEBBankIdLoginUtils.AUTHORIZATION_REQUIRED:
                throw BankIdError.AUTHORIZATION_REQUIRED.exception(UserMessage.MUST_AUTHORIZE_BANKID.getKey());
            default:
                throw new IllegalStateException(
                        String.format(
                                "#login-refactoring - Rfa: %s, Status: %s, Message: %s",
                                bankIdResponse.getRfa(),
                                bankIdResponse.getStatus(),
                                bankIdResponse.getMessage()));
            }

            Uninterruptibles.sleepUninterruptibly(2000, TimeUnit.MILLISECONDS);
        }

        throw BankIdError.TIMEOUT.exception();
    }

    private TinkApacheHttpClient4 createClient() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            InputStream stream = Files.asByteSource(new File("data/agents/seb/seb.p12")).openStream();
            keyStore.load(stream, PASSWORD.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, PASSWORD.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

            ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();

            SSLSocketFactory factory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager manager = new BasicClientConnectionManager();

            Scheme https = new Scheme("https", 443, factory);
            manager.getSchemeRegistry().register(https);

            config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, manager);

            BasicHttpParams params = new BasicHttpParams();
//            HttpHost proxy = new HttpHost("127.0.0.1", 8888);
//            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            config.getProperties().put(ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS, params);

            config.getProperties().put(CoreConnectionPNames.SO_TIMEOUT, 30000);

            return clientFactory.createCustomClient(context.getLogOutputStream(), config);
        } catch (Exception e) {
            log.error("Caught exception while creating client", e);
            return null;
        }
    }

    // TODO: handle more than 20 pending
    private List<SebTransaction> listAccountPendingTransactions(String customerId, String accountId) {
        SebRequest payload = new SebRequest();
        payload.request.VODB = new VODB();
        PCBW431Z query = new PCBW431Z();
        query.SEB_KUND_NR = customerId;
        query.KONTO_NR = accountId;
        payload.request.VODB.PCBW431Z = query;

        SebResponse response = postAsJSON(PENDING_TRANSACTIONS_URL, payload, SebResponse.class);

        return response.d.VODB.PCBW4311;
    }

    /**
     * Requests the upcoming pending transactions for a certain account, note difference from pending.
     * These are e.g. signed invoices
     */
    private List<UpcomingTransactionEntity> listUpcomingTransactions(String customerId) {
        SebRequest payload = new SebRequest();
        List<ServiceInput> serviceInput = payload.request.ServiceInput;
        serviceInput.add(new ServiceInput("SEB_KUND_NR", customerId));
        serviceInput.add(new ServiceInput("MAX_ROWS", 110));

        SebResponse response = postAsJSON(UPCOMING_TRANSACTIONS_URL, payload, SebResponse.class);

        if (response.d.VODB.UpcomingTransactions != null) {
            return FluentIterable
                    .from(response.d.VODB.UpcomingTransactions)
                    .filter(Predicates.<UpcomingTransactionEntity>notNull())
                    .toList();
        } else {
            return Lists.newArrayList();
        }
    }

    private List<AccountEntity> listAccounts(String id) {
        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("KUND_ID", id));

        // listAccounts() is used by keepAlive() which is not allowed to throw exceptions, hence this try/catch block
        ClientResponse response = postAsJSON(ACCOUNTS_URL, payload, ClientResponse.class);
        try {
            if (response.getStatus() == HttpStatus.SC_OK && response.hasEntity() && response.getType()
                    .isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
                SebResponse sebResponse = response.getEntity(SebResponse.class);

                if (sebResponse.d != null && sebResponse.d.VODB != null) {
                    return sebResponse.d.VODB.getAccountEntities();
                } else {
                    return null;
                }
            }
        } finally {
            response.close();
        }

        return null;
    }

    // Since we're updating investment accounts separately we don't want to update them when updating
    // regular accounts.
    private List<AccountEntity> listCheckingAccounts(String id) {
        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("KUND_ID", id));

        SebResponse accountsResponse = postAsJSON(ACCOUNTS_URL, payload, SebResponse.class);

        if (accountsResponse.d == null || accountsResponse.d.VODB == null) {
            return Collections.emptyList();
        }

        Optional<SebResponse> optionalResponse = fetchInvestments(DEPOT_URL);

        if (!optionalResponse.isPresent()) {
            return Collections.emptyList();
        }

        SebResponse depotsResponse = optionalResponse.get();

        Set<String> depotAccounts = depotsResponse.d.VODB.getPortfolioAccountMappers().stream()
                .map(PortfolioAccountMapperEntity::getAccountNumber)
                .collect(Collectors.toSet());

        return accountsResponse.d.VODB.getAccountEntities().stream()
                .filter(a -> !depotAccounts.contains(a.KONTO_NR.trim()))
                .collect(Collectors.toList());
    }

    private Optional<SebResponse> fetchInvestments(String url) {
        InvestmentDataRequest investmentDataRequest = new InvestmentDataRequest();
        investmentDataRequest.setRequest(new RequestWrappingEntity());
        SebResponse response = postAsJSON(url, investmentDataRequest, SebResponse.class);

        if (response.d == null || response.d.VODB == null) {
            return Optional.empty();
        }

        return Optional.of(response);
    }

    private List<Transaction> listAccountTransactions(String customerId, Account account) {
        SebRequest payload = new SebRequest();
        payload.request.VODB = new VODB();
        PCBW4341 query = new PCBW4341();
        query.SEB_KUND_NR = customerId;
        query.KONTO_NR = account.getAccountNumber();
        payload.request.VODB.PCBW4341 = query;

        List<Transaction> transactions = Lists.newArrayList();
        Set<String> transactionsVerifications = Sets.newHashSet();

        while (true) {
            SebResponse response = postAsJSON(ACCOUNT_TRANSACTION_URL, payload, SebResponse.class);

            List<SebTransaction> batch = response.d.VODB.PCBW4342;

            if (batch == null) {
                break;
            }

            List<SebTransaction> sebTransactions = Lists.newArrayList();

            for (SebTransaction sebTransaction : batch) {
                String transactionHash = sebTransaction.toString();

                if (transactionsVerifications.contains(transactionHash)) {
                    continue;
                }

                sebTransactions.add(sebTransaction);
                transactionsVerifications.add(transactionHash);
            }

            transactions.addAll(parseTransactions(sebTransactions, account));

            if (isContentWithRefresh(account, transactions)) {
                break;
            }

            if (batch.size() <= 0) {
                break;
            }

            if (batch.size() < query.MAX_ROWS) {
                break;
            }

            SebTransaction last = Iterables.getLast(batch);
            query.MAX_ROWS = 50;
            query.PCB_BOKFDAT_BLADDR = last.PCB_BOKF_DATUM;
            query.TRANSLOPNR = last.TRANSLOPNR;
        }
        return transactions;
    }

    private List<Transaction> listBilledCreditCardAccountTransactions(Account account, String handle) {

        List<Transaction> transactions = Lists.newArrayList();

        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("BILL_UNIT_HDL", handle));

        SebResponse response = postAsJSON(CREDIT_CARD_TRANSACTIONS_URL, payload, SebResponse.class);

        if (response.d != null && response.d.VODB != null && response.d.VODB.PCBW3243 != null) {
            transactions.addAll(parseCreditCardTransactions(response.d.VODB.PCBW3243, response.d.VODB.PCBW3242));
            attachCreditCardsToAccount(account, response.d.VODB.PCBW3242);
        }

        return transactions;
    }

    private List<SebCreditCardAccount> listCreditCardAccounts(String customerId) {
        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("SEB_KUND_NR", customerId));

        SebResponse response = postAsJSON(CREDIT_CARD_ACCOUNTS_URL, payload, SebResponse.class);

        List<SebCreditCardAccount> accounts = Lists.newArrayList();

        if (response.d != null && response.d.VODB != null && response.d.VODB.PCBW3201 != null) {
            accounts.addAll(response.d.VODB.PCBW3201);
        }

        return accounts;
    }

    private List<Transaction> listCreditCardAccountTransactions(Account account, String handle) {

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(listOutstandingCreditCardAccountTransactions(account, handle));
        transactions.addAll(listBilledCreditCardAccountTransactions(account, handle));

        return transactions;
    }

    private List<Transaction> listOutstandingCreditCardAccountTransactions(Account account, String handle) {

        List<Transaction> transactions = Lists.newArrayList();

        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("BILL_UNIT_HDL", handle));

        SebResponse response = postAsJSON(CREDIT_CARD_TRANSACTIONS_OUTSTANDING_URL, payload, SebResponse.class);

        if (response.d != null && response.d.VODB != null && response.d.VODB.PCBW3241 != null) {
            transactions.addAll(parseCreditCardTransactions(response.d.VODB.PCBW3241, response.d.VODB.PCBW3242));
            attachCreditCardsToAccount(account, response.d.VODB.PCBW3242);
        }

        return transactions;
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        switch (credentials.getType()) {
        case MOBILE_BANKID:
            loginWithBankId();
            break;
        default:
            throw new IllegalStateException("Credentials type not implemented.");
        }

        initiateConnection();

        USRINF01 userinfo = activate();
        customerId = userinfo.SEB_KUND_NR;
        userId = userinfo.IMS_SHORT_USERID;
        Preconditions.checkNotNull(customerId);
        Preconditions.checkNotNull(userId);
        checkLoggedInCustomerId(customerId);

        return true;
    }

    // Make sure the user always logs in to the same login to not mistakenly suddenly get the wrong transactions.
    private void checkLoggedInCustomerId(String bankCustomerId) throws LoginException {
        final String customerIdString = bankCustomerId;
        final String previousCustomerId = credentials.getPayload();
        if (previousCustomerId != null) {
            if (!Objects.equal(customerIdString, previousCustomerId)) {
                throw LoginError.NOT_CUSTOMER.exception(UserMessage.WRONG_BANKID.getKey());
            }
        }
        credentials.setPayload(customerIdString);
        systemUpdater.updateCredentialsExcludingSensitiveInformation(credentials, false);
    }

    @Override
    public void logout() throws Exception {
        postAsJSON(LOGOUT_URL, "{\"request\":{\"__type\":\"SEB_CS.SEBCSService\"}}", String.class);

        try {
            resource("/logout").get(String.class);
        }
        catch (Exception e) {
            log.error("Could not log out from SEB.", e);
        }
    }

    private void initiateConnection() throws AuthorizationException, AuthenticationException {
        InitiateRequest initiateRequest = new InitiateRequest();
        initiateRequest.setRequest(
                new UserCredentialsRequestEntity());
        initiateRequest.getRequest().setUserCredentials(
                new UserCredentials(credentials.getField(Field.Key.USERNAME).substring(2)));
        SebResponse sebResponse;
        try {
            sebResponse = postAsJSON(
                    INITIATE_SESSION_URL,
                    initiateRequest,
                    SebResponse.class);
        } catch (UniformInterfaceException e) {
            // Check if the user is younger than 18 and then throw unauthorized exception.
            SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(
                    credentials.getField(Field.Key.USERNAME));
            if (!ssn.isValid()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            if (e.getResponse().getStatus() == 403 &&
                    ssn.getAge(LocalDate.now(ZoneId.of("CET"))) < 18) {
                throw AuthorizationError.UNAUTHORIZED.exception(UserMessage.DO_NOT_SUPPORT_YOUTH.getKey());
            }

            throw e;
        }


        // It's sufficient to check that the following is fulfilled
        Preconditions.checkNotNull(sebResponse.d);
        Preconditions.checkNotNull(sebResponse.d.VODB);
        Preconditions.checkNotNull(sebResponse.d.VODB.RESULTO01);
        String initResult = sebResponse.d.VODB.RESULTO01.getInitResult();
        if (initResult != null && !Objects.equal(initResult.toLowerCase(), "ok")) {
            throw new IllegalStateException(
                    String.format(
                            "#login-refactoring - Expected initResult `ok` but received: %s",
                            initResult));
        }
        Preconditions.checkNotNull(initResult);
    }

    private List<Transaction> parseCreditCardTransaction(List<? extends SebCreditCardTransaction> transactionEntities,
            List<SebCreditCard> creditCardEntities) {

        List<Transaction> transactions = Lists.newArrayList();

        for (SebCreditCardTransaction transactionEntity : transactionEntities) {

            Transaction transaction = new Transaction();
            try {

                // Amount. Go from minor unit (e.g. öre) to major unit (e.g. kronor) and change sign.
                double amount = 0;

                if (transactionEntity.ADV_AMT != 0) {
                    amount = -transactionEntity.ADV_AMT / Math.pow(10, transactionEntity.AMT_DEC_QUANT);
                }

                // Date. Use the original date (i.e. when the transaction was actually made) but fall back on accounting date.
                String date = transactionEntity.ORIG_AMT_DATE;

                if (Strings.isNullOrEmpty(date)) {
                    date = transactionEntity.POSTING_DATE;
                }

                // If there is no date, we cannot handle the transaction, leave it.
                if (Strings.isNullOrEmpty(date)) {
                    continue;
                }

                // Type. Normally credit card, except the bill payments.
                TransactionTypes type = TransactionTypes.CREDIT_CARD;

                if (Strings.isNullOrEmpty(transactionEntity.CARD_NO)) {
                    type = TransactionTypes.DEFAULT;
                }

                transaction.setDescription(transactionEntity.SE_NAME);
                transaction.setAmount(amount);
                transaction.setDate(SEBAgentUtils.parseDate(date));
                transaction.setType(type);

                if (!Strings.isNullOrEmpty(transactionEntity.CARD_NO)) {
                    String cardHolder = SEBAgentUtils
                            .getCreditCardHolder(transactionEntity.CARD_NO, creditCardEntities);

                    String subAccount;
                    if (!Strings.isNullOrEmpty(cardHolder)) {
                        subAccount = String.format("%s (%s)", cardHolder, transactionEntity.CARD_NO);
                    } else {
                        subAccount = transactionEntity.CARD_NO;
                    }

                    transaction.setPayload(TransactionPayloadTypes.SUB_ACCOUNT, subAccount);
                }
            } catch (Exception e) {
                log.warn("Unable to parse credit card transaction");
                continue;
            }
            transactions.add(transaction);
        }
        return transactions;
    }

    private List<Transaction> parseCreditCardTransactions(List<? extends SebCreditCardTransaction> transactionEntities,
            List<SebCreditCard> creditCardEntities) {

        return parseCreditCardTransaction(transactionEntities, creditCardEntities);
    }

    private List<Transaction> parsePendingTransactions(List<SebTransaction> transactionEntities, Account account) {
        return parseTransactions(transactionEntities, account, true);
    }

    private List<Transaction> parseTransactions(List<SebTransaction> transactionEntities, Account account) {
        return parseTransactions(transactionEntities, account, false);
    }

    private List<Transaction> parseTransactions(List<SebTransaction> transactionEntities, Account account,
            boolean pending) {
        List<Transaction> transactions = Lists.newArrayList();
        try {
            for (SebTransaction transactionEntity : transactionEntities) {
                Transaction t = new Transaction();

                double amount = StringUtils.parseAmount(transactionEntity.ROR_BEL);
                if (pending) {
                    amount = StringUtils.parseAmount(transactionEntity.BELOPP) * -1;
                }

                String dateString = transactionEntity.PCB_VALUTA_DATUM;
                if (dateString == null) {
                    dateString = transactionEntity.DATUM;
                }
                if (dateString == null) {
                    dateString = transactionEntity.PCB_BOKF_DATUM;
                }
                if (dateString == null || dateString.length() == 0) {
                    continue;
                }

                SEBAgentUtils.DateAndDescriptionParser parser = new SEBAgentUtils.DateAndDescriptionParser(dateString,
                        transactionEntity.KK_TXT, transactionEntity.ROR_X_INFO);

                try {
                    parser.parse();
                } catch (Exception e) {
                    log.error("Could not parse date");
                }

                String description = parser.getDescription();
                Date date = parser.getDate();

                // Handle transactions made abroad differently.
                if (transactionEntity.ROR_TYP != null && transactionEntity.ROR_X_INFO != null) {
                    // Is the transaction made abroad?
                    if (transactionEntity.ROR_TYP.equals("5") && !transactionEntity.ROR_X_INFO.isEmpty()) {
                        SEBAgentUtils.AbroadTransactionParser foreignParser = new SEBAgentUtils.AbroadTransactionParser(
                                dateString,
                                transactionEntity.ROR_X_INFO, transactionEntity.KK_TXT);
                        try {
                            foreignParser.parse();
                            description = foreignParser.getDescription();
                            t.setPayload(TransactionPayloadTypes.LOCAL_REGION, foreignParser.getRegion());
                            t.setPayload(TransactionPayloadTypes.LOCAL_CURRENCY, foreignParser.getLocalCurrency());
                            t.setPayload(TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY,
                                    String.valueOf(foreignParser.getLocalAmount()));
                            t.setPayload(TransactionPayloadTypes.EXCHANGE_RATE,
                                    String.valueOf(foreignParser.getExchangeRate()));
                        } catch (Exception e) {
                            log.error("Could not parse foreign transaction");
                        }
                    }
                }

                t.setDescription(description);
                t.setDate(date);
                t.setAmount(amount);
                t.setPending(pending);
                t.setType(SEBAgentUtils.getTransactionType(transactionEntity.VERIF_NR));

                transactions.add(t);
            }
        } catch (Exception e) {
            log.error("Could not parse transaction from SEB", e);
        }
        return transactions;
    }

    private Account toTinkAccount(AccountEntity accountEntity) {
        Account account = new Account();

        account.setBalance(parseAmount(accountEntity.BOKF_SALDO));
        account.setAvailableCredit(parseAmount(accountEntity.DISP_BEL));

        // if there is a credit on this account

        if (!Strings.isNullOrEmpty(accountEntity.KREDBEL)) {
            double credit = parseAmount(accountEntity.KREDBEL);
            account.setBalance(account.getAvailableCredit() - credit);
        }

        if (!Strings.isNullOrEmpty(accountEntity.KTOBEN_TXT)) {
            account.setName(StringUtils.firstLetterUppercaseFormatting(accountEntity.KTOBEN_TXT));
        } else {
            account.setName(StringUtils.firstLetterUppercaseFormatting(accountEntity.KTOSLAG_TXT));
        }

        account.setAccountNumber(accountEntity.KONTO_NR);
        account.putIdentifier(new SwedishIdentifier(accountEntity.KONTO_NR));
        account.setBankId(account.getAccountNumber());

        Preconditions.checkState(
                Preconditions.checkNotNull(account.getBankId()).matches(
                        REGEXP_OR_JOINER.join("[0-9]{11}", "[0-9a-f]{32}")),
                "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

        // Determine the account type.

        Integer accountTypeCode = accountEntity.KTOSLAG_KOD;

        AccountTypes type = SEBAgentUtils.guessAccountType(accountTypeCode);

        if (type == null) {
            String accountTypeDescription = accountEntity.KTOSLAG_TXT;
            log.warn("Unknown account-product type: " + accountTypeCode + " = " + accountTypeDescription);
            type = AccountTypes.CHECKING;
        }
        account.setType(type);

        return account;
    }

    private Pair<Account, List<Transaction>> updateAccountInformation(AccountEntity accountEntity, List<Transaction> upcomingAccountTransactions,
                                          String customerId) {

        Account account = toTinkAccount(accountEntity);

        // Fetch the transaction list.

        List<Transaction> transactions = Lists.newArrayList();

        List<SebTransaction> pendingEntities = listAccountPendingTransactions(customerId, account.getAccountNumber());

        if (pendingEntities != null) {
            transactions.addAll(parsePendingTransactions(pendingEntities, account));
        }

        transactions.addAll(listAccountTransactions(customerId, account));

        transactions.addAll(upcomingAccountTransactions);

        return new Pair<>(account, SEBAgentUtils.TRANSACTION_ORDERING.reverse().sortedCopy(transactions));
    }

    private Map<Account, List<Transaction>> updateTransactionsPerAccountType(RefreshableItem type, String customerId) {
        // Pending transactions are returned for all accounts in one request, so do it once and send the list down
        List<UpcomingTransactionEntity> upcomingTransactionEntities = listUpcomingTransactions(customerId);

        Map<Account, List<Transaction>> accountTransactions= new HashMap<>();
        getAccounts().entrySet().stream()
                .filter(set -> type.isAccountType(set.getValue().getType()))
                .map(Map.Entry::getKey)
                .forEach(account -> {
                    // Filter out upcoming transactions for each account
                    List<Transaction> accountUpcomingTransactions = UpcomingTransactionEntity
                            .toTransactionsForAccount(upcomingTransactionEntities, account);
                    Pair<Account, List<Transaction>> accountListPair =
                            updateAccountInformation(account, accountUpcomingTransactions, customerId);
                    accountTransactions.put(accountListPair.first, accountListPair.second);
                });
        return accountTransactions;
    }

    private Pair<Account, List<Transaction>> updateCreditCardAccountInformation(CredentialsRequest request,
                                                    SebCreditCardAccount accountEntity) {

        Account account = new Account();
        account.setType(AccountTypes.CREDIT_CARD);
        account.setName(SEBAgentUtils.getCreditCardAccountName(accountEntity));
        account.setBalance(accountEntity.SALDO_BELOPP != 0 ? -accountEntity.SALDO_BELOPP : 0);
        account.setAvailableCredit(accountEntity.LIMIT_BELOPP - accountEntity.SALDO_BELOPP);

        String handle = accountEntity.BILL_UNIT_HDL;

        List<Transaction> transactions = Lists
                .newArrayList(listCreditCardAccountTransactions(account, handle));

        // Must be done after transactions has been listed
        Optional<String> bankId = getBankIdForCreditCardAccount(request, account);

        if (bankId.isPresent()) {
            account.setBankId(bankId.get());

            Preconditions.checkState(
                    Preconditions.checkNotNull(account.getBankId()).matches(
                            REGEXP_OR_JOINER.join("[0-9a-f]{32}", "[0-9]{11}",
                                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")),
                    "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

            return Pair.of(account, SEBAgentUtils.TRANSACTION_ORDERING.reverse().sortedCopy(transactions));
        }
        return null;
    }

    private List<Account> updateCreditCardAccounts() {
        List<SebCreditCardAccount> creditCardAccounts = listCreditCardAccounts(customerId);
        List<Account> creditCards = Lists.newArrayList();
        for (SebCreditCardAccount creditCardAccount : creditCardAccounts) {
            creditCards.add(updateCreditCardAccount(creditCardAccount));
        }
        return creditCards;
    }

    private Account updateCreditCardAccount(SebCreditCardAccount creditCardAccount) {
        Account account = new Account();
        account.setType(AccountTypes.CREDIT_CARD);
        account.setName(SEBAgentUtils.getCreditCardAccountName(creditCardAccount));
        account.setBalance(creditCardAccount.SALDO_BELOPP != 0 ? -creditCardAccount.SALDO_BELOPP : 0);
        account.setAvailableCredit(creditCardAccount.LIMIT_BELOPP - creditCardAccount.SALDO_BELOPP);

        String handle = creditCardAccount.BILL_UNIT_HDL;

        Optional<String> bankId = fetchBankIdForCreditCardAccount(account, handle);
        bankId.ifPresent(account::setBankId);
        return account;
    }

    private Optional<String> fetchBankIdForCreditCardAccount(Account account, String handle) {
        /** The card number for the credit card account is specified on the transactions
         *  for the credit card account and not on the credit card account itself.
         *  The credit card number is used to identify which of our accounts
         *  the fetched account belongs to. There for we need to fetch
         *  the credit card transactions before saving the account.
         */
        listCreditCardAccountTransactions(account, handle);

        return getBankIdForCreditCardAccount(request, account);
    }

    private Map<Account, List<Transaction>> updateCreditCardAccountsAndTransactions(CredentialsRequest request, String customerId) {

        List<SebCreditCardAccount> accounts = listCreditCardAccounts(customerId);
        Map<Account, List<Transaction>> creditCardTransactions = new HashMap<>();
        for (SebCreditCardAccount account : accounts) {
            Pair<Account, List<Transaction>> accountListPair = updateCreditCardAccountInformation(request, account);
            if (accountListPair != null) {
                creditCardTransactions.put(accountListPair.first, accountListPair.second);
            }
        }
        return  creditCardTransactions;
    }

    private Map<Account, AccountFeatures> updateLoans(String customerId) throws JsonProcessingException, ParseException {
        Map<Account, AccountFeatures> loans = new HashMap<>();
        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("KUND_ID", customerId));

        // Getting a String response here just in order to put it on the LoanData object
        // TODO: Use SebResponse instead of String here when we remove the serialized response from LoanData
        String responseContent = postAsJSON(LOAN_URL, payload, String.class);

        SebResponse response;
        try {
            response = mapper.readValue(responseContent, SebResponse.class);
        } catch (Exception e) {
            log.error("Couldn't deserialize SEB response", e);
            return Collections.emptyMap();
        }

        if (response.d == null || response.d.VODB == null) {
            statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
        } else {
            // PCBW2581 is null if there are no mortgages
            if (response.d.VODB.PCBW2581 != null) {
                for (PCBW2581 pcbw2591 : response.d.VODB.PCBW2581) {
                    if (pcbw2591 != null) {
                        Account account = pcbw2591.toAccount();
                        Loan loan = pcbw2591.toLoan();

                        loans.put(account, AccountFeatures.createForLoan(loan));
                    }
                }
            }

            // PCBW2582 is null if there are no blanco loans
            if (response.d.VODB.PCBW2582 != null) {
                for (PCBW2582 blancoLoan : response.d.VODB.PCBW2582) {
                    if (blancoLoan == null ) {
                        continue;
                    }

                    loans.put(blancoLoan.toAccount(), AccountFeatures.createForLoan(blancoLoan.toLoan()));
                }
            }
        }
        return loans;
    }

    public Map<Account, List<TransferDestinationPattern>>  updateTransferDestinations(List<Account> accounts) {
        Map<Account, List<TransferDestinationPattern>> response = new HashMap<>();

        response.putAll(getTransferAccountDestinations(accounts));
        response.putAll(getPaymentAccountDestinations(accounts));

        return response;
    }

    private Map<Account, List<TransferDestinationPattern>> getTransferAccountDestinations(List<Account> updatedAccounts) {
        SebResponse response = getTransferAccounts(EXTERNAL_ACCOUNTS_URL);
        if (response.d == null || response.d.VODB == null) {
            log.warn("Could not retrieve external bank transfer accounts.");
            return null;
        }

        List<AccountEntity> accountEntities = response.d.VODB.accountEntities;
        // User doesn't have any accounts where external transfers are enabled
        if (accountEntities == null) {
            log.info("No internal accounts for bank transfers are available");
            return Collections.emptyMap();
        }

        List<ExternalAccount> destinationAccounts = getNonNullExternalAccounts(response);

        List<ExternalAccount> seAccounts = FluentIterable
                .from(destinationAccounts)
                .filter(Predicates.not(EXTERNALACCOUNT_IS_BG_OR_PG))
                .toList();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(accountEntities)
                .setDestinationAccounts(seAccounts)
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE, TransferDestinationPattern.ALL)
                .build();
    }

    private Map<Account, List<TransferDestinationPattern>> getPaymentAccountDestinations(List<Account> updatedAccounts) {
        SebResponse response = getTransferAccounts(EXTERNAL_PAYMENT_SOURCE_ACCOUNTS_URL);

        if (response.d == null || response.d.VODB == null) {
            log.warn("Could not retrieve external accounts for payments.");
            return Collections.emptyMap();
        }

        List<AccountEntity> accountEntities = response.d.VODB.accountEntities;
        // User doesn't have any accounts where external payments are enabled
        if (accountEntities == null) {
            log.info("No source accounts found that can be used for payments");
            return Collections.emptyMap();
        }

        List<ExternalAccount> destinationAccounts = getNonNullExternalAccounts(response);

        List<ExternalAccount> bgOrPgAccounts = FluentIterable
                .from(destinationAccounts)
                .filter(EXTERNALACCOUNT_IS_BG_OR_PG)
                .toList();

        return new TransferDestinationPatternBuilder()
                .setSourceAccounts(accountEntities)
                .setDestinationAccounts(bgOrPgAccounts)
                .setTinkAccounts(updatedAccounts)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_BG, TransferDestinationPattern.ALL)
                .addMultiMatchPattern(AccountIdentifier.Type.SE_PG, TransferDestinationPattern.ALL)
                .build();
    }

    private SebResponse getTransferAccounts(String transferAccountsUrl) {
        SebRequest payload = new SebRequest();
        payload.request.ServiceInput.add(new ServiceInput("SEB_KUND_NR", customerId));

        return postAsJSON(transferAccountsUrl, payload, SebResponse.class);
    }

    private List<ExternalAccount> getNonNullExternalAccounts(SebResponse response) {
        List<ExternalAccount> destinationAccounts = response.d.VODB.ExternalAccounts;

        if (destinationAccounts == null) {
            destinationAccounts = Lists.newArrayList();
        } else {
            destinationAccounts = FluentIterable
                    .from(response.d.VODB.ExternalAccounts)
                    .filter(Predicates.notNull())
                    .toList();
        }

        return destinationAccounts;
    }

    private Builder resource(String url) {
        return client.resource(BASE_URL + url).header("User-Agent", DEFAULT_USER_AGENT);
    }

    @Override
    public boolean isLoggedIn() throws Exception {
        // return keepAlive();
        // 2017-11-23 jrenold: Temporary solution until SEB fixes their timeout problems
        // (lalmgren is in contact with them).
        return false;
    }

    @Override
    public boolean keepAlive() throws Exception {
        /*
        if (customerId == null) {
            return false;
        }

        return listAccounts(customerId) != null;
        */
        // 2017-11-23 jrenold: Temporary solution until SEB fixes their timeout problems
        // (lalmgren is in contact with them).
        return false;
    }

    @Override
    public void persistLoginSession() {

        Session session = new Session();
        session.setCustomerId(customerId);
        session.setCookiesFromClient(client);

        credentials.setPersistentSession(session);
    }

    @Override
    public void loadLoginSession() {
        Session session = credentials.getPersistentSession(Session.class);

        if (session != null) {
            customerId = session.getCustomerId();
            addSessionCookiesToClient(client, session);
        }
    }

    @Override
    public void clearLoginSession() {
        // Clean the session in memory
        customerId = null;

        // Clean the persisted session
        credentials.removePersistentSession();

        // Clear the cookies on the client
        clearSessionCookiesFromClient(client.getClientHandler().getCookieStore());
    }

    @Override
    public void attachHttpFilters(ClientFilterFactory filterFactory) {
        filterFactory.addClientFilter(client);
    }

    @Override
    public void execute(final Transfer transfer) throws Exception,
            TransferExecutionException {
        List<AccountEntity> accounts = listAccounts(customerId);

        ensureIsValidSourceAccount(accounts, transfer);

        if (Objects.equal(transfer.getType(), TransferType.EINVOICE)) {
            throw new IllegalStateException("Should never happen, since eInvoices are run through update call");
        } else if (isInternalTransfer(transfer, accounts)) {
            executeInternalTransfer(transfer);
        } else {
            executeExternalTransfer(transfer);
        }

        // If we don't have this sleep we could get a problem when we refresh the account after a transfer/payment.
        // SEB are slow in adding the transactions in upcoming so the date could be missing if the refresh comes
        // to fast. This would make use try to parse an a date that is an empty string.
        Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void update(Transfer transfer) throws Exception, TransferExecutionException {
        if (Objects.equal(transfer.getType(), TransferType.EINVOICE)) {
            // EInvoices are always submitted before signing as EINVOICE
            approveEInvoice(transfer);
        } else {
            // EInvoices and payments are updated with type PAYMENT, which SEB doesn't support (they only support delete operation)
            // This should never happen since we don't create an updatable transfer for payments or eInvoices in SEB
            throw new IllegalStateException("Should never happen, since transfers are not updatable in SEB");
        }

        // If we don't have this sleep we could get a problem when we refresh the account after a transfer/payment.
        // SEB are slow in adding the transactions in upcoming so the date could be missing if the refresh comes
        // to fast. This would make use try to parse an a date that is an empty string.
        Uninterruptibles.sleepUninterruptibly(5000, TimeUnit.MILLISECONDS);
    }

    private void ensureIsValidSourceAccount(List<AccountEntity> accounts, Transfer transfer) {
        if (accounts == null) {
            failTransfer(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE_NO_ENTITIES));
        }

        Optional<AccountEntity> sourceAccount = GeneralUtils.find(transfer.getSource(), accounts);
        if (!sourceAccount.isPresent()) {
            failTransfer(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE));
        }

        AccountIdentifier destination = transfer.getDestination();
        if (!sourceAccount.get().isAllowedToTransferTo(destination)) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_SOURCE));
        }
    }

    private void ensureSourceAccountCanExecutePayment(Transfer transfer) {
        SebResponse response = getTransferAccounts(EXTERNAL_PAYMENT_SOURCE_ACCOUNTS_URL);
        List<AccountEntity> accountEntities = response.d.VODB.accountEntities;
        ensureIsValidSourceAccount(accountEntities, transfer);
    }

    /**
     * Note regarding post-execute of an eInvoice: They can only be signed once, then it will be gone if not visiting the
     * internet bank. At the internet bank it's possible to reset the eInvoice to get it back in the eInvoice list, at
     * which point a new execute needs to be done. (As of when this commit was created.)
     */
    private void approveEInvoice(Transfer transfer) throws Exception {
        ensureNoUnsignedTransfers();
        ensureSourceAccountCanExecutePayment(transfer);

        Transfer originalTransfer = getOriginalTransfer(transfer);
        EInvoiceListEntity matchingEInvoice = fetchMatchingEInvoice(originalTransfer);

        if (isTransferModifyingEInvoice(matchingEInvoice, transfer)) {
            matchingEInvoice = updateEInvoice(matchingEInvoice, transfer);
        }

        addEInvoiceToOutbox(matchingEInvoice);
        signEInvoice(matchingEInvoice);
    }

    private EInvoiceListEntity fetchMatchingEInvoice(final Transfer transfer) throws TransferExecutionException {

        List<EInvoiceListEntity> eInvoiceEntities = fetchEInvoiceEntities();

        Optional<EInvoiceListEntity> matchingEInvoice = eInvoiceEntities.stream().filter(input -> {
            Transfer eInvoice = EInvoiceListEntity.TO_TRANSFER.apply(input);
            return eInvoice != null && Objects.equal(eInvoice.getHash(), transfer.getHash());
        }).findFirst();

        if (!matchingEInvoice.isPresent()) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_NO_MATCHES))
                    .build();
        }

        return matchingEInvoice.get();
    }

    private static Transfer getUpdatedTransfer(Transfer transfer) throws TransferExecutionException {
        final Transfer updatedTransfer = getOriginalTransfer(transfer);
        // just in case source account is changed
        updatedTransfer.setSource(transfer.getSource());

        return updatedTransfer;
    }

    private static Transfer getOriginalTransfer(Transfer transfer) throws TransferExecutionException {
        Transfer originalTransfer = SerializationUtils.deserializeFromString(
                transfer.getPayload().get(TransferPayloadType.ORIGINAL_TRANSFER), Transfer.class);

        if (originalTransfer == null) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("No original transfer on payload to compare with.").build();
        }

        return originalTransfer;
    }

    /**
     * In SEB user can only change source account, date or amount, not OCR
     */
    private boolean isTransferModifyingEInvoice(EInvoiceListEntity eInvoiceEntity, Transfer transfer)
            throws TransferExecutionException {
        if (!Objects.equal(transfer.getDestinationMessage(), eInvoiceEntity.getDestinationMessage())) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.TRANSFER_MODIFY_MESSAGE));
        }

        Date currentDueDate;
        try {
            currentDueDate = eInvoiceEntity.getCurrentDueDate();
        } catch (ParseException e) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage("Could not parse the due date of the eInvoice. This should never happen.")
                    .build();
        }

        return !Objects.equal(transfer.getSource(), eInvoiceEntity.getSource()) ||
                !Objects.equal(DateUtils.flattenTime(transfer.getDueDate()), currentDueDate) ||
                !Objects.equal(transfer.getAmount().toBigDecimal(), eInvoiceEntity.getCurrentAmount().toBigDecimal());
    }

    private EInvoiceListEntity updateEInvoice(EInvoiceListEntity eInvoiceEntity, Transfer transfer) {
        eInvoiceEntity.setCurrentAmount(transfer.getAmount());
        eInvoiceEntity.setCurrentDueDate(transfer.getDueDate());
        eInvoiceEntity.setSource(transfer.getSource());

        SebRequest sebRequest = new SebRequest();
        sebRequest.request.VODB = new VODB();
        sebRequest.request.VODB.EInvoices = Lists.newArrayList(eInvoiceEntity);
        SebResponse sebResponse = postAsJSON(EINVOICES_CHANGE_UNSIGNED_URL, sebRequest, SebResponse.class);
        abortTransferIfErrorIsPresent(sebResponse);

        final Transfer updatedTransfer = getUpdatedTransfer(transfer);
        EInvoiceListEntity updatedEInvoiceEntity = fetchMatchingEInvoice(updatedTransfer);

        if (isTransferModifyingEInvoice(updatedEInvoiceEntity, transfer)) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.EINVOICE_MODIFY_FAILED));
        }

        return updatedEInvoiceEntity;
    }

    private void addEInvoiceToOutbox(EInvoiceListEntity eInvoiceEntity) {
        eInvoiceEntity.setState(EInvoiceListEntity.State.OUTBOX);

        SebRequest sebRequest = new SebRequest();
        sebRequest.request.VODB = new VODB();
        sebRequest.request.VODB.EInvoices = Lists.newArrayList(eInvoiceEntity);
        SebResponse sebResponse = postAsJSON(EINVOICES_ADD_UNSIGNED_URL, sebRequest, SebResponse.class);
        abortTransferIfErrorIsPresent(sebResponse);
    }

    private void signEInvoice(EInvoiceListEntity matchingEInvoice) throws Exception {
        List<TransferListEntity> unsignedTransfers = getUnsignedTransfers();
        Preconditions.checkArgument(unsignedTransfers.size() == 1, "Not expected number of transfers (!= 1)");

        TransferListEntity eInvoiceTransferEntity = unsignedTransfers.get(0);

        try {
            initSignExternalPayment();
            requestSupplementalBankId();
            ensureExternalPaymentSignedOrThrow(matchingEInvoice, eInvoiceTransferEntity);
        } catch (Exception e) {
            deleteTransferAndThrowException(eInvoiceTransferEntity, e);
        }
    }

    /**
     * If transfer destination not found within the user's own account we assume it's external
     */
    private boolean isInternalTransfer(Transfer transfer, List<AccountEntity> accounts) {
        Optional<AccountEntity> internalDestinationAccount = GeneralUtils.find(transfer.getDestination(), accounts);
        return internalDestinationAccount.isPresent();
    }

    /**
     * Transfers between the user's own accounts doesn't need signing
     */
    private void executeInternalTransfer(Transfer transfer) {
        SebTransferRequestEntity internalTransfer = SebBankTransferRequestEntity
                .createInternalBankTransfer(transfer, customerId, transferMessageFormatter);
        SebRequest request = SebRequest.createWithTransfer(TransferType.BANK_TRANSFER, internalTransfer);

        // Don't know what this means
        request.request.ServiceInput = ImmutableList.of(new ServiceInput("SMALL_AMNT_FL", "J"));

        SebResponse response = postAsJSON(INTERNAL_TRANSFER_URL, request, SebResponse.class);
        abortTransferIfErrorIsPresent(response);
    }

    /**
     * All external transfers (bill payments and bank transfers) require signing with BankID or pin code.
     *
     * The external transfers are in the app put in an outbox of transfers to sign. For that reason we cannot allow
     * the user yet to have existing payments in the outbox since that would sign this transfer + all other transfers
     * in the outbox when executing.
     */
    private void executeExternalTransfer(Transfer transfer) throws Exception {
        ensureNoUnsignedTransfers();

        SebTransferRequestEntity externalTransfer;
        if (isBgOrPg(transfer)) {
            GiroMessageValidator.ValidationResult validationResult = validateBGPGTransferOrThrow(transfer);
            boolean useMessageAsOcr = isDestinationMessageOcr(validationResult);
            externalTransfer = SebInvoiceTransferRequestEntity.createInvoiceTransfer(transfer, customerId, useMessageAsOcr);
        } else {
            externalTransfer = SebBankTransferRequestEntity.createExternalBankTransfer(transfer, customerId, transferMessageFormatter);
        }

        TransferListEntity transferQueuedUp = addSebTransferToOutbox(transfer.getType(), externalTransfer);
        signExternalPayment(externalTransfer, transferQueuedUp);
    }

    private void ensureNoUnsignedTransfers() {
        if (getUnsignedTransfers().size() > 0) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.EXISTING_UNSIGNED_TRANSFERS));
        }
    }

    private boolean isDestinationMessageOcr(GiroMessageValidator.ValidationResult validationResult) {
        switch (validationResult.getAllowedType()) {
        case MESSAGE:
            return false;
        case OCR:
            return true;
        default:
            // TODO: What to do if we have both a valid message and valid OCR in validationResult? We just prioritize OCR for now, and if not present use message instead
            return validationResult.getValidOcr().isPresent();
        }
    }

    private boolean isBgOrPg(Transfer transfer) {
        AccountIdentifier.Type destinationType = transfer.getDestination().getType();
        return Objects.equal(destinationType, AccountIdentifier.Type.SE_BG) ||
                Objects.equal(destinationType, AccountIdentifier.Type.SE_PG);
    }

    /**
     * Ensure we only find one entity for a given destination of the same type as the transfer.
     */
    private GiroMessageValidator.ValidationResult validateBGPGTransferOrThrow(Transfer transfer) {
        Optional<List<GiroEntity>> searchResult = findGiroEntity(transfer.getDestination());

        if (!searchResult.isPresent() || searchResult.get().size() != 1) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.INVALID_DESTINATION));
        }

        GiroEntity giroEntity = searchResult.get().get(0);

        GiroMessageValidator messageValidator = giroEntity.createMessageValidator();
        String destinationMessage = transfer.getDestinationMessage();

        if (destinationMessage.length() > 100) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessageParametrized
                    .INVALID_MESSAGE_WHEN_MAX_LENGTH.cloneWith(100)));
        }

        GiroMessageValidator.ValidationResult validationResult = messageValidator.validate(destinationMessage);

        boolean isValidMessage = false;
        String errorMessage = catalog.getString(TransferExecutionException.EndUserMessage.INVALID_MESSAGE);

        switch (validationResult.getAllowedType()) {
        case MESSAGE:
            isValidMessage = validationResult.getValidMessage().isPresent();
            errorMessage = catalog.getString(TransferExecutionException.EndUserMessage.INVALID_DESTINATION_MESSAGE);
            break;
        case MESSAGE_OR_OCR:
            isValidMessage = validationResult.getValidMessage().isPresent() ||
                    validationResult.getValidOcr().isPresent();
            break;
        case OCR:
            isValidMessage = validationResult.getValidOcr().isPresent();
            errorMessage = catalog.getString(TransferExecutionException.EndUserMessage.INVALID_OCR);
            break;
        }

        if (!isValidMessage) {
            cancelTransfer(errorMessage);
        }

        return validationResult;
    }

    private Optional<List<GiroEntity>> findGiroEntity(AccountIdentifier destination) {
        SebGiroRequest giroRequest = SebGiroRequest.create(destination);

        String searchUrl;
        if (destination.is(AccountIdentifier.Type.SE_BG)) {
            searchUrl = FIND_BG_URL;
        } else if (destination.is(AccountIdentifier.Type.SE_PG)) {
            searchUrl = FIND_PG_URL;
        } else {
            throw new IllegalStateException("Not a valid bgpg destination type");
        }

        SebResponse sebResponse = postAsJSON(searchUrl, giroRequest, SebResponse.class);

        if (destination.is(AccountIdentifier.Type.SE_BG)) {
            return Optional.ofNullable(sebResponse.d.VODB.FindBGResult);
        } else {
            return Optional.ofNullable(sebResponse.d.VODB.FindPGResult);
        }
    }

    private TransferListEntity addSebTransferToOutbox(TransferType transferType, SebTransferRequestEntity externalTransfer) {
        SebRequest createRequest = SebRequest.createWithTransfer(transferType, externalTransfer);

        String addTransferUrl;
        if (Objects.equal(transferType, TransferType.BANK_TRANSFER)) {
            addTransferUrl = EXTERNAL_BANK_TRANSFER_URL;
        } else {
            addTransferUrl = EXTERNAL_INVOICE_TRANSFER_URL;
        }

        SebResponse createResponse = postAsJSON(addTransferUrl, createRequest, SebResponse.class);
        abortTransferIfErrorIsPresent(createResponse);

        return createResponse.d.VODB.getTransfers().get(0);
    }

    private List<TransferListEntity> getUnsignedTransfers() {
        SebRequest sebRequest = new SebRequest();
        sebRequest.request.ServiceInput.add(new ServiceInput("USER_ID", userId));
        sebRequest.request.ServiceInput.add(new ServiceInput("UPPDRAG_TYP", "A"));
        sebRequest.request.ServiceInput.add(new ServiceInput("SORT_ORDER", ""));

        SebResponse response = postAsJSON(EXTERNAL_TRANSFER_UNSIGNED, sebRequest, SebResponse.class);

        return response.d.VODB.getTransfers();
    }

    private void signExternalPayment(SebTransferRequestEntity transfer, TransferListEntity transferQueuedUp)
            throws Exception {
        try {
            initSignExternalPayment();
            requestSupplementalBankId();
            ensureExternalPaymentSignedOrThrow(transfer, transferQueuedUp);
        } catch (Exception e) {
            deleteTransferAndThrowException(transferQueuedUp, e);
        }
    }


    private void deleteTransferAndThrowException(TransferListEntity transferQueuedUp, Exception initialException)
            throws Exception {
        try {
            if (!deleteTransferFromOutbox(transferQueuedUp)) {
                log.error("Could not clean up transfer!");
            }
        } catch (Exception deleteException) {
            log.warn("Could not delete unsigned transfer from outbox but was expecting it to be possible.",
                    deleteException);
        }

        if (initialException instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

        throw initialException;
    }

    private void initSignExternalPayment() {
        SebResponse verification = postAsJSON(EXTERNAL_TRANSFER_VERIFICATION_URL, new SebRequest(), SebResponse.class);
        abortTransferIfErrorIsPresent(verification);

        // Verification response includes a message that is displayed in SEB app. We don't use it in Tink.
        if (verification.d.VODB.TransferVerification != null) {
            log.debug(verification.d.VODB.TransferVerification.SignText);
        }

        SebResponse initiateSigningResponse = postAsJSON(EXTERNAL_TRANSFER_SIGN_URL, new SebRequest(), SebResponse.class);
        abortTransferIfErrorIsPresent(initiateSigningResponse);
    }

    private void ensureExternalPaymentSignedOrThrow(MatchableTransferRequestEntity transfer, TransferListEntity transferQueuedUp)
            throws InterruptedException {

        Uninterruptibles.sleepUninterruptibly(8000, TimeUnit.MILLISECONDS);
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            SebResponse response = postAsJSON(EXTERNAL_TRANSFER_SIGN_VERIFICATION_URL, new SebRequest(), SebResponse.class);

            if (isTransferSigned(transfer, response)) {
                return;
            }

            abortIfTransferSignatureFailed(response);

            log.debug(catalog.getString("Waiting on Mobile Bank Id"));
            Thread.sleep(2000);
        }

        cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE));
    }

    private void abortIfTransferSignatureFailed(SebResponse response) throws TransferExecutionException {
        if (FluentIterable.from(response.getErrors()).anyMatch(ERROR_IS_BANKID_TRANSFER_SIGN_CANCELLED)) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.BANKID_CANCELLED));
        } else if (FluentIterable.from(response.getErrors()).anyMatch(ERROR_IS_BANKID_TRANSFER_TIMEOUT)) {
            cancelTransfer(catalog.getString(TransferExecutionException.EndUserMessage.BANKID_NO_RESPONSE));
        }

        abortTransferIfErrorIsPresent(response);
    }

    private void failTransfer(String message) throws TransferExecutionException {
        abortTransfer(SignableOperationStatuses.FAILED, message);
    }

    private void cancelTransfer(String message) throws TransferExecutionException {
        abortTransfer(SignableOperationStatuses.CANCELLED, message);
    }

    private void abortTransfer(SignableOperationStatuses status, String message) throws TransferExecutionException {
        throw TransferExecutionException.builder(status)
                .setEndUserMessage(message)
                .setMessage(String.format("Error when executing transfer: %s", message))
                .build();
    }

    private void abortTransfer(ResultInfoMessage error) throws TransferExecutionException {
        throw TransferExecutionException.builder(error.getSignableOperationStatus())
                .setEndUserMessage(error.getErrorText())
                .setMessage(error.getDescription())
                .build();
    }

    private boolean isTransferSigned(MatchableTransferRequestEntity transfer, SebResponse response) {
        List<TransferListEntity> transferEntities = response.d.VODB.getTransfers();

        if (transferEntities.isEmpty()) {
            return false;
        }

        // This should not happen. We will still continue since the user can still sing the transfer that we created
        if (transferEntities.size() > 1) {
            log.error("User has signed multiple transfers");
        }

        for (TransferListEntity transferListEntity : transferEntities) {
            if (transfer.matches(transferListEntity)) {
                return true;
            }
        }

        return false;
    }

    private void requestSupplementalBankId() {
        supplementalRequester.openBankId(null, false);
    }

    private boolean deleteTransferFromOutbox(TransferListEntity transferQueuedUp) {
        RequestWrappingEntity deleteEntity = new RequestWrappingEntity();
        deleteEntity.setServiceInput(
                Arrays.asList(
                        new ServiceInput("USER_ID", customerId),
                        new ServiceInput("TIME_STAMP", transferQueuedUp.timeStampNkl),
                        new ServiceInput("VERIF_TIME_STAMP", transferQueuedUp.timeStampNkl)
                )
        );
        GenericRequest deleteRequest = new GenericRequest(deleteEntity);

        // attempt to delete the transfer 10 times
        for (int i=0; i<10; i++) {
            postAsJSON(EXTERNAL_TRANSFER_UPDATE, deleteRequest, SebResponse.class);
            if (getUnsignedTransfers().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void abortTransferIfErrorIsPresent(SebResponse sebResponse) throws TransferExecutionException {
        Optional<ResultInfoMessage> error = sebResponse.getFirstErrorWithErrorText();

        if (error.isPresent()) {
            abortTransfer(error.get());
        }
    }

    private <T> T postAsJSON(String url, Object entity, Class<T> responseEntityType) {
        return resource(url)
                .entity(entity)
                .type(MediaType.APPLICATION_JSON)
                .post(responseEntityType);
    }

    public List<Transfer> updateEInvoices() {
        List<EInvoiceListEntity> eInvoiceEntities = fetchEInvoiceEntities();

        return Lists.newArrayList(FluentIterable
                .from(eInvoiceEntities)
                .transform(EInvoiceListEntity.TO_TRANSFER));
    }

    private List<EInvoiceListEntity> fetchEInvoiceEntities() throws IllegalStateException {
        SebRequest request = SebRequest.withSEB_KUND_NR(customerId).build();
        SebResponse sebResponse = postAsJSON(EINVOICES_URL, request, SebResponse.class);

        Preconditions.checkState(!sebResponse.hasErrors(),
                String.format("Error fetching SEB eInvoices: %s" ,sebResponse.getFirstErrorMessage().orElse(null)));

        return sebResponse.d.VODB.getEInvoices();
    }



    private enum UserMessage implements LocalizableEnum {
        MUST_AUTHORIZE_BANKID(new LocalizableKey("The first time you use your mobile BankId you have to verify it with your Digipass. Login to the SEB-app with your mobile BankID to do this.")),
        WRONG_BANKID(new LocalizableKey("Wrong BankID signature. Did you log in with the wrong personnummer?")),
        DO_NOT_SUPPORT_YOUTH(new LocalizableKey("It looks like you have SEB Ung. Unfortunately we currently only support SEB's standard login."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }
        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
