package se.tink.backend.rpc;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import se.tink.libraries.validation.validators.Pin6Validator;

public class UpdatePin6Command {
    private String oldPin6;
    private String newPin6;
    private String sessionId;
    private Optional<String> remoteAddress;

    public String getSessionId() {
        return sessionId;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public String getOldPin6() {
        return oldPin6;
    }

    public String getNewPin6() {
        return newPin6;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String oldPin6;
        private String newPin6;
        private String sessionId;
        private Optional<String> remoteAddress;

        public Builder withOldPin6(String oldPin6) {
            this.oldPin6 = oldPin6;
            return this;
        }

        public Builder withNewPin6(String newPin6) {
            this.newPin6 = newPin6;
            return this;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withRemoteAddress(Optional<String> remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public UpdatePin6Command build() throws InvalidPin6Exception {
            Pin6Validator.validateChange(oldPin6, newPin6);

            UpdatePin6Command command = new UpdatePin6Command();
            command.oldPin6 = oldPin6;
            command.newPin6 = newPin6;
            command.sessionId = sessionId;
            command.remoteAddress = remoteAddress;

            return command;
        }
    }
}
