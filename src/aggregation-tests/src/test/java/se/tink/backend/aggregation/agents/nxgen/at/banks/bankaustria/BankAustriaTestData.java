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

}
