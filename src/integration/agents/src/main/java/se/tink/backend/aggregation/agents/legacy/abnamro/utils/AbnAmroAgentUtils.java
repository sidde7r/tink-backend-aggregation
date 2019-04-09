package se.tink.backend.aggregation.agents.abnamro.utils;

import se.tink.backend.agents.rpc.Account;

public class AbnAmroAgentUtils {

    /** An account is subscribed if the the payload on the subscribed key is true. */
    public static boolean isSubscribed(Account account) {
        String subscribed = account.getPayload(AbnAmroUtils.InternalAccountPayloadKeys.SUBSCRIBED);

        return Boolean.parseBoolean(subscribed);
    }
}
