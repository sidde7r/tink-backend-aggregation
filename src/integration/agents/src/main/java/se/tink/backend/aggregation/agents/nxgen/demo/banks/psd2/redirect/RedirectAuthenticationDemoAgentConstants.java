package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class RedirectAuthenticationDemoAgentConstants {
    public static final String IT_DEMO_PROVIDER_SUCCESS_CASE = "it-test-open-banking-redirect";
    public static final String IT_DEMO_PROVIDER_FAILURE_CASE =
            "it-test-open-banking-redirect-failed";
    public static final String IT_DEMO_PROVIDER_CANCEL_CASE =
            "it-test-open-banking-redirect-cancelled";
    public static final String IT_DEMO_PROVIDER_PAYMENT_FAILURE_CASE =
            "it-test-open-banking-redirect-payment-failed";
    public static final String IT_DEMO_PROVIDER_PAYMENT_CANCEL_CASE =
            "it-test-open-banking-redirect-payment-cancelled";

    public static final String FR_DEMO_PROVIDER_SUCCESS_CASE = "fr-test-open-banking-redirect";
    public static final String FR_DEMO_PROVIDER_FAILURE_CASE =
            "fr-test-open-banking-redirect-failed";
    public static final String FR_DEMO_PROVIDER_CANCEL_CASE =
            "fr-test-open-banking-redirect-cancelled";
    public static final String FR_DEMO_PROVIDER_PAYMENT_FAILURE_CASE =
            "fr-test-open-banking-redirect-payment-failed";
    public static final String FR_DEMO_PROVIDER_PAYMENT_CANCEL_CASE =
            "fr-test-open-banking-redirect-payment-cancelled";

    public static final String UK_DEMO_PROVIDER_SUCCESS_CASE = "uk-test-open-banking-redirect";
    public static final String UK_DEMO_PROVIDER_FAILURE_CASE =
            "uk-test-open-banking-redirect-failed";
    public static final String UK_DEMO_PROVIDER_CANCEL_CASE =
            "uk-test-open-banking-redirect-cancelled";
    public static final String UK_DEMO_PROVIDER_TEMPORARY_ERROR_CASE =
            "uk-test-open-banking-redirect-temporary-error";
    public static final String UK_DEMO_PROVIDER_NO_ACCOUNTS_RETURNED_CASE =
            "uk-test-open-banking-redirect-no-accounts-returned";
    public static final String DEMO_PROVIDER_ONLY_SAVINGS_AND_CHECKING = ".*-saving-and-checking";
    public static final String DEMO_PROVIDER_CONFIGURABLE_SESSION_CASE_REGEX =
            ".*-configurable-session-expiry";
    public static final String UK_DEMO_PROVIDER_PAYMENT_FAILURE_CASE =
            "uk-test-open-banking-redirect-payment-failed";
    public static final String UK_DEMO_PROVIDER_PAYMENT_CANCEL_CASE =
            "uk-test-open-banking-redirect-payment-cancelled";

    public static final String OXFORD_PREPROD = "oxford-preprod";
    public static final String OXFORD_PREPROD_CALLBACK =
            "https://api.preprod.oxford.tink.com/api/v1/credentials/third-party/callback";

    public static final String OXFORD_STAGING = "oxford-staging";
    public static final String OXFORD_STAGING_CALLBACK =
            "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback";

    public static final TransferExecutionException FAILED_CASE_EXCEPTION =
            TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setEndUserMessage(
                            "The transfer amount is larger than what is available on the account (test)")
                    .setMessage(
                            "The transfer amount is larger than what is available on the account (test)")
                    .build();
    public static final TransferExecutionException CANCELLED_CASE_EXCEPTION =
            TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setEndUserMessage("Cancel on payment signing (test)")
                    .setMessage("Cancel on payment signing (test)")
                    .build();

    public static class CreditCard {
        public static final String ACCOUNTID = "1122 3344 - 1234";
        public static final String CREDITCARDNUMBER = "1234 5678 9101 1121";
        public static final String NAMEONCREDITCARD = "Tink Tinkerton";
        public static final String ACCOUNTNAME = "Basic Credit Card";
        public static final double BALANCE = -1456D;
        public static final double AVAILABLECREDIT = 8543D;
    }

    public static class LoanAccount {
        public static final String MORTGAGEID = "7777-333333333333";
        public static final String BLANCOID = "7777-333334444444";
        public static final String MORTGAGELOANNAME = "Loan";
        public static final String BLANCOLOANNAME = "Santander";
        public static final double MORTGAGEINTERESTNAME = 0.53;
        public static final double BLANCOINTERESTNAME = 1.73;
        public static final double MORTGAGEBALANCE = -2300D;
        public static final double BLANCOBALANCE = -5D;
    }

    public static class IdentityData {
        public static final String FIRST_NAME = "Jane";
        public static final String SURNAME = "Doe";
    }

    public static class InvestmentAccounts {
        public static final String ACCOUNTID = "7777-444444444444";
        public static final String ACCOUNTNAME = "SmallInvestment";
        public static final double ACCOUNTBALANCE = 4563;
    }

    public static class Step {
        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String SUFFICIENT_FUNDS = "SUFFICIENT_FUNDS";
        public static final String EXECUTE_PAYMENT = "EXECUTE_PAYMENT";
    }

    public static class StaticAccountUK {
        public static final String ACCOUNT_ID = "23147071417779";
        public static final String ACCOUNT_NAME = "Checking Account tink low balance";
        public static final double BALANCE = 50;
        public static final double AVAILABLE_BALANCE = -10;
        public static final double CREDIT_LIMIT = 100;
        public static final String ACCOUNT_IDENTIFIERS = "staticTestAccount";
    }
}
