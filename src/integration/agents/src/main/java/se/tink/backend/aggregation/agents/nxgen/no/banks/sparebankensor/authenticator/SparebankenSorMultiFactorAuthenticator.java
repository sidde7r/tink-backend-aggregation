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
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FinalizeBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.FirstLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.InitBankIdBody;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.SendSmsRequest;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc.VerifyCustomerResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticatorNO;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

public class SparebankenSorMultiFactorAuthenticator implements BankIdAuthenticatorNO {
    private static final AggregationLogger LOGGER = new AggregationLogger(SparebankenSorMultiFactorAuthenticator.class);
    private static final String ACTIVATION_CODE_FIELD_KEY = "activationCode";
    private static final int ACTIVATION_CODE_LENGTH = 8;

    private final SparebankenSorApiClient apiClient;
    private final EncapClient encapClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;
    private final SessionStorage sessionStorage;
    private final String mobilenumber;
    private int pollWaitCounter;

    public SparebankenSorMultiFactorAuthenticator(SparebankenSorApiClient apiClient, EncapClient encapClient,
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog,
            SessionStorage sessionStorage, String mobilenumber) {
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

        apiClient.fetchAppInformation(); // only for getting a cookie, possible we must save this cookie for later use in the first login request

        // TODO: Sor returns a 500 for incorrect nationalId, have to verify with check digits before this request.
        VerifyCustomerResponse response = apiClient.verifyCustomer(nationalId, mobilenumber);
        if (!response.isValid()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.configureBankId(nationalId, mobilenumber);
        apiClient.setSessionIdForBankIdUrls();

        InitBankIdBody initBankIdBody = InitBankIdBody.build(dob, mobilenumber);
        String initBankIdResponseString = apiClient.initBankId(initBankIdBody);

        Document doc = Jsoup.parse(initBankIdResponseString);
        Element referenceWordsElement = doc.getElementsByClass("bidm_ref-word").first();

        if (referenceWordsElement == null) {
            throw new IllegalStateException(String.format(
                    "%s: No reference words found. Could not initiate bankId",
                    SparebankenSorConstants.LogTags.BANKID_LOG_TAG.toString()));
        }

        return referenceWordsElement.text();
    }

    @Override
    public BankIdStatus collect() throws AuthenticationException, AuthorizationException {
        PollBankIdResponse pollBankIdResponse = apiClient.pollBankId();
        String pollStatus = pollBankIdResponse.getStatus();

        if (Objects.equals(pollStatus.toLowerCase(), SparebankenSorConstants.BankIdStatus.NONE)) {
            pollWaitCounter++;
            return BankIdStatus.WAITING;
        } else if (Objects.equals(pollStatus.toLowerCase(), SparebankenSorConstants.BankIdStatus.COMPLETED)) {
            continueActivation();
            return BankIdStatus.DONE;
        } else if (Objects.equals(pollStatus.toLowerCase(), SparebankenSorConstants.BankIdStatus.ERROR)) {
            // Sparebanken Sor keeps on polling until error status is returned even if user cancels bankId.
            if (pollWaitCounter > 15) {
                return BankIdStatus.TIMEOUT;
            } else {
                LOGGER.info(String.format(
                        "%s: Received error status when polling bankId",
                        SparebankenSorConstants.LogTags.BANKID_LOG_TAG.toString()));
                return BankIdStatus.FAILED_UNKNOWN;
            }
        } else {
            LOGGER.info(String.format(
                    "%s: Unknown poll status: %s. Number of polls: %s",
                    SparebankenSorConstants.LogTags.BANKID_LOG_TAG.toString(),
                    pollStatus,
                    pollWaitCounter));
            return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private void continueActivation() throws SupplementalInfoException {
        String evryToken = finalizeBankIdAuthentication();
        executeLogin(evryToken);

        SendSmsRequest sendSmsRequest = SendSmsRequest.build(mobilenumber);
        HttpResponse response = apiClient.sendSms(sendSmsRequest);

        if (!Objects.equals(response.getStatus(), HttpStatusCodes.STATUS_CODE_OK)) {
            throw new IllegalStateException(
                    "Sparebanken Sor - Something went wrong when sending request for getting activation code via sms");
        }

        Map<String, String> activationCodeResponse = supplementalInformationHelper.askSupplementalInformation(
                getActivationCodeField());

        evryToken = encapClient.activateAndAuthenticateUser(
                activationCodeResponse.get(ACTIVATION_CODE_FIELD_KEY));
        executeLogin(evryToken);
    }

    private void executeLogin(String evryToken) {
        FirstLoginRequest firstLoginRequest = FirstLoginRequest.build(evryToken);
        FirstLoginResponse firstLoginResponse = apiClient.loginFirstStep(firstLoginRequest);

        sessionStorage.put(SparebankenSorConstants.Storage.ACCESS_TOKEN, firstLoginResponse.getAccessToken());
        // We might want to add some check on the second login response. Not doing it now since I don't know
        // what fields/values that signal an error. But if we get errors here we should add a check for it.
        apiClient.loginSecondStep();
    }

    private String finalizeBankIdAuthentication() {
        FinalizeBankIdBody finalizeBankIdBody = FinalizeBankIdBody.build();
        String finalizehBankIdResponseString = apiClient.finalizeBankId(finalizeBankIdBody);
        Document doc = Jsoup.parse(finalizehBankIdResponseString);
        return doc.getElementsByAttributeValue("name", "so").first().val();
    }

    private Field getActivationCodeField() {
        Field activationCodeValue = new Field();
        activationCodeValue.setDescription(catalog.getString("Activation code"));
        activationCodeValue.setName(ACTIVATION_CODE_FIELD_KEY);
        activationCodeValue.setNumeric(true);
        activationCodeValue.setMinLength(ACTIVATION_CODE_LENGTH);
        activationCodeValue.setMaxLength(ACTIVATION_CODE_LENGTH);
        activationCodeValue.setHint(StringUtils.repeat("N", ACTIVATION_CODE_LENGTH));
        activationCodeValue.setPattern(String.format("([0-9]{%d})", ACTIVATION_CODE_LENGTH));
        activationCodeValue.setPatternError("The activation code you entered is not valid");

        return activationCodeValue;
    }
}
