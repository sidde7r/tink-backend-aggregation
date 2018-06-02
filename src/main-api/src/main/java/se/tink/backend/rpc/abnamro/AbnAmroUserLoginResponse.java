package se.tink.backend.rpc.abnamro;

import io.protostuff.Tag;
import java.util.List;
import se.tink.backend.core.AbnAmroSubscription;
import se.tink.backend.core.UserContext;
import se.tink.backend.rpc.UserLoginResponse;

/**
 * Response that holds the normal data from UserLoginResponse but also abn specific subscription information.
 */
public class AbnAmroUserLoginResponse {
    @Tag(1)
    protected UserContext context;
    @Tag(2)
    protected String sessionId;
    @Tag(3)
    protected AbnAmroSubscription subscription;
    @Tag(4)
    protected List<Long> notSubscribedContracts;

    public AbnAmroUserLoginResponse(UserLoginResponse loginResponse) {
        this.context = loginResponse.getContext();
        this.sessionId = loginResponse.getSessionId();
    }

    public UserContext getContext() {
        return context;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setContext(UserContext context) {
        this.context = context;
    }

    public AbnAmroSubscription getSubscription() {
        return subscription;
    }

    public void setSubscription(AbnAmroSubscription subscription) {
        this.subscription = subscription;
    }

    public List<Long> getUnsubscribedContracts() {
        return notSubscribedContracts;
    }

    public void setNotSubscribedContracts(List<Long> notSubscribedContracts) {
        this.notSubscribedContracts = notSubscribedContracts;
    }

}
