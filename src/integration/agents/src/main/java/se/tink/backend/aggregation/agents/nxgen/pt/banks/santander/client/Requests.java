package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient.escapeString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants;

class Requests {

    static final String CONTROL_HEADER = "MORE-Control";
    static final String BUSINESS_DATA_REQUEST_HEADER =
            "|1process|1service|1%s|1session|1executeFlow|1method|51|1execute|1|1callId|D5";
    static final String SESSION_TOKKEN_REQUEST_HEADER =
            "|110.5.5|1version|1process|1service|12.13.24.2|1appVersion|1login|1method|D4";

    private static final String INVESTMENT_TRANSACTIONS_BODY_TEMPLATE =
            "|1q26c899603|1flow|420191023133005774|1time|12019-10-23 13:30:05|1value|1flow||q26c899603|1name|D2|12019-10-23 13:30:05|1value|1flow||i075f9a2af|1name|D2|L2|1analytics|10102745243|1kernel|D4|0|6%d|1page|1readPage|1$operation|1%s|1NUCTAFU|0|1$eninctity|1gb4aa06c34|1$caller|1j4fb2efaba|1gb4aa06c34.readPage|10003|1CBALFU|6%d|1pageSize|D8|L1|30|L4";
    private static final String TRANSACTIONS_BODY_TEMPLATE =
            "|1m6068f9ba7|1flow|420191016131838810|1time|1|1value|1flow||f6a8d54732|1name|D2|L1|1analytics|10086092607|1kernel|D4|0|1|1lastLoginDate|L0|1selectedAdvertisement|1false|1over90Days|1|1bankAccount|1|1minorUsr|1|1OTPRequested|1u2b294c3f4|1lastTabIndex|1False|1nextDayTransfer|1|1hour|1|1date|D3|L1|1operationDateResult|L0|1selectedOption|0|1advertisements|D0|1filterForm|6%d|1pageSize|1|1firstFlux|1transactions|1returnName|1true|1movments|1modalFullScreen|1u1264e9491.layout|11|1analytics|1true|1fromAccounts|1account|1typeOfPush|1|L1|L0|D0|L1|1%s|1accountNumber|1|1bankAccount|1|1accountAlias|1|1formattedAccount|155|1checkDigit|1|1bankCode|1|1branchCode|D7|L1|1|1USER_ACCOUNTS_TIMESTAMP|L0|1USER_ACCOUNTS|0|1isWorld123Client|1|1clientSegment|0|1needsAcceptance|D5|L1|L5|1loginResult|50|1has_sca|D1|L1|1sess_key_status|0|1accountsInfo|1|1_isOTP|1|1defaultAccount|1|1accountDescription|1%s|1branchCode|1|1formattedBankAccount|1|1numAuthorizedBalance|1|1closingBalance|1%s|1accountNumber|1|1bankAccount|1|1numAvailableBalance|1Available Balance|1c219ad3922|1|1number|1TOTAPTPL|1BIC|1|1SMS_MESSAGE|1|1formattedIBAN|1|1IBAN|1CASA|1accountAlias|1N|1isPrefered|1Accounting Balance:|1w4395e00f1|13|1index|1df3a81f55e|1code|21883|I|1data|1df3a81f55e|1name|D3|1b3f97ad03e|D20|L1|1selectedAccount|1k4b0f16e41|1z644851d52.action|0|1tec118464b|0|1smsValue|1Confirmation SMS|1e897e2b784|1|1m71aa29d2e|1otpRequestMessageV2|1otpRequestMessage|D5|1smsFormData|13|1selectedIndex|1readPage|1$operation|L0|1bySideToken|1related|1back.type|50|1privacyMode|D1|1settings|L0|1checkingAccounts|1|1USER_ACCOUNTS_TIMESTAMP|L0|1USER_ACCOUNTS|0|1isWorld123Client|1PB|1clientSegment|1|1lastLogin|50|1needsAcceptance|D6|L1|1sessionResult|6%d|1page|50|1privacyMode|1custom|1filterValue|1|1accountNumber|1|1bankAccount|1|1accountAlias|1|1formattedAccount|1|1checkDigit|1|1bankCode|10003|1branchCode|D7|L1|1splitAccountResult|L0|1selectedAddressee|L0|D0|L1|1False|1nextDayTransfer|1|1hour|1|1date|D3|L1|L3|1exec|4%s|1filterEndDate|1pf562adfd9|1selectedMenu|1|1formattedNIB|0|1$entity|1w2296e8694|1$caller|1|1z644851d52|131129999|1value.1|101011900|1value.0|1true|1post213Version|1operationsMenu|1operationsMenu|1|1selectedIBAN|4%s|1filterStartDate|1|1forceCall|1|1sendVariables|1|1currency|1|1closingBalance|1|1availableBalance|1|1numAuthorizedBalance|1|1authorizedBalance|1|1SMS_MESSAGE|1|1currencySimbol|1|1numAvailableBalance|1|1numClosingBalance|D9|L1|1getBalanceResult|1td0d9e00bf|1w2296e8694.readPage|D55|L1|30|L4";
    private static final String CREDIT_CARD_TRANSACTIONS_BODY_TEMPLATE =
            "|1a541e8552b|1flow|420191024144819275|1time|1|1value|1flow||a541e8552b|1name|D2|L1|1analytics|10105634223|1kernel|D4|0|6%d|1page|1m38ae520e9|1j8c6b81435.readPage|1readPage|1$operation|1|1nextDayTransfer|1|1hour|1|1date|D3|L1|1operationDateResult|0|1$entity|1j8c6b81435|1$caller|1|1BALCTIT|1|1ups|1|1startDate|1|1fullNumber|1|1ZCLIENTE|6|1doubleBalance|1|1CMARCA|1|1MONTDISP|1|1color|10110VP|1CPRODSUB|1|1name|1|1formattedAvailable|1|1CEMPRESA|1|1formattedLimit|1|1watchBckAppleImageCode|32|1index|1|1limit|1|1baseAccount|1|1branch|1%s|1number|1CR|1type|1|1product|1|1MSALDO|1|1baseAccountName|1|1account|1|1expirationDate|1|1FAMILIA|1|1doubleAvailable|1|1backgroundImageCode|1|1CSITUAC|1|1state|1|1ccPartialNumber|1|1currency|1|1availableBalance|6|1used|1|1ccPartialNumberFind|1|1productCardType|1|1accountNumber|1|1statementsImage|D39|L1|1selectedCard|6%d|1pageSize|D8|L1|30|L4";
    private static final String SESSION_TOKEN_BODY_TEMPLATE =
            "|0|0|50|1encoder.compress|1%s|1user|12.13.24.2|1appVersion|11||1||3||10|1timezone|1en|1language|1|1group|1%s|1password|1|1key|12C5AFE4B-85FC-4971-A258-2D0821648594|1deviceUUID|1f6e01ffef77fe05d908faecc23adc2b6||2019-10-17 22:13:35||D|1modelKey|1-1|1encoder.maxsize|311|1channel|110.5.5|1version|1BT|1orgunit|1SANTANDER-channel-objectivec-ios; 2.13.24.2; en_SE; Europe/Warsaw; iPhone; 12.4|1userAgent|D15|L1|30|L4";
    private static final String DEPOSIT_DETAILS_BODY_TEMPLATE =
            "|420191030121937446|1time|1e8f22d3851|1flow|10120881438|1kernel|D3|0|1|1analyticsFlow|1|1DPname|1|1accountIndex|1|1CPRODSUB|1|1CMARCA|1|1number|1|1backgroundImageCode|1%s|1branch|1|1balance|1|1CMOEDA|1|1baseDO|1|1CEMPRESA|1%s|1accountNumber|1|1baseDOname|1|1rawBalance|1|1index|51|1CAT85Validated|D16|L1|1selectedDP|D2|L1|30|L4";

    static final String CARDS_BODY =
            "|420191024110845561|1time|1e8e78d88a8|1flow|0|1kernel|D3|0|1u9ad4d0557|1analyticsFlow|0|1filter|1false|1onlyDebitAndCredit|1true|1showFictional|D4|L1|30|L4";
    static final String ACCOUNTS_BODY =
            "||1time|1n024156573|1flow|0|1kernel|D3|0|1readPage|1$operation|D1|L1|30|L4";
    static final String ASSETS_BODY =
            "|1x26bf0e990|1flow|420191022142453892|1time|L0|1analytics|0|1kernel|D4|0|D0|L1|30|L4";
    static final String IDENTITY_DATA_BODY =
            "|420191024110846066|1time|1idf7198124|1flow|10105025969|1kernel|D3|0|D0|L1|30|L4";
    static final String LOANS_BODY =
            "|1x8a27b23e4|1flow|420191028153651729|1time|1|1value|1flow||ya23a76d0e|1name|D2|L1|1analytics|0|1kernel|D4|0|D0|L1|30|L4";

    static String constructTransactionsRequestBody(
            String accountNumber,
            String branchCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            int pageNumber,
            int pageSize) {

        DateTimeFormatter requestDateFormat =
                DateTimeFormatter.ofPattern(SantanderConstants.DATE_FORMAT);

        return String.format(
                TRANSACTIONS_BODY_TEMPLATE,
                pageSize,
                accountNumber,
                branchCode,
                accountNumber,
                pageNumber,
                requestDateFormat.format(dateTo),
                requestDateFormat.format(dateFrom));
    }

    static String constructCreditCardTransactionsRequestBody(
            String fullCardNumber, int pageNumber, int pageSize) {
        return String.format(
                CREDIT_CARD_TRANSACTIONS_BODY_TEMPLATE, pageNumber, fullCardNumber, pageSize);
    }

    static String contructInvestmentTransactionsRequestBody(
            String accountNumber, int page, int pageSize) {
        return String.format(INVESTMENT_TRANSACTIONS_BODY_TEMPLATE, page, accountNumber, pageSize);
    }

    static String constructTokenRequestBody(String login, String password) {
        return String.format(
                SESSION_TOKEN_BODY_TEMPLATE, escapeString(login), escapeString(password));
    }

    static String constructDepositDetailsBody(String accountNumber, String branchCode) {
        return String.format(DEPOSIT_DETAILS_BODY_TEMPLATE, branchCode, accountNumber);
    }
}
