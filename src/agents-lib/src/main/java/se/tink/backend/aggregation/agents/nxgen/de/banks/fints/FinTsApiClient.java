package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.HKSPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SepaAccountGuesser;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth.HKIDN;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth.HKSYN;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.auth.HKVVB;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.dialog.HKEND;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.saldo.HKSAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement.HKKAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement.MT940Statement;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsAccountTypeConverter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsParser;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.AccountTypes;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FinTsApiClient {
    private final TinkHttpClient apiClient;
    private final FinTsConfiguration configuration;
    private final SepaAccountGuesser sepaAccountGuesser;
    private int messageNumber = 1;
    private String dialogId = "0";
    private String systemId = "0";
    private List<String> tanCapability;
    private int hksalVersion = 6;
    private int hkkazVersion = 6;
    // We need to get full information of account in two calls, so need to cache here.
    private List<SEPAAccount> sepaAccounts;
    private static final Logger LOGGER = LoggerFactory.getLogger(FinTsApiClient.class);
    private static final AggregationLogger LONGLOGGER = new AggregationLogger(FinTsApiClient.class);

    private boolean endDateSupported = true;

    private final PersistentStorage persistStorage;

    public FinTsApiClient(TinkHttpClient apiClient, FinTsConfiguration configuration, PersistentStorage persistStorage) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        sepaAccounts = new ArrayList<>();
        this.sepaAccountGuesser = new SepaAccountGuesser();
        this.persistStorage = persistStorage;
    }

    private FinTsResponse sendMessage(FinTsRequest message) {
        String b64Request = Base64.getEncoder().encodeToString(message.toString().getBytes());
        String b64Response =
                apiClient.request(configuration.getEndpoint()).post(String.class, b64Request);

        String plainResponse = new String(Base64.getDecoder().decode(b64Response.replaceAll("\\R", "")),
                StandardCharsets.ISO_8859_1);
        FinTsResponse response = new FinTsResponse(plainResponse);

        this.messageNumber++;

        return response;
    }

    private FinTsRequest getMessageSync() {
        HKIDN segIdentification =
                new HKIDN(3, configuration.getBlz(), configuration.getUsername(), "0");
        HKVVB segPrepare = new HKVVB(4, persistStorage.get(FinTsConstants.Storage.REG_NUMBER));
        HKSYN segSync = new HKSYN(5);
        this.messageNumber = 1;
        return new FinTsRequest(
                configuration, dialogId, this.messageNumber, this.systemId, segIdentification, segPrepare, segSync);
    }

    private FinTsRequest getMessageInit() {
        HKIDN segIdentification =
                new HKIDN(3, configuration.getBlz(), configuration.getUsername(), this.systemId);
        HKVVB segPrepare = new HKVVB(4, persistStorage.get(FinTsConstants.Storage.REG_NUMBER));

        return new FinTsRequest(
                configuration,
                this.dialogId,
                this.messageNumber,
                this.systemId,
                this.tanCapability,
                segIdentification,
                segPrepare);
    }

    private FinTsRequest getMessageEnd() {
        return new FinTsRequest(
                configuration,
                this.dialogId,
                this.messageNumber,
                this.systemId,
                this.tanCapability,
                new HKEND(3, this.dialogId));
    }

    private String getAccountString(SEPAAccount sepaAccount, int hVersion) {
        String account;
        switch (hVersion) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            account =
                    String.join(
                            ":",
                            sepaAccount.getAccountNo(),
                            sepaAccount.getSubAccount(),
                            FinTsConstants.SegData.COUNTRY_CODE,
                            sepaAccount.getBlz());
            break;
        case 7:
            account =
                    String.join(
                            ":",
                            sepaAccount.getIban(),
                            sepaAccount.getBic(),
                            sepaAccount.getAccountNo(),
                            sepaAccount.getSubAccount(),
                            FinTsConstants.SegData.COUNTRY_CODE,
                            sepaAccount.getBlz());
            break;
        default:
            throw new IllegalArgumentException("Invalid hVersion found!");
        }
        return account;
    }

    private FinTsRequest getMessageGetBalance(SEPAAccount sepaAccount) {

        return new FinTsRequest(
                configuration,
                this.dialogId,
                this.messageNumber,
                this.systemId,
                this.tanCapability,
                new HKSAL(3, this.hksalVersion,
                        this.getAccountString(sepaAccount, this.hksalVersion)));
    }

    public Collection<String> sync() {
        FinTsResponse syncResponse = sendMessage(getMessageSync());
        if (!syncResponse.isSuccess() || syncResponse.isAccountBlocked()) {
            Collection<String> rmg = syncResponse.getGlobalStatus().values();
            Collection<String> rms = syncResponse.getLocalStatus().values();

            return Stream.concat(rmg.stream(), rms.stream()).collect(Collectors.toList());
        }
        this.systemId = syncResponse.getSystemId();
        this.tanCapability = syncResponse.getSupportedTanMechanisms();
        this.hksalVersion = syncResponse.getHKSALMaxVersion();
        this.hkkazVersion = syncResponse.getHKKAZMaxVersion();
        this.dialogId = syncResponse.getDialogId();

        this.end();
        return Collections.emptyList();
    }

    public void end() {
        sendMessage(getMessageEnd());
        this.dialogId = "0";
        this.messageNumber = 1;
    }

    public void init() {
        FinTsResponse initResponse = sendMessage(getMessageInit());
        List<String> accounts = initResponse.findSegments(FinTsConstants.Segments.HIUPD);

        for (String account : accounts) {
            List<String> elements = FinTsParser.getSegmentDataGroups(account);
            if (!Strings.isNullOrEmpty(elements.get(1))) {
                List<String> element1Elements = FinTsParser.getDataGroupElements(elements.get(1));
                SEPAAccount sepaAccount = new SEPAAccount();
                sepaAccount.setAccountNo(element1Elements.get(0));
                sepaAccount.setBlz(element1Elements.get(3));
                sepaAccount.setIban(elements.get(2));
                sepaAccount.setCustomerId(elements.get(3));
                // Some banks don't set the account type field...
                sepaAccount.setAccountType(getAccountType(elements.get(4), elements.get(8)));
                sepaAccount.setCurrency(elements.get(5));
                sepaAccount.setAccountOwner1(elements.get(6));
                sepaAccount.setAccountOwner2(elements.get(7));
                sepaAccount.setProductName(elements.get(8));
                sepaAccount.setAccountLimit(elements.get(9));
                sepaAccount.setPermittedBusinessTransactions(elements.get(10));

                StringBuilder extension = new StringBuilder();
                extension.append(elements.get(10));
                for (int i = 11; i < elements.size(); i++) {
                    extension.append(elements.get(i));
                }
                sepaAccount.setExtensions(extension.toString());
                this.sepaAccounts.add(sepaAccount);
            }
        }

        this.dialogId = initResponse.getDialogId();
    }

    private int getAccountType(String accountType, String productName) {
        if (!Strings.isNullOrEmpty(accountType)) {
            return Integer.valueOf(accountType);
        }

        return sepaAccountGuesser.guessSepaAccountType(productName);
    }

    public boolean keepAlive() {
        FinTsRequest getAccountRequest =
                new FinTsRequest(
                        configuration,
                        this.dialogId,
                        this.messageNumber,
                        this.systemId,
                        this.tanCapability,
                        new HKSPA(3, null, null, null));
        FinTsResponse getAccountResponse = sendMessage(getAccountRequest);
        return getAccountResponse.isSuccess();
    }

    public List<SEPAAccount> getAccounts() {
        FinTsRequest getAccountRequest =
                new FinTsRequest(
                        configuration,
                        this.dialogId,
                        this.messageNumber,
                        this.systemId,
                        this.tanCapability,
                        new HKSPA(3, null, null, null));
        FinTsResponse getAccountResponse = sendMessage(getAccountRequest);
        if (!getAccountResponse.isSuccess()) {
            throw new IllegalStateException(getAccountResponse.toString());
        }
        String accounts = getAccountResponse.findSegment(FinTsConstants.Segments.HISPA);
        List<String> splits = FinTsParser.getSegmentDataGroups(accounts);
        List<String> accountList = splits.subList(1, splits.size());

        for (String account : accountList) {
            // J:IBAN:BIC:AccNo:<Something>:CountryCode:BLZ
            List<String> elements = FinTsParser.getDataGroupElements(account);
            // Sometimes we got accounts with same accountNo. That's why we need some special handling here.
            SEPAAccount targetAccount = this.sepaAccounts.stream()
                    .filter(sepaAccount -> {
                        switch (hksalVersion) {
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            return Objects.equals(sepaAccount.getAccountNo(), elements.get(3));
                        case 7:
                            return Objects.equals(sepaAccount.getIban(), elements.get(1));
                        default:
                            throw new IllegalArgumentException("Invalid hVersion found!");
                        }

                    })
                    .filter(sepaAccount -> sepaAccount.getBic() == null)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);

            targetAccount.setBic(elements.get(2));
            targetAccount.setSubAccount(elements.get(4));

            // N means this account doesn't have IBAN or BIC, which indicates it is not a checking account.
            if (Objects.equals("N", elements.get(0))) {
                // We don't know yet what type it is if the bank doesn't set the account type field
                targetAccount.setAccountType(99);
            }
        }
        return this.sepaAccounts;
    }

    public List<SEPAAccount> getSepaAccounts() {
        return this.sepaAccounts;
    }

    public void getBalance(SEPAAccount account) {
        // Defensive check
        if (!account.getExtensions().contains(FinTsConstants.Segments.HKSAL)) {
            throw new IllegalStateException("Target account does not support HKSAL");
        }
        FinTsResponse getBalanceResponse = sendMessage(this.getMessageGetBalance(account));

        if (!getBalanceResponse.isSuccess()) {
            throw new IllegalStateException(getBalanceResponse.toString());
        }

        List<String> segments = getBalanceResponse.findSegments(FinTsConstants.Segments.HISAL);

        for (String balanceSeg : segments) {
            List<String> deg = FinTsParser.getSegmentDataGroups(balanceSeg);
            SEPAAccount sepaAccount =
                    this.sepaAccounts
                            .stream()
                            .filter(acc -> Objects
                                    .equals(this.getAccountString(acc, this.hksalVersion), deg.get(1)))
                            .findFirst()
                            .orElseThrow(IllegalStateException::new);
            sepaAccount.setBalance(FinTsParser.getDataGroupElements(deg.get(4)).get(1), isBalancePositive(deg.get(4)));
        }
    }

    private boolean isBalancePositive(String balanceSegment) {
        return balanceSegment.startsWith("C");
    }

    // Sparkasse sometimes sends FinTsConstants.StatusCode.NO_ENTRY if a phone number is not active on account
    private boolean noEntry(Map<String, String> status) {
        if (status.containsValue(FinTsConstants.StatusCode.NO_ENTRY) &&
                status.containsKey(FinTsConstants.StatusMessage.NO_ACTIVE_PHONE_NUMBER_WARNING)) {
            return false;
        }
        return status.containsValue(FinTsConstants.StatusCode.NO_ENTRY);
    }

    public Collection<MT940Statement> getTransactions(String accountNo, Date start, Date end) {
        SEPAAccount targetAccount = this.sepaAccounts.stream()
                .filter(sepaAccount -> Objects.equals(sepaAccount.getAccountNo(), accountNo))
                .filter(sepaAccount -> FinTsAccountTypeConverter.getAccountTypeFor(sepaAccount.getAccountType())
                        != AccountTypes.OTHER)
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        FinTsRequest getTransactionRequest = this.createStatementRequest(targetAccount, start, end, null);

        FinTsResponse response = sendMessage(getTransactionRequest);
        String segment = response.findSegment(FinTsConstants.Segments.HIKAZ);

        Map<String, String> status = response.getLocalStatus();

        //Comdirect do not support getting transactions with an end date
        if (status.containsKey(FinTsConstants.StatusMessage.END_DATE_NOT_SUPPORTED)) {
            endDateSupported = false;
            getTransactionRequest = this.createStatementRequest(targetAccount, start, new Date(), null);
            response = sendMessage(getTransactionRequest);
            status = response.getLocalStatus();
        }

        //TODO: Need to start checking key AND value
        if (status.containsValue(FinTsConstants.StatusCode.ACCOUNT_NOT_ASSIGNED) ||
                noEntry(status) ||
                status.containsValue(FinTsConstants.StatusCode.NO_DATA_AVAILABLE) ||
                status.containsValue(FinTsConstants.StatusCode.TECHNICAL_ERROR) ||
                Strings.isNullOrEmpty(segment)
                ) {
            return Collections.emptyList();
        }

        // Any missing error code shall be caught here
        if (!response.isSuccess()) {
            throw new IllegalStateException(response.toString());
        }

        String mt940 = FinTsParser.getMT940Content(segment);
        Map<String, String> touchdowns = response.getTouchDowns(getTransactionRequest);
        List<MT940Statement> transactions = new ArrayList<>(this.parseMt940Transactions(mt940));

        // Process with touchdowns
        String seg;
        String mt940Content;
        while (touchdowns.containsKey(FinTsConstants.Segments.HKKAZ)) {
            try {
                FinTsRequest getFurtherTransactionRequest =
                        this.createStatementRequest(targetAccount, start, end,
                                touchdowns.get(FinTsConstants.Segments.HKKAZ));
                FinTsResponse furtherTransactionsResponse = sendMessage(getFurtherTransactionRequest);
                seg = furtherTransactionsResponse.findSegment(FinTsConstants.Segments.HIKAZ);
                mt940Content = FinTsParser.getMT940Content(seg);
                transactions.addAll(this.parseMt940Transactions(mt940Content));
                touchdowns = furtherTransactionsResponse.getTouchDowns(getFurtherTransactionRequest);
            } catch (Exception e) {
                continue;
            }
        }

        // Some banks does not take the end date in to account, here we check
        if (!endDateSupported) {
            Interval transactionInterval = new Interval(new DateTime(start), new DateTime(end));
            return transactions.stream()
                    .filter(transaction -> transactionInterval.contains(new DateTime(transaction.getDate())))
                    .collect(Collectors.toList());
        }  else  {
            return transactions.stream().filter(transaction -> transaction.getDate().before(end))
                    .collect(Collectors.toList());
        }
    }

    private List<MT940Statement> parseMt940Transactions(String mt940) {
        Scanner mt940Scanner = new Scanner(mt940);
        List<MT940Statement> transactions = new ArrayList<>();
        while (mt940Scanner.hasNextLine()) {
            String nextLine = mt940Scanner.nextLine();
            while (nextLine.startsWith(FinTsConstants.SegData.MT940_TURNOVER_FIELD)) {
                nextLine = this.process_61_86elements(nextLine, mt940Scanner, transactions);
            }
        }
        return transactions;
    }

    private String process_61_86elements(String nextLine, Scanner mt940Scanner, List<MT940Statement> transactions) {
        String tag61 = nextLine.substring(4);
        StringBuilder tag86 = new StringBuilder();
        if (!mt940Scanner.hasNextLine()) {
            return "";
        }
        nextLine = mt940Scanner.nextLine();
        if (nextLine.startsWith(FinTsConstants.SegData.MT940_MULTIPURPOSE_FIELD)) {
            tag86.append(nextLine.substring(4));
        }
        while (mt940Scanner.hasNextLine()) {
            nextLine = mt940Scanner.nextLine();
            if (!nextLine.startsWith(":")) {
                tag86.append(nextLine);
                continue;
            }
            break;
        }
        transactions.add(new MT940Statement(tag61, tag86.toString()));
        return nextLine;
    }

    private FinTsRequest createStatementRequest(SEPAAccount account, Date start, Date end, String touchdown) {
        return new FinTsRequest(
                configuration,
                this.dialogId,
                this.messageNumber,
                this.systemId,
                this.tanCapability,
                new HKKAZ(3,
                        this.hkkazVersion,
                        this.getAccountString(account, this.hkkazVersion),
                        start.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime(),
                        end.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime(),
                        touchdown));
    }
}
