package se.tink.backend.aggregation.agents.banks.uk.barclays;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.util.concurrent.Uninterruptibles;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.banks.uk.barclays.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.banks.uk.barclays.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.PinSentryInitResponse;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.userregistration.UserInformationResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BarclaysAgent extends AbstractAgent implements RefreshableItemExecutor {
    private final Credentials credentials;
    private final BarclaysApiClient apiClient;
    /* temporary */ private final String deviceIdentifier;

    private List<AccountEntity> accounts = null;

    static { Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); }

    public BarclaysAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        credentials = request.getCredentials();
        deviceIdentifier = credentials.getField("phoneNumber");
        TinkApacheHttpClient4 client = clientFactory.createCustomClient(context.getLogOutputStream());
        apiClient = new BarclaysApiClient(credentials.getUserId(), client, deviceIdentifier);
    }

    // todo: Move this to a more general place
    private Map<String, String> askSupplementalInformation(Field... fields) {
        CredentialsStatus oldStatus = credentials.getStatus();
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));
        String supplementalInformation = supplementalRequester.requestSupplementalInformation(credentials);
        if (supplementalInformation == null) {
            // maybe throw an exception instead
            return null;
        }

        // we must reset the credential status in order to request supplemental info multiple times
        credentials.setStatus(oldStatus);
        statusUpdater.updateStatus(oldStatus);

        // updateStatus is an asynchronous call, must wait a bit
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);

        return SerializationUtils.deserializeFromString(
                                        supplementalInformation,
                                        new TypeReference<HashMap<String, String>>() {});
    }

    private void storeIdentity(BarclaysIdentity identity) {
        Map<String, String> sIdentity = identity.save();
        credentials.addSensitivePayload(sIdentity);
    }

    private BarclaysIdentity loadIdentity() {
        BarclaysIdentity identity = new BarclaysIdentity(credentials.getUserId(), deviceIdentifier);
        identity.load(credentials.getSensitivePayload());
        return identity;
    }

    private boolean hasStoredIdentity() {
        return !credentials.getSensitivePayload().isEmpty();
    }


    @Override
    public boolean login() throws Exception {
        if (hasStoredIdentity()) {
            loginUser();
        } else {
            registerUser();
        }

        return true;
    }

    @Override
    public void logout() throws Exception {
        // NYI
    }

    private List<AccountEntity> getAccounts() {
        if (accounts != null) {
            return accounts;
        }

        accounts = apiClient.fetchAccounts();
        return accounts;
    }

    @Override
    public void refresh(RefreshableItem item) {
        switch (item) {
        case CHECKING_ACCOUNTS:
            List<AccountEntity> accounts = getAccounts();
            financialDataCacher.cacheAccounts(accounts.stream()
                    .filter(AccountEntity::isAccountPersonal)
                    .map(AccountEntity::toTinkAccount)
                    .collect(Collectors.toList()));
            break;
        case CHECKING_TRANSACTIONS:
            for (AccountEntity account : getAccounts()) {
                if (!account.isAccountPersonal()) {
                    continue;
                }
                List<TransactionEntity> transactions = apiClient.fetchTransactions(account.getProductIdentifier());

                financialDataCacher.updateTransactions(
                        account.toTinkAccount(),
                        transactions.stream()
                                .map(TransactionEntity::toTinkTransaction)
                                .collect(Collectors.toList()));
            }
            break;
        }
    }

    private void loginUser() {
        BarclaysIdentity identity = loadIdentity();
        BarclaysSession session = apiClient.authenticateDevice(identity);
        apiClient.setSession(session);
        apiClient.queryConfiguration();
    }

    private void registerUser() throws AuthenticationException {
        BarclaysIdentity identity = apiClient.registerDevice();
        BarclaysSession session = apiClient.authenticateDevice(identity);
        apiClient.setSession(session);


        // These fields SHOULD be in the providers-uk.json, but due to limitations* in the tink app they must
        // be queried here instead.
        // *: You cannot scroll in the supplemental information view and the keyboard takes up half the screen,
        // this results in that a user cannot read or enter data into the bottom most fields.
        Field accountNumberField = new Field();
        accountNumberField.setMasked(false);
        accountNumberField.setDescription("Account number");
        accountNumberField.setName("accountNumber");
        accountNumberField.setHelpText("Account number as it is written on your debit card.");
        accountNumberField.setNumeric(true);
        accountNumberField.setMinLength(6);
        accountNumberField.setMaxLength(12);
        accountNumberField.setHint("NNNNNNNN");
        accountNumberField.setOptional(false);

        Field sortCodeField = new Field();
        sortCodeField.setMasked(false);
        sortCodeField.setDescription("Sort code");
        sortCodeField.setName("sortCode");
        sortCodeField.setHelpText("Sort code as it is written on your debit card.");
        sortCodeField.setNumeric(true);
        sortCodeField.setMinLength(6);
        sortCodeField.setMaxLength(6);
        sortCodeField.setHint("NNNNNN");
        sortCodeField.setOptional(false);

        Map<String, String> supplementalInformation = askSupplementalInformation(sortCodeField, accountNumberField);
        if (supplementalInformation == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        // Send user/account information
        UserInformationResponse userInformationResponse = apiClient.submitUserInformation(
                                                                        credentials.getField("firstName"),
                                                                        credentials.getField("lastName"),
                                                                        supplementalInformation.get("sortCode"),
                                                                        supplementalInformation.get("accountNumber"),
                                                                        credentials.getField("phoneNumber"));
        // the server gave us a new aid (final aid)
        identity.setaId(userInformationResponse.getAid());

        // SMS challenge
        apiClient.initiateSmsChallenge();

        Field smsVerificationField = new Field();
        smsVerificationField.setMasked(false);
        smsVerificationField.setDescription("SMS verification code");
        smsVerificationField.setName("smsVerificationCode");
        smsVerificationField.setHelpText("The verification code sent by SMS to the telephone number specified in the previous step.");
        smsVerificationField.setNumeric(true);
        smsVerificationField.setMinLength(6);
        smsVerificationField.setMaxLength(6);

        supplementalInformation = askSupplementalInformation(smsVerificationField);
        if (supplementalInformation == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        // Respond with SMS response code
        apiClient.respondSmsChallenge(supplementalInformation.get("smsVerificationCode"));


        // We must query the configuration at this stage in order to be able to send commands later on
        apiClient.queryConfiguration();


        // PinSentry challenge
        PinSentryInitResponse pinSentryInitResponse = apiClient.initiatePinSentryChallenge();

        Field cardLastFourDigitsField = new Field();
        cardLastFourDigitsField.setMasked(false);
        cardLastFourDigitsField.setDescription("Last four digits of your Barclays card number.");
        cardLastFourDigitsField.setName("cardLastFourDigits");
        cardLastFourDigitsField.setHelpText("The credit/debit card you use in your PinSentry reader.");
        cardLastFourDigitsField.setNumeric(true);
        cardLastFourDigitsField.setMinLength(4);
        cardLastFourDigitsField.setMaxLength(4);

        Field pinSentryChallenge = new Field();
        pinSentryChallenge.setMasked(false);
        pinSentryChallenge.setDescription("The PinSentry challenge.");
        pinSentryChallenge.setName("pinSentryChallenge");
        pinSentryChallenge.setHelpText("Enter this number into your PinSentry reader.");
        pinSentryChallenge.setValue(pinSentryInitResponse.getPinSentryChallenge());
        pinSentryChallenge.setImmutable(true);

        Field pinSentryResponse = new Field();
        pinSentryResponse.setMasked(false);
        pinSentryResponse.setDescription("The PinSentry response.");
        pinSentryResponse.setName("pinSentryResponse");
        pinSentryResponse.setHelpText("The response code from your PinSentry reader.");
        pinSentryResponse.setNumeric(true);
        pinSentryResponse.setMinLength(8);
        pinSentryResponse.setMaxLength(8);

        supplementalInformation = askSupplementalInformation(cardLastFourDigitsField, pinSentryChallenge, pinSentryResponse);
        if (supplementalInformation == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        apiClient.respondPinSentryChallenge(
                            supplementalInformation.get("cardLastFourDigits"),
                            supplementalInformation.get("pinSentryResponse"));

        storeIdentity(identity);
    }
}
