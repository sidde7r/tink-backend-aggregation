package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSubscriptionResponse extends ErrorResponse {

    public static class ErrorCodes {
        public static final String ALREADY_ACTIVE_SUBSCRIPTION = "MESSAGE_BAI578_0010";
        public static final String NON_RETAIL_CUSTOMER = "MESSAGE_BAI578_0004";
        public static final String UNDER_AGE_16 = "MESSAGE_BAI578_0016";

        /**
         * Token isn't allowed at ABN AMRO. Either we are sending the wrong token or ABN AMRO has
         * the wrong token configured.
         */
        public static final String INVALID_AUTHORIZATION_TOKEN = "MESSAGE_BAI578_0013";

        /**
         * User wasn't authorised to create a subscription. This would typically happen if an old
         * pilot user is trying to sign-up and he/she isn't allowed any longer due to different
         * logic after full market release.
         */
        public static final String INVALID_USER_AUTHORIZATION = "MESSAGE_BAI578_0017";

        /** Means that the it was a technical failure in the subscription process */
        public static final String TECHNICAL_FAILURE = "MESSAGE_BAI578_0007";
    }

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return id != null;
    }

    public boolean isCustomerAlreadySubscribed() {
        return getErrorByKey(ErrorCodes.ALREADY_ACTIVE_SUBSCRIPTION).isPresent();
    }

    public boolean isNonRetailCustomer() {
        return getErrorByKey(ErrorCodes.NON_RETAIL_CUSTOMER).isPresent();
    }

    public boolean isUnderAge16() {
        return getErrorByKey(ErrorCodes.UNDER_AGE_16).isPresent();
    }

    public Optional<String> getReason() {
        if (CollectionUtils.isEmpty(getMessages())) {
            return Optional.empty();
        }

        switch (getMessages().get(0).getMessageKey()) {
            case ErrorCodes.ALREADY_ACTIVE_SUBSCRIPTION:
                return Optional.of("A subscription is already active");
            case ErrorCodes.NON_RETAIL_CUSTOMER:
                return Optional.of("Customer is a non retail");
            case ErrorCodes.UNDER_AGE_16:
                return Optional.of("Customer is below 16 years old");
            case ErrorCodes.INVALID_AUTHORIZATION_TOKEN:
                return Optional.of("Invalid authorization token");
            case ErrorCodes.INVALID_USER_AUTHORIZATION:
                return Optional.of("Invalid user authorization");
            case ErrorCodes.TECHNICAL_FAILURE:
                return Optional.of("Technical failure in subscription process");
            default:
                return Optional.empty();
        }
    }
}
