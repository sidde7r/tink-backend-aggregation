package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex;

import org.junit.Ignore;

@Ignore
public class AmericanExpressV62PredicatesTestData {
    public static final String CARD_NUMBER = "12345";
    public static final String PARTNER_CARD_NUMBER = "12346";
    public static final String MAIN_CARD =
            "{\"sortedIndex\":0,"
                    + "\"cardNumberDisplay\":\"XXX-"
                    + CARD_NUMBER
                    + "\","
                    + "\"cardProductName\":\"SAS Amex Premium\","
                    + "\"homeCountryLocale\":\"sv_SE\","
                    + "\"cardKey\":\"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF\","
                    + "\"cardId\":\"66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666\","
                    + "\"accountToken\":\"000000000000000\","
                    + "\"accountKey\":\"EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE\","
                    + "\"cardArt\":[{\"tag\":\"big-image\",\"url\":\"https://secure.cmax.americanexpress.com/Internet/CardArt/EMEA/se-cardasset-config/images/IMAGE.gif\"},"
                    + "{\"tag\":\"big-image\",\"url\":\"https://secure.cmax.americanexpress.com/Internet/CardArt/EMEA/se-cardasset-config/images/IMAGE.gif\"}],"
                    + "\"financialTab\":{\"balanceDue\":{\"label\":\"Återstående saldo\",\"value\":\"0,00 kr\"},\"paymentDueInfo\":{\"value\":\"Betalning krävs inte nu\",\"iconName\":\"iconPaymentDue\"},\"statementBalance\":{\"label\":\"Fakturans saldo\",\"value\":\"11.484,13 kr\",\"detail\":\"(4 nov 2018 - 2 dec 2018)\"},\"recentPaymentsAndCredits\":{\"label\":\"Nya inbetalningar\",\"value\":\"11.484,13 kr\"},\"recentCharges\":{\"label\":\"Nya köp\",\"value\":\"1.952,32 kr\"},\"totalBalance\":{\"label\":\"Aktuellt saldo\",\"value\":\"1.952,32 kr\"},\"availableCredit\":{\"label\":\"Tillgänglig kredit\",\"value\":\"145.476,00 kr\"},\"transactionAndStatementsCTA\":{\"label\":\"Transaktioner och fakturor\",\"iconName\":\"iconBilling\"}},\"overviewTab\":{\"mainBalance\":{\"label\":\"Aktuelltsaldo\",\"value\":\"1.952,32 kr\"},\"balanceDue\":{\"label\":\"Återstående saldo\",\"value\":\"0,00 kr\"},\"paymentDueInfo\":{\"value\":\"Betalning krävs inte nu\",\"iconName\":\"iconPaymentDue\"},\"availableCredit\":{\"label\":\"Tillgänglig kredit\",\"value\":\"145.476,00 kr\"},\"loyaltyBalance\":{\"label\":\"För fullständigt poängsaldo, besök sas.se/eurobonus\"}},\"accountTab\":{\"sections\":[{\"title\":\"Konto\",\"options\":[{\"label\":\"Hantera kort\",\"type\":\"CARD_MANAGEMENT\",\"iconName\":\"iconCard\"}]},{\"title\":\"Inställningar\",\"options\":[{\"label\":\"Touch ID\",\"type\":\"TOUCH_ID\",\"iconName\":\"iconFraudProtection\"}]},{\"title\":\"Medlemskap\",\"options\":[{\"label\":\"Bjud in en vän\",\"type\":\"MGM\",\"iconName\":\"iconP2p\"}]}],\"footer\":[{\"label\":\"Hjälp / Kontakta oss\",\"type\":\"HELP_CONTACT_US\"},{\"label\":\"Villkor & sekretess\",\"type\":\"LEGAL_PRIVACY\"},{\"label\":\"Logga ut\",\"type\":\"LOGOUT\"}]},\"rewards\":{\"message\":\"För fullständigt poängsaldo, besök sas.se/eurobonus\"},\"capabilities\":{\"activateRegister\":    {\"label\":\"Aktivera och lägg till Kort\",\"showInMenu\":false,\"enabled\":true},\"addSomeoneToYourCard\":{\"label\":\"Lägg till någon till ditt konto\",\"webURL\":\"https://global.americanexpress.com/acq/intl/deca/emea/application/view.do?request_ty    pe=authreg_view&Face=sv_SE&fl=V&jt=SP\",\"showInMenu\":false,\"enabled\":true},\"cml\":{\"showInMenu\":false,\"enabled\":true},\"help\":{\"label\":\"Help / Contact us\",\"showInMenu\":false,\"enabled\":true},\"logout\":{\"label\":\"Log Out\",\"showInMenu\":false,    \"enabled\":true},\"manageCardsV2\":{\"label\":\"Hantera Kort\",\"showInMenu\":false,\"enabled\":true},\"nativeAppMGM\":{\"label\":\"Bjud in och bli belönad\",\"showInMenu\":false,\"enabled\":true},\"pdfStatements\":{\"label\":\"PDF Fakturor\",\"showInMenu\":false    ,\"enabled\":true},\"pendingTransaction\":{\"showInMenu\":false,\"enabled\":true},\"removeCardV2\":{\"label\":\"Ta bort ett Kort\",\"showInMenu\":false,\"enabled\":true},\"statements\":{\"label\":\"Se transaktioner\",\"showInMenu\":false,\"enabled\":true},\"terms\":{\"label\":\"Legal / Privacy\",\"showInMenu\":false,\"enabled\":true},\"transactionDtl\":{\"showInMenu\":false,\"enabled\":true},\"transactions\":{\"label\":\"Se transaktioner\",\"showInMenu\":false,\"enabled\":true}},"
                    + "\"cardEndingDisplay\":\"Kortetssistasiffror - "
                    + CARD_NUMBER
                    + "\",\"nameOnCard\":\"OWNER NAME\",\"canceled\":false}";
    public static final String PARTNER_CARD =
            "{\"sortedIndex\":1,"
                    + "\"cardNumberDisplay\":\"XXX-"
                    + PARTNER_CARD_NUMBER
                    + "\","
                    + "\"cardProductName\":\"SAS Amex Premium\","
                    + "\"homeCountryLocale\":\"sv_SE\","
                    + "\"cardKey\":\"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF\","
                    + "\"cardId\":\"66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666\","
                    + "\"accountToken\":\"000000000000000\","
                    + "\"accountKey\":\"EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE\","
                    + "\"cardArt\":[{\"tag\":\"big-image\",\"url\":\"https://secure.cmax.americanexpress.com/Internet/CardArt/EMEA/se-cardasset-config/images/IMAGE.gif\"},"
                    + "{\"tag\":\"big-image\",\"url\":\"https://secure.cmax.americanexpress.com/Internet/CardArt/EMEA/se-cardasset-config/images/IMAGE.gif\"}],"
                    + "\"financialTab\":{\"balanceDue\":{\"label\":\"Återstående saldo\",\"value\":\"0,00 kr\"},\"paymentDueInfo\":{\"value\":\"Betalning krävs inte nu\",\"iconName\":\"iconPaymentDue\"},\"statementBalance\":{\"label\":\"Fakturans saldo\",\"value\":\"11.484,13 kr\",\"detail\":\"(4 nov 2018 - 2 dec 2018)\"},\"recentPaymentsAndCredits\":{\"label\":\"Nya inbetalningar\",\"value\":\"11.484,13 kr\"},\"recentCharges\":{\"label\":\"Nya köp\",\"value\":\"1.952,32 kr\"},\"totalBalance\":{\"label\":\"Aktuellt saldo\",\"value\":\"1.952,32 kr\"},\"availableCredit\":{\"label\":\"Tillgänglig kredit\",\"value\":\"145.476,00 kr\"},\"transactionAndStatementsCTA\":{\"label\":\"Transaktioner och fakturor\",\"iconName\":\"iconBilling\"}},\"overviewTab\":{\"mainBalance\":{\"label\":\"Aktuelltsaldo\",\"value\":\"1.952,32 kr\"},\"balanceDue\":{\"label\":\"Återstående saldo\",\"value\":\"0,00 kr\"},\"paymentDueInfo\":{\"value\":\"Betalning krävs inte nu\",\"iconName\":\"iconPaymentDue\"},\"availableCredit\":{\"label\":\"Tillgänglig kredit\",\"value\":\"145.476,00 kr\"},\"loyaltyBalance\":{\"label\":\"För fullständigt poängsaldo, besök sas.se/eurobonus\"}},\"accountTab\":{\"sections\":[{\"title\":\"Konto\",\"options\":[{\"label\":\"Hantera kort\",\"type\":\"CARD_MANAGEMENT\",\"iconName\":\"iconCard\"}]},{\"title\":\"Inställningar\",\"options\":[{\"label\":\"Touch ID\",\"type\":\"TOUCH_ID\",\"iconName\":\"iconFraudProtection\"}]},{\"title\":\"Medlemskap\",\"options\":[{\"label\":\"Bjud in en vän\",\"type\":\"MGM\",\"iconName\":\"iconP2p\"}]}],\"footer\":[{\"label\":\"Hjälp / Kontakta oss\",\"type\":\"HELP_CONTACT_US\"},{\"label\":\"Villkor & sekretess\",\"type\":\"LEGAL_PRIVACY\"},{\"label\":\"Logga ut\",\"type\":\"LOGOUT\"}]},\"rewards\":{\"message\":\"För fullständigt poängsaldo, besök sas.se/eurobonus\"},\"capabilities\":{\"activateRegister\":    {\"label\":\"Aktivera och lägg till Kort\",\"showInMenu\":false,\"enabled\":true},\"addSomeoneToYourCard\":{\"label\":\"Lägg till någon till ditt konto\",\"webURL\":\"https://global.americanexpress.com/acq/intl/deca/emea/application/view.do?request_ty    pe=authreg_view&Face=sv_SE&fl=V&jt=SP\",\"showInMenu\":false,\"enabled\":true},\"cml\":{\"showInMenu\":false,\"enabled\":true},\"help\":{\"label\":\"Help / Contact us\",\"showInMenu\":false,\"enabled\":true},\"logout\":{\"label\":\"Log Out\",\"showInMenu\":false,    \"enabled\":true},\"manageCardsV2\":{\"label\":\"Hantera Kort\",\"showInMenu\":false,\"enabled\":true},\"nativeAppMGM\":{\"label\":\"Bjud in och bli belönad\",\"showInMenu\":false,\"enabled\":true},\"pdfStatements\":{\"label\":\"PDF Fakturor\",\"showInMenu\":false    ,\"enabled\":true},\"pendingTransaction\":{\"showInMenu\":false,\"enabled\":true},\"removeCardV2\":{\"label\":\"Ta bort ett Kort\",\"showInMenu\":false,\"enabled\":true},\"statements\":{\"label\":\"Se transaktioner\",\"showInMenu\":false,\"enabled\":true},\"terms\":{\"label\":\"Legal / Privacy\",\"showInMenu\":false,\"enabled\":true},\"transactionDtl\":{\"showInMenu\":false,\"enabled\":true},\"transactions\":{\"label\":\"Se transaktioner\",\"showInMenu\":false,\"enabled\":true}},"
                    + "\"cardEndingDisplay\":\"Kortetssistasiffror - "
                    + PARTNER_CARD_NUMBER
                    + "\",\"nameOnCard\":\"OWNER NAME\",\"canceled\":false}";

    public static final String PARTNER_SUBCARD =
            "{\"cardMemberName\":\"PARTNER NAME\","
                    + "\"cardProductName\":\"SAS Amex Premium-"
                    + PARTNER_CARD_NUMBER
                    + "\",\"suppIndex\":\"01\"}";
    public static final String REGULAR_SUBCARD =
            "{\"cardMemberName\":\"MAIN OWNER\",\"cardProductName\":\"SAS Amex Premium-11111\",\"suppIndex\":\"00\"}";
    public static final String TRANSACTION =
            "{\"transactionId\":\"AAAAAAAAAAAAAAAAAAAAAAA---AAAA---AAAAAAAA\","
                    + "\"transactionReference\":\"AAAAAAAAAAAAAAAAAAAAAAA\","
                    + "\"secondaryId\":\"222222222222222\","
                    + "\"billingCycleIndex\":1,"
                    + "\"billingCycleDate\":20181202,"
                    + "\"suppIndex\":\"02\","
                    + "\"displaySuppIcon\":true,"
                    + "\"cardMemberName\":\"PARTNER NAME\","
                    + "\"type\":\"DEBIT\","
                    + "\"chargeDate\":{\"formattedDate\":\"10 nov 2018\",\"rawValue\":20181111},"
                    + "\"amount\":{\"formattedAmount\":\"1,00 kr\",\"rawValue\":1.0,\"stringRawValue\":\"1.0\"},"
                    + "\"description\":[\"SOME SHOPPING\"],"
                    + "\"extendedTransactionDetails\":{\"merchantName\":\"SOME STORE 123\",\"address\":[\"S-111 10\",\"SWEDEN\"],"
                    + "\"processDate\":{\"formattedDate\":\"11 nov 2018\",\"rawValue\":20181111}},"
                    + "\"formattedAmount\":\"1,1 kr\"}";
}
