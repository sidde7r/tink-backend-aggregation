package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.ErrorText;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.HTMLTags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FinalizeBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.InitBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.models.DeviceRegistrationResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

public class SparebankenSorMultiFactorAuthenticator implements BankIdAuthenticatorNO {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SparebankenSorMultiFactorAuthenticator.class);
    private static final String ACTIVATION_CODE_FIELD_KEY = "activationCode";
    private static final int ACTIVATION_CODE_LENGTH = 8;

    private final SparebankenSorApiClient apiClient;
    private final EncapClient encapClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;
    private final SessionStorage sessionStorage;
    private final String mobilenumber;
    private String username;
    private int pollWaitCounter;

    public SparebankenSorMultiFactorAuthenticator(
            SparebankenSorApiClient apiClient,
            EncapClient encapClient,
            SupplementalInformationHelper supplementalInformationHelper,
            Catalog catalog,
            SessionStorage sessionStorage,
            String mobilenumber) {
        this.apiClient = apiClient;
        this.encapClient = encapClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
        this.sessionStorage = sessionStorage;
        this.mobilenumber = mobilenumber;
    }

    @Override
    public String init(String nationalId, String dob, String mobilenumber)
            throws AuthenticationException, AuthorizationException {
        pollWaitCounter = 0;
        this.username = nationalId;

        apiClient.fetchAppInformation(); // only for getting a cookie, possible we must save this
        // cookie for later use in the first login request

        // TODO: Sor returns a 500 for incorrect nationalId, have to verify with check digits before
        // this request.
        VerifyCustomerResponse response = apiClient.verifyCustomer(nationalId, mobilenumber);
        if (!response.isValid()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.configureBankId(nationalId, mobilenumber);
        apiClient.setSessionIdForBankIdUrls();

        InitBankIdBody initBankIdBody = InitBankIdBody.build(dob, mobilenumber);
        String initBankIdResponseString = apiClient.initBankId(initBankIdBody);

        Document doc = Jsoup.parse(initBankIdResponseString);

        handleLoginErrors(doc);

        Element referenceWordsElement = doc.getElementsByClass(HTMLTags.BANKID_REF_WORD).first();

        if (referenceWordsElement == null) {
            throw new IllegalStateException(
                    String.format(
                            "%s: No reference words found. Could not initiate bankId",
                            SparebankenSorConstants.LogTags.BANKID_LOG_TAG.toString()));
        }

        return referenceWordsElement.text();
    }

    private void handleLoginErrors(final Document doc) throws BankIdException {

        Element errorElement = doc.getElementsByClass(HTMLTags.LOGIN_ERROR_CLASS).first();

        if (errorElement == null) {
            return; // No errors found
        }

        if (errorElement.hasText()) {

            if (ErrorText.BANKID_BLOCKED.equalsIgnoreCase(errorElement.text().trim())) {
                throw BankIdError.BLOCKED.exception();
            }
        }

        LOGGER.warn(String.format("Potential unknown login error %s", errorElement.toString()));
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        PollBankIdResponse pollBankIdResponse = apiClient.pollBankId();
        String pollStatus = pollBankIdResponse.getStatus();

        switch (pollStatus.toLowerCase()) {
            case SparebankenSorConstants.BankIdStatus.NONE:
                pollWaitCounter++;
                return BankIdStatus.WAITING;
            case SparebankenSorConstants.BankIdStatus.COMPLETED:
                continueActivation();
                return BankIdStatus.DONE;
            case SparebankenSorConstants.BankIdStatus.ERROR:
                // Sparebanken Sor keeps on polling until error status is returned even if user
                // cancels
                // bankId.
                if (pollWaitCounter > 15) {
                    return BankIdStatus.TIMEOUT;
                } else {
                    LOGGER.info(
                            String.format(
                                    "%s: Received error status when polling bankId",
                                    SparebankenSorConstants.LogTags.BANKID_LOG_TAG.toString()));
                    return BankIdStatus.FAILED_UNKNOWN;
                }
            default:
                LOGGER.info(
                        String.format(
                                "%s: Unknown poll status: %s. Number of polls: %s",
                                SparebankenSorConstants.LogTags.BANKID_LOG_TAG.toString(),
                                pollStatus,
                                pollWaitCounter));
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private void continueActivation() throws SupplementalInfoException {
        String evryToken = finalizeBankIdAuthentication();
        sessionStorage.put(Storage.EVRY_TOKEN, evryToken);
        executeLogin(evryToken);

        SendSmsRequest sendSmsRequest = SendSmsRequest.build(mobilenumber);
        HttpResponse response = apiClient.sendSms(sendSmsRequest);

        if (!Objects.equals(response.getStatus(), HttpStatusCodes.STATUS_CODE_OK)) {
            throw new IllegalStateException(
                    "Sparebanken Sor - Something went wrong when sending request for getting activation code via sms");
        }

        Map<String, String> activationCodeResponse =
                supplementalInformationHelper.askSupplementalInformation(getActivationCodeField());
        try {
            DeviceRegistrationResponse deviceRegistrationResponse =
                    encapClient.registerDevice(
                            username, activationCodeResponse.get(ACTIVATION_CODE_FIELD_KEY));
            evryToken = deviceRegistrationResponse.getDeviceToken();
            sessionStorage.put(Storage.EVRY_TOKEN, evryToken);
            executeLogin(evryToken);
        } finally {
            encapClient.saveDevice();
        }
    }

    private void executeLogin(String evryToken) {
        FirstLoginRequest firstLoginRequest = FirstLoginRequest.build(evryToken);
        FirstLoginResponse firstLoginResponse = apiClient.loginFirstStep(firstLoginRequest);

        sessionStorage.put(
                SparebankenSorConstants.Storage.ACCESS_TOKEN, firstLoginResponse.getAccessToken());
        // We might want to add some check on the second login response. Not doing it now since I
        // don't know
        // what fields/values that signal an error. But if we get errors here we should add a check
        // for it.
        apiClient.loginSecondStep();
    }

    private String finalizeBankIdAuthentication() {
        FinalizeBankIdBody finalizeBankIdBody = FinalizeBankIdBody.build();
        String finalizedBankIdResponseString = apiClient.finalizeBankId(finalizeBankIdBody);
        Document doc = Jsoup.parse(finalizedBankIdResponseString);
        Element scriptTag = doc.getElementsByTag("script").get(0);
        String scriptData = scriptTag.dataNodes().get(0).getWholeData();
        return StringUtils.substringBetween(scriptData, "?so=", "&");
    }

    private Field getActivationCodeField() {
        return Field.builder()
                .description(catalog.getString("Activation code"))
                .name(ACTIVATION_CODE_FIELD_KEY)
                .numeric(true)
                .minLength(ACTIVATION_CODE_LENGTH)
                .maxLength(ACTIVATION_CODE_LENGTH)
                .hint(StringUtils.repeat("N", ACTIVATION_CODE_LENGTH))
                .pattern(String.format("([0-9]{%d})", ACTIVATION_CODE_LENGTH))
                .patternError("The activation code you entered is not valid")
                .build();
    }
}
