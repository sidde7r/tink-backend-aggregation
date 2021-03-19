package se.tink.backend.aggregation.workers.commands;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DebugDecryptSpecialCharactersWorkerCommand extends AgentWorkerCommand {

    private static final Logger log =
            LoggerFactory.getLogger(DebugDecryptSpecialCharactersWorkerCommand.class);

    private final AgentWorkerCommandContext context;
    private final CredentialsCrypto credentialsCrypto;

    public DebugDecryptSpecialCharactersWorkerCommand(
            AgentWorkerCommandContext context, CredentialsCrypto credentialsCrypto) {
        this.context = context;
        this.credentialsCrypto = credentialsCrypto;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            CredentialsRequest request = context.getRequest();
            String credentialsId = context.getRequest().getCredentials().getId();
            Credentials credentialsNonUTF = request.getCredentials().clone();
            Credentials credentialsUTF8 = request.getCredentials().clone();

            int nonUTFHash = getHash(credentialsNonUTF, null);
            int utf8Hash = getHash(credentialsUTF8, StandardCharsets.UTF_8);

            if (nonUTFHash != utf8Hash) {
                log.warn(
                        "[decrypt] credentialsId {} nonUTFHash is not equal to utf8Hash",
                        credentialsId);
            }

        } catch (Exception e) {
            log.warn("Error with debugging", e);
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    private int getHash(final Credentials credentials, final Charset charset) {
        credentialsCrypto.decrypt(credentials, charset);
        String msg = charset != null ? charset.name() : "NonUTF";
        infoLog(msg, credentials);
        return getHashCode(credentials.getFieldsSerialized());
    }

    private void infoLog(final String msg, final Credentials credentials) {
        int fieldsSerializedHascode = getHashCode(credentials.getFieldsSerialized());

        log.info(
                "[decrypt][{}] credentialsId {} fieldSeralized hashcode {}",
                msg,
                credentials.getId(),
                fieldsSerializedHascode);
        credentials
                .getFields()
                .forEach(
                        (k, v) ->
                                log.info(
                                        "[decrypt][{}] key: {} value hashcode: {}",
                                        msg,
                                        k,
                                        v.hashCode()));
    }

    private int getHashCode(final String fieldsSerialized) {
        if (fieldsSerialized == null) {
            return 0;
        }
        return fieldsSerialized.hashCode();
    }

    @Override
    protected void doPostProcess() throws Exception {}
}
