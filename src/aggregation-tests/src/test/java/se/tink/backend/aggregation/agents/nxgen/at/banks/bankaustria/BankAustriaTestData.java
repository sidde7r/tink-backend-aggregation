package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

public class BankAustriaTestData {

    public static final class RandomData {
        public static final String IBAN_1 = "AT023225000000704957"; // http://randomiban.com/?country=Austria
        public static final String BANK_ID_ACCOUNT_KEY_1 = "2011-11-11-11.11.11.811111";
        static final String ACCOUNT_ID_1 = "00000704957";
        static final String ACCOUNT_ID_1_FORMATED = "00000 704 957";
        static final String NATIONAL_NUMBER = "00000000010011011011";

        public static final String IBAN_2 = "AT022050303300646365"; // http://randomiban.com/?country=Austria
        static final String BANK_ID_ACCOUNT_KEY_2 = "2012-12-12-12.12.12.822222";

        public static final String NAME = "Hilda Hoffman"; // https://randomuser.me/
        
        static final String RANDOM_UUID_NOT_USED_IN_TEST = "c4168eac-84b8-46ea-b735-c9da9bfb97fd"; // https://randomuser.me/
        static final String RANDOM_COMPANYID_NOT_USED_IN_TEST = "EB01222222"; 
        static final String CONTRACTID_NOT_USED_IN_TEST = "01222222-000"; 


    }


    public static final String OTML_ERROR_LOGIN_WRONG_FORMAT = "{\"target\":\".update.authentication_login\",\"cache\":\"\",\"params\":{\"userId.error\":\"The field contains non numeric characters\",\"otml_context\":\"c1\"},\"datasources\":\"<datasources><datasource key=\\\"response\\\"><element key=\\\"action\\\" val=\\\"null\\\" /><element key=\\\"code\\\" val=\\\"none\\\" /><element key=\\\"message\\\" val=\\\"UserCode - The field contains non numeric characters\\\" /><element key=\\\"result\\\" val=\\\"ko\\\" /><element key=\\\"type\\\" val=\\\"validation\\\" /><\\/datasource><datasource key=\\\"otml_store_session\\\"><element key=\\\"account\\\" val=\\\"\\\"><\\/element><element key=\\\"accountInformationAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"activationPushTanState\\\" val=\\\"null\\\" /><element key=\\\"appReloadHome\\\" val=\\\"false\\\" /><element key=\\\"attachments\\\" val=\\\"\\\"><element key=\\\"empty\\\" val=\\\"true\\\" /><\\/element><element key=\\\"business\\\" val=\\\"false\\\" /><element key=\\\"businessNetEnabled\\\" val=\\\"true\\\" /><element key=\\\"cardPeriods\\\" val=\\\"null\\\" /><element key=\\\"cards\\\" val=\\\"null\\\" /><element key=\\\"deviceAuthenticationEnabled\\\" val=\\\"false\\\" /><element key=\\\"deviceManagementActive\\\" val=\\\"false\\\" /><element key=\\\"legacyDevice\\\" val=\\\"false\\\" /><element key=\\\"logged\\\" val=\\\"false\\\" /><element key=\\\"loginProcedureFinished\\\" val=\\\"false\\\" /><element key=\\\"loginWithFingerprintEnabled\\\" val=\\\"true\\\" /><element key=\\\"markets\\\" val=\\\"null\\\" /><element key=\\\"mobileUnchainedEnabled\\\" val=\\\"true\\\" /><element key=\\\"newAnonymFolderAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"newCreditTransferAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"newOrganAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"orderArchiveAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"orderCancellationAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"orderOverviewAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"payeeDomesticOwn\\\" val=\\\"\\\"><\\/element><element key=\\\"payeerDomesticOwn\\\" val=\\\"\\\"><\\/element><element key=\\\"postLogin\\\" val=\\\"true\\\" /><element key=\\\"qrCodeEnabled\\\" val=\\\"true\\\" /><element key=\\\"savingAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"searchTradeManagmentAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securePushVersion\\\" val=\\\"42\\\" /><element key=\\\"securitiesAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securityOrderAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securityTransactionAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securityType\\\" val=\\\"null\\\" /><element key=\\\"sessionTimeout\\\" val=\\\"null\\\" /><element key=\\\"showAccountCustomization\\\" val=\\\"false\\\" /><element key=\\\"signatureFolderAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"snapBackEnabled\\\" val=\\\"true\\\" /><element key=\\\"standingOrderAccountList\\\" val=\\\"\\\"><\\/element><element key=\\\"sweepOrderAccountList\\\" val=\\\"\\\"><\\/element><element key=\\\"unreadInbox\\\" val=\\\"null\\\" /><element key=\\\"unreadOutbox\\\" val=\\\"null\\\" /><element key=\\\"userHash\\\" val=\\\"\\\" /><element key=\\\"userPreference\\\" val=\\\"\\\"><element key=\\\"dateAndTimeFormatPattern\\\" val=\\\"dd.MM.yyyy HH:mm\\\" /><element key=\\\"dateFormatPattern\\\" val=\\\"dd.MM.yyyy\\\" /><element key=\\\"numberFormat\\\" val=\\\"de_AT\\\" /><element key=\\\"periodFormatPattern\\\" val=\\\"MM.yyyy\\\" /><element key=\\\"timeFormat\\\" val=\\\"de_AT\\\" /><element key=\\\"timeFormatPattern\\\" val=\\\"HH:mm\\\" /><element key=\\\"tomorrow\\\" val=\\\"03.07.2018\\\" /><\\/element><\\/datasource><\\/datasources>\"}";
    public static final String OTML_ERROR_LOGIN_WRONG_CREDENTIALS  = "{\"target\":\".popup\",\"cache\":\"\",\"params\":{\"content_id\":\"genericpopup\",\"otml_context\":\"c1\",\"popup_datasource\":\"generic_popup_model.pop_confirm.[]\",\"otml_stack\":\"push.generic_popup\"},\"datasources\":\"<datasources><datasource key=\\\"response\\\"><element key=\\\"action\\\" val=\\\"null\\\" /><element key=\\\"code\\\" val=\\\"none\\\" /><element key=\\\"message\\\" val=\\\"User code or PIN not correct.\\\" /><element key=\\\"result\\\" val=\\\"ko\\\" /><element key=\\\"type\\\" val=\\\"service\\\" /><\\/datasource><datasource key=\\\"otml_store_session\\\"><element key=\\\"account\\\" val=\\\"\\\"><\\/element><element key=\\\"accountInformationAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"activationPushTanState\\\" val=\\\"null\\\" /><element key=\\\"appReloadHome\\\" val=\\\"false\\\" /><element key=\\\"attachments\\\" val=\\\"\\\"><element key=\\\"empty\\\" val=\\\"true\\\" /><\\/element><element key=\\\"business\\\" val=\\\"false\\\" /><element key=\\\"businessNetEnabled\\\" val=\\\"true\\\" /><element key=\\\"cardPeriods\\\" val=\\\"null\\\" /><element key=\\\"cards\\\" val=\\\"null\\\" /><element key=\\\"deviceAuthenticationEnabled\\\" val=\\\"false\\\" /><element key=\\\"deviceManagementActive\\\" val=\\\"false\\\" /><element key=\\\"legacyDevice\\\" val=\\\"false\\\" /><element key=\\\"logged\\\" val=\\\"false\\\" /><element key=\\\"loginProcedureFinished\\\" val=\\\"false\\\" /><element key=\\\"loginWithFingerprintEnabled\\\" val=\\\"true\\\" /><element key=\\\"markets\\\" val=\\\"null\\\" /><element key=\\\"mobileUnchainedEnabled\\\" val=\\\"true\\\" /><element key=\\\"newAnonymFolderAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"newCreditTransferAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"newOrganAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"orderArchiveAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"orderCancellationAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"orderOverviewAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"payeeDomesticOwn\\\" val=\\\"\\\"><\\/element><element key=\\\"payeerDomesticOwn\\\" val=\\\"\\\"><\\/element><element key=\\\"postLogin\\\" val=\\\"true\\\" /><element key=\\\"qrCodeEnabled\\\" val=\\\"true\\\" /><element key=\\\"savingAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"searchTradeManagmentAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securePushVersion\\\" val=\\\"42\\\" /><element key=\\\"securitiesAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securityOrderAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securityTransactionAccount\\\" val=\\\"\\\"><\\/element><element key=\\\"securityType\\\" val=\\\"null\\\" /><element key=\\\"sessionTimeout\\\" val=\\\"null\\\" /><element key=\\\"showAccountCustomization\\\" val=\\\"false\\\" /><element key=\\\"signatureFolderAccounts\\\" val=\\\"\\\"><\\/element><element key=\\\"snapBackEnabled\\\" val=\\\"true\\\" /><element key=\\\"standingOrderAccountList\\\" val=\\\"\\\"><\\/element><element key=\\\"sweepOrderAccountList\\\" val=\\\"\\\"><\\/element><element key=\\\"unreadInbox\\\" val=\\\"null\\\" /><element key=\\\"unreadOutbox\\\" val=\\\"null\\\" /><element key=\\\"userHash\\\" val=\\\"\\\" /><element key=\\\"userPreference\\\" val=\\\"\\\"><element key=\\\"dateAndTimeFormatPattern\\\" val=\\\"dd.MM.yyyy HH:mm\\\" /><element key=\\\"dateFormatPattern\\\" val=\\\"dd.MM.yyyy\\\" /><element key=\\\"numberFormat\\\" val=\\\"de_AT\\\" /><element key=\\\"periodFormatPattern\\\" val=\\\"MM.yyyy\\\" /><element key=\\\"timeFormat\\\" val=\\\"de_AT\\\" /><element key=\\\"timeFormatPattern\\\" val=\\\"HH:mm\\\" /><element key=\\\"tomorrow\\\" val=\\\"28.06.2018\\\" /><\\/element><\\/datasource><\\/datasources>\"}";
    public static final String OTML_SUCCESSFUL_LOGIN  = "{\"target\": \"home\", \"cache\": \"\", \"params\": {\"otml_context\": \"c1\", \"otml_stack\": \"mark.welcome;push.otml_piggybacking;mark.logged\"}, \"xml\": \"<map name=\\\"home\\\" version=\\\"1\\\"><elements><element key=\\\"home\\\" ><entry canGoBack=\\\"false\\\" android_canGoBack=\\\"true\\\" wp_canGoBack=\\\"true\\\" otml_in_animation=\\\"true\\\" ipad_menu_type=\\\"light\\\" floating_buttons_active=\\\"true\\\"><script><![CDATA[ function reloadHome(params) { var reload = params.appReloadHome; if (reload == 'true') { actionCallbackWithParams(newAction(\\\"[baseaddr]/home.htm\\\",\", \"datasources\": \"<datasources>\"}";

    // Datasource part of response from BankAustriaConstants.Urls.SETTINGS
    public static final String SETTINGS_DATA_SOURCES = "<datasources>" +
            "<datasource key=\"response\">" +
            "    <element key=\"action\" val=\"null\"/>" +
            "    <element key=\"code\" val=\"none\"/>" +
            "    <element key=\"customizedAccountMetaModelsList\" val=\"\">" +
            "        <element key=\"\" val=\"\">" +
            "            <element key=\"accountCurrency\" val=\"EUR\"/>" +
            "            <element key=\"accountKey\" val=\""+ RandomData.BANK_ID_ACCOUNT_KEY_1 +"\"/>" +
            "            <element key=\"accountNickname\" val=\"\"/>" +
            "            <element key=\"accountNumber\" val=\"" + RandomData.IBAN_1 +"\"/>" +
            "            <element key=\"accountType\" val=\"CURRENT\"/>" +
            "            <element key=\"favorite\" val=\"true\"/>" +
            "            <element key=\"frontEndId\" val=\"" + RandomData.RANDOM_UUID_NOT_USED_IN_TEST + "\"/>" +
            "        </element>" +
            "    </element>" +
            "    <element key=\"message\" val=\"null\"/>" +
            "    <element key=\"omtlPlat\" val=\"ios\"/>" +
            "    <element key=\"result\" val=\"ok\"/>" +
            "    <element key=\"type\" val=\"none\"/>" +
            "</datasource>" +
            "</datasources>";

    // Datasource part of response from BankAustriaConstants.Urls.SETTINGS modified to two accounts
    public static final String SETTINGS_ASSUMED_DATA_SOURCES = "<datasources>" +
            "<datasource key=\"response\">" +
            "    <element key=\"action\" val=\"null\"/>" +
            "    <element key=\"code\" val=\"none\"/>" +
            "    <element key=\"customizedAccountMetaModelsList\" val=\"\">" +
            "        <element key=\"\" val=\"\">" +
            "            <element key=\"accountCurrency\" val=\"EUR\"/>" +
            "            <element key=\"accountKey\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "            <element key=\"accountNickname\" val=\"\"/>" +
            "            <element key=\"accountNumber\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "            <element key=\"accountType\" val=\"CURRENT\"/>" +
            "            <element key=\"favorite\" val=\"true\"/>" +
            "            <element key=\"frontEndId\" val=\"" + RandomData.RANDOM_UUID_NOT_USED_IN_TEST +"\"/>" +
            "        </element>" +
            "        <element key=\"\" val=\"\">" +
            "            <element key=\"accountCurrency\" val=\"SEK\"/>" +
            "            <element key=\"accountKey\" val=\""+ RandomData.BANK_ID_ACCOUNT_KEY_2 + "\"/>" +
            "            <element key=\"accountNickname\" val=\"\"/>" +
            "            <element key=\"accountNumber\" val=\"" + RandomData.IBAN_2 + "\"/>" +
            "            <element key=\"accountType\" val=\"CURRENT\"/>" +
            "            <element key=\"favorite\" val=\"true\"/>" +
            "            <element key=\"frontEndId\" val=\"" + RandomData.RANDOM_UUID_NOT_USED_IN_TEST + "\"/>" +
            "        </element>" +
            "    </element>" +
            "    <element key=\"message\" val=\"null\"/>" +
            "    <element key=\"omtlPlat\" val=\"ios\"/>" +
            "    <element key=\"result\" val=\"ok\"/>" +
            "    <element key=\"type\" val=\"none\"/>" +
            "</datasource>" +
            "</datasources>";

    // Datasource part of response from BankAustriaConstants.Urls.MOVEMENTS
    public static final String BALANCE_MOVEMENTS_FOR_ACCOUNT = "<datasources>" +
            "<datasource key=\"response\">" +
            "    <element key=\"account\" val=\"\">" +
            "        <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "        <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "        <element key=\"alias\" val=\"null\"/>" +
            "        <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "        <element key=\"balance\" val=\"\"/>" +
            "        <element key=\"bank\" val=\"\">" +
            "            <element key=\"bankCode\" val=\"0000012000\"/>" +
            "            <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "            <element key=\"id\" val=\"12000\"/>" +
            "            <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "        </element>" +
            "        <element key=\"branchEurosigId\" val=\"null\"/>" +
            "        <element key=\"cin\" val=\"null\"/>" +
            "        <element key=\"companies\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                <element key=\"sia\" val=\"00000\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "        <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "        <element key=\"controvalore\" val=\"\">" +
            "            <element key=\"amount\" val=\"null\"/>" +
            "            <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "            <element key=\"currency\" val=\"EUR\"/>" +
            "            <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "            <element key=\"decimal\" val=\"00\"/>" +
            "            <element key=\"integer\" val=\"null\"/>" +
            "            <element key=\"integerFormatted\" val=\"\"/>" +
            "            <element key=\"value\" val=\"0.0\"/>" +
            "            <element key=\"valueFormatted\" val=\"null\"/>" +
            "            <element key=\"valueString\" val=\"0.00\"/>" +
            "        </element>" +
            "        <element key=\"currency\" val=\"EUR\"/>" +
            "        <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "        <element key=\"description\" val=\"" + RandomData.NAME + "\"/>" +
            "        <element key=\"frontEndId\" val=\"" + RandomData.RANDOM_UUID_NOT_USED_IN_TEST + "\"/>" +
            "        <element key=\"handler\" val=\"W3VT\"/>" +
            "        <element key=\"iban\" val=\""+ RandomData.IBAN_1+ "\"/>" +
            "        <element key=\"id\" val=\""+ RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "        <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "        <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "        <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "        <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "        <element key=\"packageKey\" val=\"0\"/>" +
            "        <element key=\"pan\" val=\"null\"/>" +
            "        <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "        <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "        <element key=\"relationType\" val=\"null\"/>" +
            "        <element key=\"shortKey\" val=\"1\"/>" +
            "        <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "        <element key=\"swiftCode\" val=\"null\"/>" +
            "        <element key=\"tag25\" val=\"null\"/>" +
            "        <element key=\"type\" val=\"C1\"/>" +
            "    </element>" +
            "    <element key=\"action\" val=\"null\"/>" +
            "    <element key=\"balance\" val=\"\">" +
            "        <element key=\"accountable\" val=\"\">" +
            "            <element key=\"amount\" val=\"null\"/>" +
            "            <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "            <element key=\"currency\" val=\"EUR\"/>" +
            "            <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "            <element key=\"decimal\" val=\"48\"/>" +
            "            <element key=\"integer\" val=\"12\"/>" +
            "            <element key=\"integerFormatted\" val=\"12\"/>" +
            "            <element key=\"value\" val=\"12.48\"/>" +
            "            <element key=\"valueFormatted\" val=\"null\"/>" +
            "            <element key=\"valueString\" val=\"12.48\"/>" +
            "        </element>" +
            "        <element key=\"available\" val=\"\">" +
            "            <element key=\"amount\" val=\"null\"/>" +
            "            <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "            <element key=\"currency\" val=\"EUR\"/>" +
            "            <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "            <element key=\"decimal\" val=\"48\"/>" +
            "            <element key=\"integer\" val=\"12\"/>" +
            "            <element key=\"integerFormatted\" val=\"12\"/>" +
            "            <element key=\"value\" val=\"12.48\"/>" +
            "            <element key=\"valueFormatted\" val=\"null\"/>" +
            "            <element key=\"valueString\" val=\"12.48\"/>" +
            "        </element>" +
            "        <element key=\"drawing\" val=\"\">" +
            "            <element key=\"amount\" val=\"null\"/>" +
            "            <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "            <element key=\"currency\" val=\"EUR\"/>" +
            "            <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "            <element key=\"decimal\" val=\"48\"/>" +
            "            <element key=\"integer\" val=\"12\"/>" +
            "            <element key=\"integerFormatted\" val=\"12\"/>" +
            "            <element key=\"value\" val=\"12.48\"/>" +
            "            <element key=\"valueFormatted\" val=\"null\"/>" +
            "            <element key=\"valueString\" val=\"12.48\"/>" +
            "        </element>" +
            "    </element>" +
            "    <element key=\"code\" val=\"none\"/>" +
            "    <element key=\"currency\" val=\" &#8364;\"/>" +
            "    <element key=\"currentMonth\" val=\"false\"/>" +
            "    <element key=\"endDate\" val=\"\">" +
            "        <element key=\"ISO8601String\" val=\"2018-07-03T10:12:36+0200\"/>" +
            "        <element key=\"date\" val=\"2018-07-03T10:12:36+0200\"/>" +
            "        <element key=\"day\" val=\"03\"/>" +
            "        <element key=\"month\" val=\"07\"/>" +
            "        <element key=\"rawDate\" val=\"2018-07-03T10:12:36+0200\"/>" +
            "        <element key=\"year\" val=\"2018\"/>" +
            "    </element>" +
            "    <element key=\"fromWp\" val=\"false\"/>" +
            "    <element key=\"message\" val=\"null\"/>" +
            "    <element key=\"minimumDate\" val=\"\">" +
            "        <element key=\"ISO8601String\" val=\"2016-07-03T10:12:36+0200\"/>" +
            "        <element key=\"date\" val=\"2016-07-03T10:12:36+0200\"/>" +
            "        <element key=\"day\" val=\"03\"/>" +
            "        <element key=\"month\" val=\"07\"/>" +
            "        <element key=\"rawDate\" val=\"2018-07-03T10:12:36+0200\"/>" +
            "        <element key=\"year\" val=\"2016\"/>" +
            "    </element>" +
            "    <element key=\"misure\" val=\"\">" +
            "        <element key=\"\" val=\"0\"/>" +
            "        <element key=\"\" val=\"100\"/>" +
            "    </element>" +
            "    <element key=\"monthlyBalance\" val=\"-0,75 &#8364;\"/>" +
            "    <element key=\"monthlyBalanceLabel\" val=\"03.06.2018 - 03.07.2018\"/>" +
            "    <element key=\"monthlyBalanceValue\" val=\"-0.7500000000000001\"/>" +
            "    <element key=\"monthlyIncoming\" val=\"+0,00 &#8364;\"/>" +
            "    <element key=\"monthlyIncomingValue\" val=\"0.0\"/>" +
            "    <element key=\"monthlyOutcoming\" val=\"-0,75 &#8364;\"/>" +
            "    <element key=\"monthlyOutcomingValue\" val=\"-0.7500000000000001\"/>" +
            "    <element key=\"movements\" val=\"\">" +
            "        <element key=\"\" val=\"\">" +
            "            <element key=\"account\" val=\"null\"/>" +
            "            <element key=\"accountId\" val=\"\"/>" +
            "            <element key=\"accountLabel\" val=\"null\"/>" +
            "            <element key=\"amount\" val=\"\">" +
            "                <element key=\"amount\" val=\"null\"/>" +
            "                <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                <element key=\"decimal\" val=\"68\"/>" +
            "                <element key=\"integer\" val=\"-0\"/>" +
            "                <element key=\"integerFormatted\" val=\" 0\"/>" +
            "                <element key=\"value\" val=\"-0.68\"/>" +
            "                <element key=\"valueFormatted\" val=\"null\"/>" +
            "                <element key=\"valueString\" val=\"-0.68\"/>" +
            "            </element>" +
            "            <element key=\"bookingText\" val=\"PORTO\"/>" +
            "            <element key=\"formattedPaymentCode\" val=\"306\"/>" +
            "            <element key=\"id\" val=\"0\"/>" +
            "            <element key=\"internalNote\" val=\"null\"/>" +
            "            <element key=\"paymentCodeDescription\"" +
            "                     val=\"??? [306.50]&#60;--[bundles.geb.products.pages.online.CheckingHistoryPage] locale[en]\"/>" +
            "            <element key=\"recordData\" val=\"null\"/>" +
            "            <element key=\"recordDataKey\" val=\"0\"/>" +
            "            <element key=\"recordDataLineMaxLength\" val=\"null\"/>" +
            "            <element key=\"recordDataLines\" val=\"null\"/>" +
            "            <element key=\"recordDataRow\" val=\"null\"/>" +
            "            <element key=\"transactionDate\" val=\"\">" +
            "                <element key=\"ISO8601String\" val=\"2018-07-02T10:12:36+0200\"/>" +
            "                <element key=\"date\" val=\"2018-07-02T10:12:36+0200\"/>" +
            "                <element key=\"day\" val=\"02\"/>" +
            "                <element key=\"month\" val=\"07\"/>" +
            "                <element key=\"rawDate\" val=\"null\"/>" +
            "                <element key=\"year\" val=\"2018\"/>" +
            "            </element>" +
            "            <element key=\"type\" val=\"\"/>" +
            "            <element key=\"valueDate\" val=\"\">" +
            "                <element key=\"ISO8601String\" val=\"2018-06-30T10:12:36+0200\"/>" +
            "                <element key=\"date\" val=\"2018-06-30T10:12:36+0200\"/>" +
            "                <element key=\"day\" val=\"30\"/>" +
            "                <element key=\"month\" val=\"06\"/>" +
            "                <element key=\"rawDate\" val=\"null\"/>" +
            "                <element key=\"year\" val=\"2018\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"\" val=\"\">" +
            "            <element key=\"account\" val=\"null\"/>" +
            "            <element key=\"accountId\" val=\"\"/>" +
            "            <element key=\"accountLabel\" val=\"null\"/>" +
            "            <element key=\"amount\" val=\"\">" +
            "                <element key=\"amount\" val=\"null\"/>" +
            "                <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                <element key=\"decimal\" val=\"02\"/>" +
            "                <element key=\"integer\" val=\"-0\"/>" +
            "                <element key=\"integerFormatted\" val=\" 0\"/>" +
            "                <element key=\"value\" val=\"-0.02\"/>" +
            "                <element key=\"valueFormatted\" val=\"null\"/>" +
            "                <element key=\"valueString\" val=\"-0.02\"/>" +
            "            </element>" +
            "            <element key=\"bookingText\" val=\"&#220;BERZIEHUNGSPROVISION\"/>" +
            "            <element key=\"formattedPaymentCode\" val=\"306\"/>" +
            "            <element key=\"id\" val=\"1\"/>" +
            "            <element key=\"internalNote\" val=\"null\"/>" +
            "            <element key=\"paymentCodeDescription\"" +
            "                     val=\"??? [306.50]&#60;--[bundles.geb.products.pages.online.CheckingHistoryPage] locale[en]\"/>" +
            "            <element key=\"recordData\" val=\"null\"/>" +
            "            <element key=\"recordDataKey\" val=\"0\"/>" +
            "            <element key=\"recordDataLineMaxLength\" val=\"null\"/>" +
            "            <element key=\"recordDataLines\" val=\"null\"/>" +
            "            <element key=\"recordDataRow\" val=\"null\"/>" +
            "            <element key=\"transactionDate\" val=\"\">" +
            "                <element key=\"ISO8601String\" val=\"2018-07-02T10:12:36+0200\"/>" +
            "                <element key=\"date\" val=\"2018-07-02T10:12:36+0200\"/>" +
            "                <element key=\"day\" val=\"02\"/>" +
            "                <element key=\"month\" val=\"07\"/>" +
            "                <element key=\"rawDate\" val=\"null\"/>" +
            "                <element key=\"year\" val=\"2018\"/>" +
            "            </element>" +
            "            <element key=\"type\" val=\"\"/>" +
            "            <element key=\"valueDate\" val=\"\">" +
            "                <element key=\"ISO8601String\" val=\"2018-06-30T10:12:36+0200\"/>" +
            "                <element key=\"date\" val=\"2018-06-30T10:12:36+0200\"/>" +
            "                <element key=\"day\" val=\"30\"/>" +
            "                <element key=\"month\" val=\"06\"/>" +
            "                <element key=\"rawDate\" val=\"null\"/>" +
            "                <element key=\"year\" val=\"2018\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"\" val=\"\">" +
            "            <element key=\"account\" val=\"null\"/>" +
            "            <element key=\"accountId\" val=\"\"/>" +
            "            <element key=\"accountLabel\" val=\"null\"/>" +
            "            <element key=\"amount\" val=\"\">" +
            "                <element key=\"amount\" val=\"null\"/>" +
            "                <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                <element key=\"decimal\" val=\"05\"/>" +
            "                <element key=\"integer\" val=\"-0\"/>" +
            "                <element key=\"integerFormatted\" val=\" 0\"/>" +
            "                <element key=\"value\" val=\"-0.05\"/>" +
            "                <element key=\"valueFormatted\" val=\"null\"/>" +
            "                <element key=\"valueString\" val=\"-0.05\"/>" +
            "            </element>" +
            "            <element key=\"bookingText\" val=\"SOLLZINSEN\"/>" +
            "            <element key=\"formattedPaymentCode\" val=\"542\"/>" +
            "            <element key=\"id\" val=\"2\"/>" +
            "            <element key=\"internalNote\" val=\"null\"/>" +
            "            <element key=\"paymentCodeDescription\"" +
            "                     val=\"??? [542.48]&#60;--[bundles.geb.products.pages.online.CheckingHistoryPage] locale[en]\"/>" +
            "            <element key=\"recordData\" val=\"null\"/>" +
            "            <element key=\"recordDataKey\" val=\"0\"/>" +
            "            <element key=\"recordDataLineMaxLength\" val=\"null\"/>" +
            "            <element key=\"recordDataLines\" val=\"null\"/>" +
            "            <element key=\"recordDataRow\" val=\"null\"/>" +
            "            <element key=\"transactionDate\" val=\"\">" +
            "                <element key=\"ISO8601String\" val=\"2018-07-02T10:12:36+0200\"/>" +
            "                <element key=\"date\" val=\"2018-07-02T10:12:36+0200\"/>" +
            "                <element key=\"day\" val=\"02\"/>" +
            "                <element key=\"month\" val=\"07\"/>" +
            "                <element key=\"rawDate\" val=\"null\"/>" +
            "                <element key=\"year\" val=\"2018\"/>" +
            "            </element>" +
            "            <element key=\"type\" val=\"\"/>" +
            "            <element key=\"valueDate\" val=\"\">" +
            "                <element key=\"ISO8601String\" val=\"2018-06-30T10:12:36+0200\"/>" +
            "                <element key=\"date\" val=\"2018-06-30T10:12:36+0200\"/>" +
            "                <element key=\"day\" val=\"30\"/>" +
            "                <element key=\"month\" val=\"06\"/>" +
            "                <element key=\"rawDate\" val=\"null\"/>" +
            "                <element key=\"year\" val=\"2018\"/>" +
            "            </element>" +
            "        </element>" +
            "    </element>" +
            "    <element key=\"movementsSize\" val=\"null\"/>" +
            "    <element key=\"percIncoming\" val=\"0\"/>" +
            "    <element key=\"percOutcoming\" val=\"100\"/>" +
            "    <element key=\"result\" val=\"ok\"/>" +
            "    <element key=\"showCuttedDateMessage\" val=\"false\"/>" +
            "    <element key=\"startDate\" val=\"\">" +
            "        <element key=\"ISO8601String\" val=\"2018-06-03T10:12:36+0200\"/>" +
            "        <element key=\"date\" val=\"2018-06-03T10:12:36+0200\"/>" +
            "        <element key=\"day\" val=\"03\"/>" +
            "        <element key=\"month\" val=\"06\"/>" +
            "        <element key=\"rawDate\" val=\"2018-06-03T10:12:36+0200\"/>" +
            "        <element key=\"year\" val=\"2018\"/>" +
            "    </element>" +
            "    <element key=\"type\" val=\"none\"/>" +
            "</datasource>" +
            "</datasources>";

    // Datasource part of response from BankAustriaConstants.Urls.LOGIN
    public static final String FIRST_AFTER_SIGN_IN = "<datasources>" +
            "    <datasource key=\"response\">" +
            "        <element key=\"qrPaymentData\" val=\"null\"/>" +
            "    </datasource>" +
            "    <datasource key=\"otml_store_session\">" +
            "        <element key=\"account\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"7ca359c5-b64e-4a74-b6d8-c37088603a5d\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"accountInformationAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"null\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"2a5477e9-30b6-4b03-9350-40b84a55d612\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"activationPushTanState\" val=\"null\"/>" +
            "        <element key=\"appReloadHome\" val=\"false\"/>" +
            "        <element key=\"attachments\" val=\"\">" +
            "            <element key=\"empty\" val=\"true\"/>" +
            "        </element>" +
            "        <element key=\"business\" val=\"false\"/>" +
            "        <element key=\"businessNetEnabled\" val=\"true\"/>" +
            "        <element key=\"cardPeriods\" val=\"null\"/>" +
            "        <element key=\"cards\" val=\"null\"/>" +
            "        <element key=\"deviceAuthenticationEnabled\" val=\"false\"/>" +
            "        <element key=\"deviceManagementActive\" val=\"true\"/>" +
            "        <element key=\"legacyDevice\" val=\"false\"/>" +
            "        <element key=\"logged\" val=\"true\"/>" +
            "        <element key=\"loginProcedureFinished\" val=\"true\"/>" +
            "        <element key=\"loginWithFingerprintEnabled\" val=\"true\"/>" +
            "        <element key=\"markets\" val=\"null\"/>" +
            "        <element key=\"mobileUnchainedEnabled\" val=\"true\"/>" +
            "        <element key=\"newAnonymFolderAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"672c1782-ec23-457c-b668-7eca15b7f8a6\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"newCreditTransferAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"9994682f-9ae8-48f4-b045-e6f50a53e2f3\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"newOrganAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"54931dac-8ebe-42c8-bc28-738591436368\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"orderArchiveAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"ee21a781-1105-4613-84b1-44d351ee306d\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"orderCancellationAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"ee21a781-1105-4613-84b1-44d351ee306d\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"orderOverviewAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"931f712e-9628-49c6-8934-fbf4850c6deb\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"payeeDomesticOwn\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"db8ee602-761b-4088-8c31-9ee12671202a\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"payeerDomesticOwn\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"0101d9b8-9034-40bd-80f4-bff2abd40615\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"postLogin\" val=\"true\"/>" +
            "        <element key=\"qrCodeEnabled\" val=\"true\"/>" +
            "        <element key=\"savingAccount\" val=\"null\"/>" +
            "        <element key=\"searchTradeManagmentAccount\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"8bb4ca54-3f19-4a40-bfff-bc4d627200b1\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"securePushVersion\" val=\"42\"/>" +
            "        <element key=\"securitiesAccount\" val=\"\"></element>" +
            "        <element key=\"securityOrderAccount\" val=\"\"></element>" +
            "        <element key=\"securityTransactionAccount\" val=\"\"></element>" +
            "        <element key=\"securityType\" val=\"mtan\"/>" +
            "        <element key=\"sessionTimeout\" val=\"1800\"/>" +
            "        <element key=\"showAccountCustomization\" val=\"true\"/>" +
            "        <element key=\"signatureFolderAccounts\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"cab795f8-e313-49be-8269-03d122b3fcd7\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"snapBackEnabled\" val=\"true\"/>" +
            "        <element key=\"standingOrderAccountList\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"1785ab99-ec48-44f1-8d4b-a085fdd91fcd\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"sweepOrderAccountList\" val=\"\">" +
            "            <element key=\"\" val=\"\">" +
            "                <element key=\"accountNumber\" val=\"" + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"accountNumberFormatted\" val=\"" + RandomData.ACCOUNT_ID_1_FORMATED + "\"/>" +
            "                <element key=\"alias\" val=\"null\"/>" +
            "                <element key=\"anagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"balance\" val=\"\"/>" +
            "                <element key=\"bank\" val=\"\">" +
            "                    <element key=\"bankCode\" val=\"0000012000\"/>" +
            "                    <element key=\"description\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                    <element key=\"id\" val=\"12000\"/>" +
            "                    <element key=\"name\" val=\"UNICREDIT BANK AUSTRIA AG\"/>" +
            "                </element>" +
            "                <element key=\"branchEurosigId\" val=\"null\"/>" +
            "                <element key=\"cin\" val=\"null\"/>" +
            "                <element key=\"companies\" val=\"\">" +
            "                    <element key=\"\" val=\"\">" +
            "                        <element key=\"fiscalIdentifier\" val=\"\"/>" +
            "                        <element key=\"id\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                        <element key=\"name\" val=\"" + RandomData.NAME + "\"/>" +
            "                        <element key=\"sia\" val=\"00000\"/>" +
            "                    </element>" +
            "                </element>" +
            "                <element key=\"companyid\" val=\"" + RandomData.RANDOM_COMPANYID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"contractId\" val=\"" + RandomData.CONTRACTID_NOT_USED_IN_TEST + "\"/>" +
            "                <element key=\"controvalore\" val=\"\">" +
            "                    <element key=\"amount\" val=\"null\"/>" +
            "                    <element key=\"amountWithCurrency\" val=\"nullEUR\"/>" +
            "                    <element key=\"currency\" val=\"EUR\"/>" +
            "                    <element key=\"currencyLabel\" val=\"&#8364;\"/>" +
            "                    <element key=\"decimal\" val=\"00\"/>" +
            "                    <element key=\"integer\" val=\"null\"/>" +
            "                    <element key=\"integerFormatted\" val=\"\"/>" +
            "                    <element key=\"value\" val=\"0.0\"/>" +
            "                    <element key=\"valueFormatted\" val=\"null\"/>" +
            "                    <element key=\"valueString\" val=\"0.00\"/>" +
            "                </element>" +
            "                <element key=\"currency\" val=\"EUR\"/>" +
            "                <element key=\"dataBaseCode\" val=\"BA\"/>" +
            "                <element key=\"description\" val=\"Merlin Mahmud\"/>" +
            "                <element key=\"frontEndId\" val=\"a2a40b57-a939-4904-b177-b78c78bd6d4d\"/>" +
            "                <element key=\"handler\" val=\"W3VT\"/>" +
            "                <element key=\"iban\" val=\"" + RandomData.IBAN_1 + "\"/>" +
            "                <element key=\"id\" val=\"" + RandomData.BANK_ID_ACCOUNT_KEY_1 + "\"/>" +
            "                <element key=\"label\" val=\"AT19 12000 " + RandomData.ACCOUNT_ID_1 + "\"/>" +
            "                <element key=\"nationalBankCode\" val=\"0000012000\"/>" +
            "                <element key=\"nationalBranchCode\" val=\"0000000905\"/>" +
            "                <element key=\"nationalNumber\" val=\"" + RandomData.NATIONAL_NUMBER + "\"/>" +
            "                <element key=\"packageKey\" val=\"0\"/>" +
            "                <element key=\"pan\" val=\"null\"/>" +
            "                <element key=\"relatedAnagraphicCode\" val=\"0000000064759489\"/>" +
            "                <element key=\"relatedAnagraphicType\" val=\"PF\"/>" +
            "                <element key=\"relationType\" val=\"null\"/>" +
            "                <element key=\"shortKey\" val=\"1\"/>" +
            "                <element key=\"simpleAccountBalance\" val=\"null\"/>" +
            "                <element key=\"swiftCode\" val=\"null\"/>" +
            "                <element key=\"tag25\" val=\"null\"/>" +
            "                <element key=\"type\" val=\"C1\"/>" +
            "            </element>" +
            "        </element>" +
            "        <element key=\"unreadInbox\" val=\"0\"/>" +
            "        <element key=\"unreadOutbox\" val=\"null\"/>" +
            "        <element key=\"userHash\" val=\"7C3AE517A30646B85FD2696CB62118B6\"/>" +
            "        <element key=\"userPreference\" val=\"\">" +
            "            <element key=\"dateAndTimeFormatPattern\" val=\"dd.MM.yyyy HH:mm\"/>" +
            "            <element key=\"dateFormatPattern\" val=\"dd.MM.yyyy\"/>" +
            "            <element key=\"numberFormat\" val=\"de_AT\"/>" +
            "            <element key=\"periodFormatPattern\" val=\"MM.yyyy\"/>" +
            "            <element key=\"timeFormat\" val=\"de_AT\"/>" +
            "            <element key=\"timeFormatPattern\" val=\"HH:mm\"/>" +
            "            <element key=\"tomorrow\" val=\"06.07.2018\"/>" +
            "        </element>" +
            "    </datasource>" +
            "</datasources>";

    // Datasource part of response from Get Settings when a rtaMessage is displayed
    public static final String RTA_MESSAGE = "<datasources>\n"
            + "    <datasource key=\"response\">\n"
            + "        <element key=\"action\" val=\"null\"/>\n"
            + "        <element key=\"code\" val=\"none\"/>\n"
            + "        <element key=\"message\" val=\"null\"/>\n"
            + "        <element key=\"os\" val=\"ios\"/>\n"
            + "        <element key=\"result\" val=\"ok\"/>\n"
            + "        <element key=\"rows\" val=\"0\"/>\n"
            + "        <element key=\"rtaMessage\" val=\"\">\n"
            + "            <element key=\"advTypeSender\" val=\"                              \"/>\n"
            + "            <element key=\"alreadyRead\" val=\"false\"/>\n"
            + "            <element key=\"attachments\" val=\"\"></element>\n"
            + "            <element key=\"customerRejected\" val=\"null\"/>\n"
            + "            <element key=\"dateExpiration\" val=\"null\"/>\n"
            + "            <element key=\"dateInsert\" val=\"2018-09-19T10:06:38+0200\"/>\n"
            + "            <element key=\"dcaSender\" val=\"              \"/>\n"
            + "            <element key=\"htmlMessage\" val=\"false\"/>\n"
            + "            <element key=\"id\" val=\"5010536\"/>\n"
            + "            <element key=\"idTopic\" val=\"0\"/>\n"
            + "            <element key=\"links\" val=\"\"></element>\n"
            + "            <element key=\"mandatory\" val=\"true\"/>\n"
            + "            <element key=\"nameReceiver\" val=\"\"/>\n"
            + "            <element key=\"nameSender\" val=\"Bank Austria\"/>\n"
            + "            <element key=\"ndgSender\" val=\"                  \"/>\n"
            + "            <element key=\"originalMessage\" val=\"\">\n"
            + "                <element key=\"answAvailable\" val=\"false\"/>\n"
            + "                <element key=\"attach\" val=\"null\"/>\n"
            + "                <element key=\"customerRejected\" val=\"null\"/>\n"
            + "                <element key=\"dateExpire\" val=\"null\"/>\n"
            + "                <element key=\"dateInsert\" val=\"2018-09-19 10:06:38.605\"/>\n"
            + "                <element key=\"flagAttach\" val=\"N\"/>\n"
            + "                <element key=\"flagRejectable\" val=\"false\"/>\n"
            + "                <element key=\"idCategory\" val=\"0\"/>\n"
            + "                <element key=\"idMessage\" val=\"5010536\"/>\n"
            + "                <element key=\"idMsgType\" val=\"RTA\"/>\n"
            + "                <element key=\"idRtaCategory\" val=\"0\"/>\n"
            + "                <element key=\"idStatus\" val=\"AC\"/>\n"
            + "                <element key=\"idTopic\" val=\"0\"/>\n"
            + "                <element key=\"messageTextContainsHTML\" val=\"false\"/>\n"
            + "                <element key=\"read\" val=\"false\"/>\n"
            + "                <element key=\"receiver\" val=\"null\"/>\n"
            + "                <element key=\"rejectMessage\" val=\"null\"/>\n"
            + "                <element key=\"rtaAcceptance\" val=\"null\"/>\n"
            + "                <element key=\"rtaAlreadyRead\" val=\"false\"/>\n"
            + "                <element key=\"rtaMandatory\" val=\"true\"/>\n"
            + "                <element key=\"sender\" val=\"\">\n"
            + "                    <element key=\"advIdSender\" val=\"RTA0_N304552    \"/>\n"
            + "                    <element key=\"advTypeSender\" val=\"                              \"/>\n"
            + "                    <element key=\"branchSender\" val=\"null\"/>\n"
            + "                    <element key=\"dcaSender\" val=\"              \"/>\n"
            + "                    <element key=\"nameSender\" val=\"Bank Austria\"/>\n"
            + "                    <element key=\"ndgSender\" val=\"                  \"/>\n"
            + "                </element>\n"
            + "                <element key=\"signInfo\" val=\"null\"/>\n"
            + "                <element key=\"subject\" val=\"RmFsc2NoZSBTaWNoZXJoZWl0cy1BcHAgdW5kIEJldHJ1Z3NhbnJ1ZmU=\"/>\n"
            + "                <element key=\"subjectEn\" val=\"RmFrZSBzZWN1cml0eSBhcHBzIGFuZCBmcmF1ZHVsZW50IHRlbGVwaG9uZSBjYWxscw==\"/>\n"
            + "                <element key=\"text\"\n"
            + "                         val=\"PHA+PGZvbnQgY29sb3I9IiNGRjAwMDAiPjxiPkZhbHNjaGUgU2ljaGVyaGVpdHMtQXBwITwvYj48L2ZvbnQ+IERlcnplaXQgc2luZCBQaGlzaGluZy1NYWlscyBpbSBVbWxhdWYsIHdlbGNoZSBTaWUgdW50ZXIgZGVtIFZvcndhbmQgZGVyIGVyZm9yZGVybGljaGVuIEluc3RhbGxhdGlvbiBlaW5lciAiU2ljaGVyaGVpdHMtQXBwIiBhdWYgZWluZSBnZWbDpGxzY2h0ZSBTZWl0ZSBtaXQgZWluZW0gQmFua2luZy1Mb2dpbiBmw7xocmVuLiBOYWNoIGRlbSBMb2dpbiBlcmhhbHRlbiBTaWUgZWluZSBJbnN0YWxsYXRpb25zYW5sZWl0dW5nIGbDvHIgZGllc2UgQXBwLCBkaWUgaW4gV2lya2xpY2hrZWl0IGVpbiBUcm9qYW5lciBpc3QsIGF1ZiBJaHJlbSBBbmRyb2lkIFNtYXJ0cGhvbmUuIEJpdHRlIHdlbmRlbiBTaWUgc2ljaCBhdWNoIGluIGRpZXNlbSBGYWxsIHVtZ2VoZW5kIGFuIHVuc2VyZSBPbmxpbmVCYW5raW5nIEhvdGxpbmUgdW50ZXIgMDUwNTA1LTI2MTAwLjxwPg0KPHA+PGZvbnQgY29sb3I9IiNGRjAwMDAiPjxiPkFjaHR1bmcgQmV0cnVnc2FucnVmZSE8L2I+PC9mb250PiBEYSBlcyBlcm5ldXQgQW5ydWZlIHZvbiBmYWxzY2hlbiBCYW5rIEF1c3RyaWEgTWl0YXJiZWl0ZXJpbm5lbiB1bmQgTWl0YXJiZWl0ZXJuIGdpYnQgd2FybmVuIHdpciBTaWUgZWluZHJpbmdsaWNoIHZvciBkaWVzZXIgQmV0cnVnc21ldGhvZGUuIEJpdHRlIGdlYmVuIFNpZSBrZWluZSBUQU4gb2RlciBadWdhbmdzZGF0ZW4gYW0gVGVsZWZvbiB3ZWl0ZXIsIGF1Y2ggbmljaHQgYW4gdmVybWVpbnRsaWNoZSBCYW5rIEF1c3RyaWEgTWl0YXJiZWl0ZXJpbm5lbiBvZGVyIE1pdGFyYmVpdGVyISBEaWUgQmV0csO8Z2VyIHNpbmQgc2VociBlcmZpbmRlcmlzY2ggdW5kIG5lbm5lbiBkaWUgVGF0c2FjaGVuIG5hdMO8cmxpY2ggbmljaHQgYmVpbSBOYW1lbi4gQml0dGUgZ2xlaWNoZW4gU2llIGF1Y2gga2VpbmUgIkNvZGVzIiBhYiBvZGVyIG1hY2hlbiBiZWkgIlNpY2hlcmhlaXRzw7xiZXJwcsO8ZnVuZ2VuIiBvZGVyIMOkaG5saWNoZW0gbWl0LiBMYXNzZW4gU2llIHNpY2ggbmljaHQgdW50ZXIgRHJ1Y2sgc2V0emVuIHVuZCB3ZW5kZW4gU2llIHNpY2ggZ2dmLiBhbiB1bnNlcmUgT25saW5lQmFua2luZyBIb3RsaW5lIHVudGVyIDA1MDUwNS0yNjEwMC48cD4=\"/>\n"
            + "                <element key=\"textEn\"\n"
            + "                         val=\"PHA+PGZvbnQgY29sb3I9IiNGRjAwMDAiPjxiPkZha2UgU2VjdXJpdHkgYXBwISA8L2I+PC9mb250PiBQaGlzaGluZyBlLW1haWxzIGFyZSBhIHRyaWNrIHVzZWQgYnkgZnJhdWRzdGVycy4gVGhlIGluY2x1ZGVkIGxpbmsgcm91dGVzIHlvdSB0byBhIGZha2UgT25saW5lQmFua2luZyBsb2dpbiBwYWdlIGluIG9yZGVyIHRvIGNvYXggeW91IGludG8gaW5zdGFsbGluZyBhICJzZWN1cml0eSBhcHAiLiBBZnRlciBsb2dnaW5nIG9udG8gdGhlIHBhZ2UsIHlvdSByZWNlaXZlIGRpcmVjdGlvbnMgZm9yIGluc3RhbGxpbmcgdGhlIGFwcCwgd2hpY2ggaXMgYWN0dWFsbHkgYSBUcm9qYW4gdmlydXMsIG9uIHlvdXIgYW5kcm9pZCBzbWFydHBob25lLiBJbiBjYXNlIHRoaXMgaGFwcGVucywgcGxlYXNlIGNvbnRhY3Qgb3VyIE9ubGluZUJhbmtpbmcgSG90bGluZSAwNTA1MDUtMjYxMDAgaW1tZWRpYXRlbHkuIDxwPg0KPHA+PGZvbnQgY29sb3I9IiNGRjAwMDAiPjxiPkJld2FyZSBvZiBmcmF1ZHVsZW50IHRlbGVwaG9uZSBjYWxscyEgPC9iPjwvZm9udD4gUGVyc29ucyBwcmV0ZW5kaW5nIHRvIGJlIEJhbmsgQXVzdHJpYSBlbXBsb3llZXMgYXJlIGNvbnRhY3RpbmcgY3VzdG9tZXJzIHZpYSBwaG9uZS4gV2UgdXJnZSB5b3UgdG8gYmUgb24geW91ciBndWFyZCBhZ2FpbnN0IHRoZXNlIGRlY2VpdGZ1bCBwcmFjdGljZXMuIFBsZWFzZSBkbyBub3QgZGlzY2xvc2UgeW91ciBUQU4gb3IgeW91ciBhY2Nlc3MgZGF0YSBvbiB0aGUgcGhvbmUsIGluY2x1ZGluZyB0byBwZXJzb25zIGNsYWltaW5nIHRvIGJlIEJhbmsgQXVzdHJpYSBlbXBsb3llZXMuIFRoZXNlIGZyYXVkc3RlcnMgYXJlIHNraWxsZnVsIGFuZCBhcmUsIG9mIGNvdXJzZSwgYWN0aW5nIHVuZGVyIGZhbHNlIHByZXRlbmNlcy4gUGxlYXNlIGRvIG5vdCBsZXQgeW91cnNlbGYgYmUgcHJlc3N1cmVkIGludG8gcmV2ZWFsaW5nIHlvdXIgY29kZSBvciBpbnRvIHBhcnRpY2lwYXRpbmcgaW4gInNlY3VyaXR5IGNoZWNrcyIuIEluIGNhc2Ugb2YgZG91YnQsIHBsZWFzZSBjb250YWN0IG91ciBPbmxpbmVCYW5raW5nIEhvdGxpbmU6IDA1MDUwNS0yNjEwMC48cD4=\"/>\n"
            + "            </element>\n"
            + "            <element key=\"read\" val=\"false\"/>\n"
            + "            <element key=\"rejectMessage\" val=\"null\"/>\n"
            + "            <element key=\"rejectable\" val=\"false\"/>\n"
            + "            <element key=\"row\" val=\"48\"/>\n"
            + "            <element key=\"senderId\" val=\"RTA0_N304552    \"/>\n"
            + "            <element key=\"signInfo\" val=\"null\"/>\n"
            + "            <element key=\"status\" val=\"AC\"/>\n"
            + "            <element key=\"subject\" val=\"Falsche Sicherheits-App und Betrugsanrufe\"/>\n"
            + "            <element key=\"subjectEn\" val=\"Fake security apps and fraudulent telephone calls\"/>\n"
            + "            <element key=\"text\"\n"
            + "                     val=\"&#60;p&#62;&#60;font color=&#34;#FF0000&#34;&#62;&#60;b&#62;Falsche Sicherheits-App!&#60;/b&#62;&#60;/font&#62; Derzeit sind Phishing-Mails im Umlauf, welche Sie unter dem Vorwand der erforderlichen Installation einer &#34;Sicherheits-App&#34; auf eine gef&#228;lschte Seite mit einem Banking-Login f&#252;hren. Nach dem Login erhalten Sie eine Installationsanleitung f&#252;r diese App, die in Wirklichkeit ein Trojaner ist, auf Ihrem Android Smartphone. Bitte wenden Sie sich auch in diesem Fall umgehend an unsere OnlineBanking Hotline unter 050505-26100.&#60;p&#62;\\r\\n&#60;p&#62;&#60;font color=&#34;#FF0000&#34;&#62;&#60;b&#62;Achtung Betrugsanrufe!&#60;/b&#62;&#60;/font&#62; Da es erneut Anrufe von falschen Bank Austria Mitarbeiterinnen und Mitarbeitern gibt warnen wir Sie eindringlich vor dieser Betrugsmethode. Bitte geben Sie keine TAN oder Zugangsdaten am Telefon weiter, auch nicht an vermeintliche Bank Austria Mitarbeiterinnen oder Mitarbeiter! Die Betr&#252;ger sind sehr erfinderisch und nennen die Tatsachen nat&#252;rlich nicht beim Namen. Bitte gleichen Sie auch keine &#34;Codes&#34; ab oder machen bei &#34;Sicherheits&#252;berpr&#252;fungen&#34; oder &#228;hnlichem mit. Lassen Sie sich nicht unter Druck setzen und wenden Sie sich ggf. an unsere OnlineBanking Hotline unter 050505-26100.&#60;p&#62;\"/>\n"
            + "            <element key=\"textEn\"\n"
            + "                     val=\"&#60;p&#62;&#60;font color=&#34;#FF0000&#34;&#62;&#60;b&#62;Fake Security app! &#60;/b&#62;&#60;/font&#62; Phishing e-mails are a trick used by fraudsters. The included link routes you to a fake OnlineBanking login page in order to coax you into installing a &#34;security app&#34;. After logging onto the page, you receive directions for installing the app, which is actually a Trojan virus, on your android smartphone. In case this happens, please contact our OnlineBanking Hotline 050505-26100 immediately. &#60;p&#62;\\r\\n&#60;p&#62;&#60;font color=&#34;#FF0000&#34;&#62;&#60;b&#62;Beware of fraudulent telephone calls! &#60;/b&#62;&#60;/font&#62; Persons pretending to be Bank Austria employees are contacting customers via phone. We urge you to be on your guard against these deceitful practices. Please do not disclose your TAN or your access data on the phone, including to persons claiming to be Bank Austria employees. These fraudsters are skillful and are, of course, acting under false pretences. Please do not let yourself be pressured into revealing your code or into participating in &#34;security checks&#34;. In case of doubt, please contact our OnlineBanking Hotline: 050505-26100.&#60;p&#62;\"/>\n"
            + "            <element key=\"type\" val=\"RTA\"/>\n"
            + "        </element>\n"
            + "        <element key=\"type\" val=\"none\"/>\n"
            + "    </datasource>\n"
            + "</datasources>\n";




}
