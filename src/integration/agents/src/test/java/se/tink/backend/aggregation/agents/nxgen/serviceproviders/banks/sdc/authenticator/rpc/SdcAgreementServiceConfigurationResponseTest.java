package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SdcAgreementServiceConfigurationResponseTest {

    @Test
    public void testFindPhoneNumber() throws Exception {

        SdcAgreementServiceConfigurationResponse response =
                SerializationUtils.deserializeFromString(
                        TEST_DATA, SdcAgreementServiceConfigurationResponse.class);

        assertTrue(response.findFirstPhoneNumber().get().hasPhoneNumber());
    }

    private static final String TEST_DATA =
            "{"
                    + "\"paymentProfile\": {"
                    + "\"defaultPaymentAccount\": null,"
                    + "\"paymentConfirmation\": \"always\","
                    + "\"paymentShowSortOrder\": false,"
                    + "\"paymentShowRegistration\": false,"
                    + "\"paymentDueDateDefault\": false,"
                    + "\"maxPaymentAmount\": {"
                    + "\"localizedValue\": \"25 000,00\","
                    + "\"localizedValueWithCurrency\": \"25 000,00\","
                    + "\"value\": 2500000,"
                    + "\"scale\": 2,"
                    + "\"currency\": null,"
                    + "\"localizedValueWithCurrencyAtEnd\": null,"
                    + "\"roundedAmountWithIsoCurrency\": null,"
                    + "\"roundedAmountWithCurrencySymbol\": null"
                    + "}"
                    + "},"
                    + "\"serviceConfiguration\": {"
                    + "\"saxoTrader\": false,"
                    + "\"loan\": true,"
                    + "\"giro\": true,"
                    + "\"transfer\": true,"
                    + "\"outbox\": true,"
                    + "\"epayment\": true,"
                    + "\"creditCard\": true,"
                    + "\"cardsOverview\": false,"
                    + "\"blockCard\": false,"
                    + "\"custody\": false,"
                    + "\"accounts\": true,"
                    + "\"sharedAgreementAccounts\": true,"
                    + "\"spendingOverview\": false,"
                    + "\"communicationRead\": true,"
                    + "\"communicationWrite\": true,"
                    + "\"netmeeting\": true,"
                    + "\"investment\": {"
                    + "\"realtime\": false,"
                    + "\"security\": true,"
                    + "\"deposit\": false,"
                    + "\"trade\": false,"
                    + "\"orderBook\": false"
                    + "},"
                    + "\"bsAgreementsShow\": false,"
                    + "\"bsAgreementsDecline\": false,"
                    + "\"bsAgreementsCreate\": false,"
                    + "\"regularTransfersShow\": true,"
                    + "\"regularTransfersCreate\": true,"
                    + "\"regularTransfersDecline\": true,"
                    + "\"aftaleGiroShow\": true,"
                    + "\"aftaleGiroCreate\": true,"
                    + "\"aftaleGiroDelete\": true,"
                    + "\"autoGiroReceiver\": false,"
                    + "\"autoGiroList\": false,"
                    + "\"autoGiroRegisterSign\": false,"
                    + "\"autoGiroCancel\": false,"
                    + "\"eCardAgreementsShow\": false,"
                    + "\"eCardCreate\": false,"
                    + "\"eCardChange\": false,"
                    + "\"eCardToBS\": false,"
                    + "\"bsToECard\": false,"
                    + "\"eInvoiceAgreementShow\": true,"
                    + "\"eInvoiceCreate\": true,"
                    + "\"eInvoiceChange\": true,"
                    + "\"fbfPrimaryOwner\": true,"
                    + "\"snapCash\": true,"
                    + "\"userNotificationsServicesRead\": false,"
                    + "\"userNotificationsServicesWrite\": false,"
                    + "\"totalkredit\": false"
                    + "},"
                    + "\"ownAgreement\": true,"
                    + "\"phoneNumbers\": [{"
                    + "\"phoneNumber\": \"12345678\","
                    + "\"countryType\": \"dk.sdc.datatypes.enumerations.types.CountryType:: isoCountryCode: NO name: Norway isoCurrencyCodes: NOK currencies: NOK\""
                    + "}]"
                    + "}";
}
