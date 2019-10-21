package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.ApiResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderApiClient {

    private final TinkHttpClient tinkHttpClient;
    private final SessionStorage persistentStorage;
    private final Parser parser;

    public SantanderApiClient(TinkHttpClient tinkHttpClient, SessionStorage persistentStorage) {
        this.tinkHttpClient = tinkHttpClient;
        this.persistentStorage = persistentStorage;
        this.parser = new Parser();
    }

    public ApiResponse fetchAuthToken(String login, String password) {
        String rawResponse =
                tinkHttpClient
                        .request(SantanderConstants.API_URL)
                        .header(
                                "MORE-Control",
                                "|110.5.5|1version|1process|1service|12.13.24.2|1appVersion|1login|1method|D4")
                        .body(
                                String.format(
                                        "|0|0|50|1encoder.compress|1%s|1user|12.13.24.2|1appVersion|11||1||3||10|1timezone|1en|1language|1|1group|1%s|1password|1|1key|12C5AFE4B-85FC-4971-A258-2D0821648594|1deviceUUID|1f6e01ffef77fe05d908faecc23adc2b6||2019-10-17 22:13:35||D|1modelKey|1-1|1encoder.maxsize|311|1channel|110.5.5|1version|1BT|1orgunit|1SANTANDER-channel-objectivec-ios; 2.13.24.2; en_SE; Europe/Warsaw; iPhone; 12.4|1userAgent|D15|L1|30|L4",
                                        escapeString(login), escapeString(password)))
                        .post(String.class);

        return parser.parseResponse(rawResponse);
    }

    public ApiResponse fetchAccounts() {
        String rawResponse =
                tinkHttpClient
                        .request(SantanderConstants.API_URL)
                        .header(
                                "MORE-Control",
                                String.format(
                                        "|1process|1service|1%s|1session|1executeFlow|1method|51|1execute|1|1callId|D5",
                                        persistentStorage.get(STORAGE.SESSION_TOKEN)))
                        .body(
                                "||1time|1n024156573|1flow|0|1kernel|D3|0|1readPage|1$operation|D1|L1|30|L4")
                        .post(String.class);
        return parser.parseResponse(rawResponse);
    }

    public ApiResponse fetchTransactions(
            String accountNumber,
            String branchCode,
            Date dateFrom,
            Date dateTo,
            int pageNumber,
            int pageSize) {

        SimpleDateFormat requestDateFormat = new SimpleDateFormat(SantanderConstants.DATE_FORMAT);

        String rawResponse =
                tinkHttpClient
                        .request(SantanderConstants.API_URL)
                        .header(
                                "MORE-Control",
                                String.format(
                                        "|1process|1service|1%s|1session|1executeFlow|1method|51|1execute|1|1callId|D5",
                                        persistentStorage.get(STORAGE.SESSION_TOKEN)))
                        .body(
                                String.format(
                                        "|1m6068f9ba7|1flow|420191016131838810|1time|1|1value|1flow||f6a8d54732|1name|D2|L1|1analytics|10086092607|1kernel|D4|0|1|1lastLoginDate|L0|1selectedAdvertisement|1false|1over90Days|1|1bankAccount|1|1minorUsr|1|1OTPRequested|1u2b294c3f4|1lastTabIndex|1False|1nextDayTransfer|1|1hour|1|1date|D3|L1|1operationDateResult|L0|1selectedOption|0|1advertisements|D0|1filterForm|6%s|1pageSize|1|1firstFlux|1transactions|1returnName|1true|1movments|1modalFullScreen|1u1264e9491.layout|11|1analytics|1true|1fromAccounts|1account|1typeOfPush|1|L1|L0|D0|L1|1%s|1accountNumber|1|1bankAccount|1|1accountAlias|1|1formattedAccount|155|1checkDigit|1|1bankCode|1|1branchCode|D7|L1|1|1USER_ACCOUNTS_TIMESTAMP|L0|1USER_ACCOUNTS|0|1isWorld123Client|1|1clientSegment|0|1needsAcceptance|D5|L1|L5|1loginResult|50|1has_sca|D1|L1|1sess_key_status|0|1accountsInfo|1|1_isOTP|1|1defaultAccount|1|1accountDescription|1%s|1branchCode|1|1formattedBankAccount|1|1numAuthorizedBalance|1|1closingBalance|1%s|1accountNumber|1|1bankAccount|1|1numAvailableBalance|1Available Balance|1c219ad3922|1|1number|1TOTAPTPL|1BIC|1|1SMS_MESSAGE|1|1formattedIBAN|1|1IBAN|1CASA|1accountAlias|1N|1isPrefered|1Accounting Balance:|1w4395e00f1|13|1index|1df3a81f55e|1code|21883|I|1data|1df3a81f55e|1name|D3|1b3f97ad03e|D20|L1|1selectedAccount|1k4b0f16e41|1z644851d52.action|0|1tec118464b|0|1smsValue|1Confirmation SMS|1e897e2b784|1|1m71aa29d2e|1otpRequestMessageV2|1otpRequestMessage|D5|1smsFormData|13|1selectedIndex|1readPage|1$operation|L0|1bySideToken|1related|1back.type|50|1privacyMode|D1|1settings|L0|1checkingAccounts|1|1USER_ACCOUNTS_TIMESTAMP|L0|1USER_ACCOUNTS|0|1isWorld123Client|1PB|1clientSegment|1|1lastLogin|50|1needsAcceptance|D6|L1|1sessionResult|6%s|1page|50|1privacyMode|1custom|1filterValue|1|1accountNumber|1|1bankAccount|1|1accountAlias|1|1formattedAccount|1|1checkDigit|1|1bankCode|10003|1branchCode|D7|L1|1splitAccountResult|L0|1selectedAddressee|L0|D0|L1|1False|1nextDayTransfer|1|1hour|1|1date|D3|L1|L3|1exec|4%s|1filterEndDate|1pf562adfd9|1selectedMenu|1|1formattedNIB|0|1$entity|1w2296e8694|1$caller|1|1z644851d52|131129999|1value.1|101011900|1value.0|1true|1post213Version|1operationsMenu|1operationsMenu|1|1selectedIBAN|4%s|1filterStartDate|1|1forceCall|1|1sendVariables|1|1currency|1|1closingBalance|1|1availableBalance|1|1numAuthorizedBalance|1|1authorizedBalance|1|1SMS_MESSAGE|1|1currencySimbol|1|1numAvailableBalance|1|1numClosingBalance|D9|L1|1getBalanceResult|1td0d9e00bf|1w2296e8694.readPage|D55|L1|30|L4",
                                        pageSize,
                                        accountNumber,
                                        branchCode,
                                        accountNumber,
                                        pageNumber,
                                        requestDateFormat.format(dateTo),
                                        requestDateFormat.format(dateFrom)))
                        .post(String.class);
        return parser.parseResponse(rawResponse);
    }

    private String escapeString(String argValue) {
        return argValue.replace("|", "||");
    }
}
