package se.tink.backend.aggregation.agents.abnamro.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorEntity {
    private static final ImmutableMap<String, String> ERROR_REASONS_BY_KEY =
            ImmutableMap.<String, String>builder()

                    // Descriptions copied from ABN AMRO documentation
                    .put(
                            "MESSAGE_BAI538_0001",
                            "Functional Error from ICS – customer /account not found")
                    .put("MESSAGE_BAI538_0002", "Functional Error from ICS – No Approval available")
                    .put(
                            "MESSAGE_BAI538_0003",
                            "Functional Error from ICS – Service not available for this account level")
                    .put("MESSAGE_BAI538_0004", "Technical Error from ICS")
                    .put("MESSAGE_BAI538_0050", "Input contract number list is empty or missing")
                    .put(
                            "MESSAGE_BAI538_0051",
                            "Input contract number list is present but contains empty contract number")
                    .put(
                            "MESSAGE_BAI538_0053",
                            "Input contract number not configured with Msec, hence the user is not allowed to access the contract")
                    .put(
                            "MESSAGE_BAI538_0054",
                            "The user not authorized for credit cards, result based on Msec")
                    .put(
                            "MESSAGE_BAI538_0055",
                            "Technical Exception caused due to inconsistent data")
                    .put("MESSAGE_BAI538_0056", "Input contract number invalid")
                    .put("MESSAGE_BAI538_0057", "Technical Error caused by configuration")
                    .put(
                            "MESSAGE_BAI538_0058",
                            "Credit card functionality disabled by the maintenance using the configurable flag")
                    .put(
                            "MESSAGE_BAI538_0059",
                            "Technical Error while interaction with backend transactions")

                    // Descriptions not available in documentation
                    .put(
                            "MESSAGE_BAI538_0032",
                            "Temporary problem with the communication between ABN AMRO And ICS")

                    // Error that is returned if the authorization token is invalid
                    .put("MESSAGE_BAI538_0061", "Invalid authorization token")

                    // Errors that are related to enrollment/signing
                    .put("MESSAGE_BAI680_0002", "Phone number is null or empty")
                    .put("MESSAGE_BAI680_0001", "Entry does not exist")
                    .put("MESSAGE_BAI680_0003", "Technical error")
                    .put("MESSAGE_BAI680_0004", "Technical error")
                    .put("MESSAGE_BAI680_0005", "Technical error")
                    .build();

    /**
     * Errors that are related to a temporary error at ABN ICS and that we can retry directly (for
     * example a unstable VPN connection)
     */
    public static final ImmutableSet<String> RETRYABLE_ERROR_KEYS =
            ImmutableSet.of("MESSAGE_BAI538_0032");

    /**
     * Errors that are related to that the user needs to give an "approval" in some way for being
     * able to use ICS Credit Cards.
     */
    public static final ImmutableSet<String> APPROVAL_ERRORS =
            ImmutableSet.of("MESSAGE_BAI538_0002");

    private String messageKey;
    private String messageType;
    private String messageText;

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getReason() {
        if (messageKey == null) {
            return null;
        }
        return ERROR_REASONS_BY_KEY.get(messageKey);
    }

    public boolean isRetryable() {
        return messageKey != null && RETRYABLE_ERROR_KEYS.contains(messageKey);
    }
}
