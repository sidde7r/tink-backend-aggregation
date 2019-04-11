package se.tink.backend.aggregation.agents.nxgen.demo.banks.password.executor.transfer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;

public class PasswordDemoTransferExecutor implements BankTransferExecutor {
    private final Credentials credentials;
    private final SupplementalRequester supplementalRequester;

    public PasswordDemoTransferExecutor(
            Credentials credentials, SupplementalRequester supplementalRequester) {
        this.credentials = credentials;
        this.supplementalRequester = supplementalRequester;
    }

    private static List<Field> createChallengeAndResponse(String code) {
        Field challengeField = new Field();

        challengeField.setImmutable(true);
        challengeField.setDescription("Code");
        challengeField.setValue("Code");
        challengeField.setName("code");
        challengeField.setHelpText("The code is: " + code);

        Field responseField = new Field();

        responseField.setDescription("Response code");
        responseField.setName("response");
        responseField.setNumeric(false);
        responseField.setHint("NNNNN");
        responseField.setMaxLength(code.length());
        responseField.setMinLength(code.length());
        responseField.setPattern("([a-zA-Z0-9]{" + code.length() + "})");

        return Lists.newArrayList(challengeField, responseField);
    }

    @Override
    public Optional<String> executeTransfer(Transfer transfer) throws TransferExecutionException {
        System.out.println("username: " + credentials.getField("username"));
        if (!Objects.equal(credentials.getField("username"), "201212121212")) {
            String response = requestChallengeResponse(credentials, "code1");
            if (Strings.isNullOrEmpty(response) || !response.equals("code1")) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage("Code no. 1 was invalid")
                        .build();
            }

            response = requestChallengeResponse(credentials, "code2");
            if (Strings.isNullOrEmpty(response) || !response.equals("code2")) {
                throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                        .setEndUserMessage("Code no. 2 was invalid")
                        .build();
            }
        }
        return Optional.empty();
    }

    private String requestChallengeResponse(Credentials credentials, String code) {
        List<Field> fields = createChallengeAndResponse(code);

        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        credentials.setSupplementalInformation(SerializationUtils.serializeToString(fields));

        String supplementalInformation =
                supplementalRequester.requestSupplementalInformation(credentials, true);

        if (Strings.isNullOrEmpty(supplementalInformation)) {
            return null;
        }

        Map<String, String> answers =
                SerializationUtils.deserializeFromString(
                        supplementalInformation, new TypeReference<HashMap<String, String>>() {});

        return answers.get("response");
    }
}
