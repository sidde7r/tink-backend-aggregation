package se.tink.backend.common.product.targeting;

import com.google.common.collect.ListMultimap;
import java.util.List;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.User;

public class Profile {
    final User user;
    final ListMultimap<String, Credentials> credentialsByProviderName;
    final ListMultimap<FraudDetailsContentType, FraudDetails> fraudDetailsByType;

    public Profile(User user, ListMultimap<String, Credentials> credentialsByProviderName,
            ListMultimap<FraudDetailsContentType, FraudDetails> fraudDetailsByType) {

        this.user = user;
        this.credentialsByProviderName = credentialsByProviderName;
        this.fraudDetailsByType = fraudDetailsByType;
    }

    public ListMultimap<String, Credentials> getCredentials() {
        return credentialsByProviderName;
    }

    public ListMultimap<FraudDetailsContentType, FraudDetails> getFraudDetails() {
        return fraudDetailsByType;
    }

    public List<FraudDetails> getFraudDetails(FraudDetailsContentType type) {
        return fraudDetailsByType.get(type);
    }

    public User getUser() {
        return user;
    }
}
