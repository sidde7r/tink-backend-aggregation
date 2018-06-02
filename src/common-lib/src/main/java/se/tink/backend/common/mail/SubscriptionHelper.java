package se.tink.backend.common.mail;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.core.Subscription;
import se.tink.backend.core.SubscriptionPk;
import se.tink.backend.core.SubscriptionToken;
import se.tink.backend.core.SubscriptionType;
import se.tink.backend.utils.StringUtils;

/**
 * Utility methods for checking whether a user has opted out from certain mails. Useful when sending mails.
 * <p>
 * Could have been incorporated in {@link MailSender}, however it only has static methods and this class has a dependency
 * on {@link SubscriptionRepository}.
 */
public class SubscriptionHelper {
    
    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private SubscriptionRepository subscriptionRepository;

    private SubscriptionTokenRepository subscriptionTokenRepository;

    @Inject
    public SubscriptionHelper(SubscriptionRepository subscriptionRepository, SubscriptionTokenRepository subscriptionTokenRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionTokenRepository = subscriptionTokenRepository;
    }

    private String createAndReturnTokenFor(String userId) {
        SubscriptionToken token = new SubscriptionToken();
        token.setToken(StringUtils.generateUUID());
        token.setUserId(userId);
        return subscriptionTokenRepository.save(token).getToken();
    }

    public String getOrCreateTokenFor(String userId) {
        List<SubscriptionToken> tokens = subscriptionTokenRepository.findAllByUserId(userId);

        // XXX: Minor race condition if two callers would call this concurrently, we could potentially create duplicate
        // tokens for a single user.
        if (!tokens.isEmpty()) {
            return tokens.get(0).getToken();
        } else {
            return createAndReturnTokenFor(userId);
        }
    }

    public boolean subscribesTo(String userId, SubscriptionType type) {
        Subscription possibleMatch = subscriptionRepository.findOne(new SubscriptionPk(userId, type));
        if (possibleMatch != null) {
            return possibleMatch.isSubscribed();
        }
        
        return type.isSubscribedByDefault();
    }
}
