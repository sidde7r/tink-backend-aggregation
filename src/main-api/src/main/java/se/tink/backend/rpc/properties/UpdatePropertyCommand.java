package se.tink.backend.rpc.properties;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class UpdatePropertyCommand {
    private String userId;
    private String propertyId;
    private int numberOfRooms;
    private int numberOfSquareMeters;

    private UpdatePropertyCommand() {
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public int getNumberOfSquareMeters() {
        return numberOfSquareMeters;
    }

    public String getUserId() {
        return userId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String userId;
        private String propertyId;
        private int numberOfRooms;
        private int numberOfSquareMeters;

        public Builder withUser(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder withPropertyId(String propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public Builder withNumberOfRooms(int numberOfRooms) {
            this.numberOfRooms = numberOfRooms;
            return this;
        }

        public Builder withNumberOfSquareMeters(int numberOfSquareMeters) {
            this.numberOfSquareMeters = numberOfSquareMeters;
            return this;
        }

        public UpdatePropertyCommand build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(userId), "UserId must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(propertyId), "PropertyId must not be null or empty.");

            UpdatePropertyCommand command = new UpdatePropertyCommand();
            command.userId = userId;
            command.propertyId = propertyId;
            command.numberOfRooms = numberOfRooms;
            command.numberOfSquareMeters = numberOfSquareMeters;

            return command;
        }
    }
}
