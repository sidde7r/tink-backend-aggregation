package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DanskeBankUrl {
    private static final String BASE_URL_MB = "https://mb.danskebank.dk/smartphones/gmb.svc";
    private static final String BASE_URL_PRIVATESERVICE = "https://privateservice01.danskebank.com";

    public static final String EINVOICE_APPROVE_CHALLENGE_FORMAT = BASE_URL_MB + "/eInvoices/%s/Approve/Challenge";
    public static final String EINVOICE_DETAILS_FORMAT = BASE_URL_MB + "/eInvoices/%s/Approve/Details";
    public static final String EINVOICE_APPROVE_FORMAT = BASE_URL_MB + "/eInvoices/%s/Approve";
    public static final String EINVOICE_LIST = BASE_URL_MB + "/eInvoices";
    public static final String SERVICE_ACCOUNTS_ID_TRANSACTION_FUTURE = BASE_URL_MB + "/Service/Accounts/%s/Transactions/Future";
    public static final String SERVICE_ACCOUNTS_ID_TRANSACTION = BASE_URL_MB + "/Service/Accounts/%s/Transactions";
    public static final String ACCOUNTS_BILLS_DETAILS = BASE_URL_MB + "/Accounts/Bills/Details";
    public static final String ACCOUNTS_TRANSFER_DETAILS = BASE_URL_MB + "/Accounts/Transfer/Details";
    public static final String TRANSFER = BASE_URL_MB + "/Service/Accounts/Transfer";
    public static final String TRANSFER_CONFIRMATION = BASE_URL_MB + "/Service/Challenge/Accept";
    public static final String BILL_CONFIRMATION = BASE_URL_MB + "/Accounts/Bills/Challenge";
    public static final String BILL = BASE_URL_MB + "/Accounts/Bills";
    public static final String ACCOUNTS = BASE_URL_MB + "/Accounts";
    public static final String LOGIN_CHALLENGE = BASE_URL_MB + "/Login/Challenge";
    public static final String LOGIN = BASE_URL_MB + "/Login";
    public static final String CREATESESSION = BASE_URL_MB + "/CreateSession";
    public static final String INITSESSION = BASE_URL_MB + "/InitSession";
    public static final String CUSTOMER_SETTINGS = BASE_URL_PRIVATESERVICE + "/RES/Customer/customerSettings/loginInfo";
    public static final String CARDS = BASE_URL_PRIVATESERVICE + "/RES/Cards/Cards";
    public static final String PORTFOLIOS = BASE_URL_MB + "/Portfolios";
    public static final String PORTFOLIO_PAPERS = PORTFOLIOS + "/%s/Papers";

    public static String eInvoiceApproveChallenge(String eInvoiceId) throws UnsupportedEncodingException {
        return String.format(EINVOICE_APPROVE_CHALLENGE_FORMAT, URLEncoder.encode(eInvoiceId, "UTF-8"));
    }

    public static String eInvoiceDetails(String eInvoiceId) throws UnsupportedEncodingException {
        return String.format(EINVOICE_DETAILS_FORMAT, URLEncoder.encode(eInvoiceId, "UTF-8"));
    }

    public static String eInvoiceApprove(String eInvoiceId) throws UnsupportedEncodingException {
        return String.format(EINVOICE_APPROVE_FORMAT, URLEncoder.encode(eInvoiceId, "UTF-8"));
    }

    public static String portfolioPapers(String portfolioId) {
        try {
            return String.format(PORTFOLIO_PAPERS, URLEncoder.encode(portfolioId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("This never happens when the encoding is correct");
        }

    }

    public static String serviceAccountsIdTransaction(String accountId, DanskeBankV2Agent.TransactionType transactionType) {
        switch (transactionType) {
        case FUTURE:
            return String.format(SERVICE_ACCOUNTS_ID_TRANSACTION_FUTURE, accountId);
        default:
            return String.format(SERVICE_ACCOUNTS_ID_TRANSACTION, accountId);
        }
    }
}
