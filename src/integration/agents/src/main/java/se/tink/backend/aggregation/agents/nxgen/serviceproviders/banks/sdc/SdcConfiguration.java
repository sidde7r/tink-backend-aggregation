package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCustodyDetailsModel;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public abstract class SdcConfiguration implements ClientConfiguration {
    protected String baseUrl;
    protected final String bankCode;

    public SdcConfiguration(Provider provider) {
        this.bankCode = provider.getPayload();
    }

    public URL getInitSessionUrl() {
        return new URL(baseUrl + SdcConstants.Url.INIT_SESSION_PATH);
    }

    public URL getBankIdLoginUrl() {
        return new URL(baseUrl + SdcConstants.Url.BANK_ID_LOGIN_PATH_SE);
    }

    public URL getAgreementsUrl() {
        return new URL(baseUrl + SdcConstants.Url.AGREEMENTS_PATH_SE);
    }

    public URL getSelectAgreementUrl() {
        return new URL(baseUrl + SdcConstants.Url.SELECT_AGREEMENT_PATH);
    }

    public URL getLogoutUrl() {
        return new URL(baseUrl + SdcConstants.Url.LOGOUT_PATH);
    }

    public URL getFilterAccountsUrl() {
        return new URL(baseUrl + SdcConstants.Url.FILTER_ACCOUNTS_PATH);
    }

    public URL getSearchTransactionsUrl() {
        return new URL(baseUrl + SdcConstants.Url.SEARCH_TRANSACTIONS_PATH);
    }

    public URL getUrlForTotalKreditLoans() {
        return new URL(baseUrl + SdcConstants.Url.TOTALKREDIT_LOANS_PATH);
    }

    public URL getPinLogonUrl() {
        return new URL(baseUrl + SdcConstants.Url.LOGON_PIN_PATH);
    }

    public URL getPinDeviceUrl() {
        return new URL(baseUrl + SdcConstants.Url.PIN_DEVICE_PATH);
    }

    public URL getSendOTPRequestUrl() {
        return new URL(baseUrl + SdcConstants.Url.SEND_OTP_REQUEST_PATH);
    }

    public URL getSignOTPUrl() {
        return new URL(baseUrl + SdcConstants.Url.SIGN_OTP_PATH);
    }

    public URL getChallengeUrl() {
        return new URL(baseUrl + SdcConstants.Url.CHALLENGE_PATH);
    }

    public URL getCustodyOverviewUrl() {
        return new URL(baseUrl + SdcConstants.Url.INVESTMENT_DEPOSITS_OVERVIEW_PATH);
    }

    public URL getCustodyContentUrl(SdcCustodyDetailsModel custodyDetails) {
        return new URL(
                baseUrl
                        + SdcConstants.Url.INVESTMENT_DEPOSITS_CONTENT_PATH
                        + custodyDetails.getId());
    }

    public URL getListLoansUrl() {
        return new URL(baseUrl + SdcConstants.Url.LOAN_LIST_PATH);
    }

    public URL getListCreditCardsUrl() {
        return new URL(baseUrl + SdcConstants.Url.CREDIT_CARD_LIST_PATH);
    }

    public URL getListCreditCardProviderAccountsUrl() {
        return new URL(baseUrl + SdcConstants.Url.CREDIT_CARD_PROVIDER_ACCOUNTS_LIST_PATH);
    }

    public URL getSearchCreditCardTransactionsUrl() {
        return new URL(baseUrl + SdcConstants.Url.CREDIT_CARD_TRANSACTIONS_PATH);
    }

    public String getBankCode() {
        return bankCode;
    }

    public abstract boolean canRetrieveInvestmentData();

    // can be removed once we stop logging and start fetching
    public abstract LogTag getLoanLogTag();

    public abstract LogTag getInvestmentsLogTag();

    public abstract String getPhoneCountryCode();

    // parse error messages to find reason for error
    public boolean isNotCustomer(String errorMessage) {
        return SdcConstants.ErrorMessage.isNotCustomer(errorMessage);
    }

    public boolean isLoginError(String errorMessage) {
        for (SdcConstants.ErrorMessage error : SdcConstants.ErrorMessage.values()) {
            if (error.isLoginError(errorMessage)) {
                return true;
            }
        }

        return false;
    }

    public boolean isUserBlocked(String errorMessage) {
        for (SdcConstants.ErrorMessage error : SdcConstants.ErrorMessage.values()) {
            if (error.isBlocked(errorMessage)) {
                return true;
            }
        }

        return false;
    }

    public boolean isDeviceRegistrationNotAllowed(String errorMessage) {
        return SdcConstants.ErrorMessage.isDeviceRegistrationNotAllowed(errorMessage);
    }
}
