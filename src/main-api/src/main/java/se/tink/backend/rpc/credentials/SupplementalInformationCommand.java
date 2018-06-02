package se.tink.backend.rpc.credentials;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import org.json.JSONObject;
import se.tink.backend.core.SupplementalStatus;

public class SupplementalInformationCommand {
    private String userId;
    private String credentialsId;
    private String supplementalInformation;

    private SupplementalInformationCommand() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getSupplementalInformation() {
        return supplementalInformation;
    }

    public final static class Builder {
        private String userId;
        private String credentialsId;
        private SupplementalStatus status;
        private String supplementalInformation;

        public Builder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withCredentialsId(String credentialsId) {
            this.credentialsId = credentialsId;
            return this;
        }

        public Builder withStatus(SupplementalStatus status) {
            this.status = status;
            return this;
        }

        public Builder withSupplementalInformation(String supplementalInformation) {
            this.supplementalInformation = supplementalInformation;
            return this;
        }

        public Builder withSupplementalInformation(Map<String, String> supplementalInformation) {
            this.supplementalInformation = new JSONObject(supplementalInformation).toString();
            return this;
        }

        /**
         * Build the supplemental command. The supplemental information field will be `null` if the status is aborted.
         */
        public SupplementalInformationCommand build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(userId), "UserId must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(credentialsId), "CredentialsId must not be null or empty.");

            SupplementalInformationCommand command = new SupplementalInformationCommand();
            command.userId = userId;
            command.credentialsId = credentialsId;

            if (Objects.equals(status, SupplementalStatus.CANCELLED)) {
                command.supplementalInformation = null;
            } else {
                command.supplementalInformation = supplementalInformation;
            }

            return command;
        }
    }

}
