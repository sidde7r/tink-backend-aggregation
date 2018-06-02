package se.tink.libraries.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;
import com.yubico.client.v2.exceptions.YubicoValidationException;
import com.yubico.client.v2.exceptions.YubicoValidationFailure;
import java.util.Collection;
import se.tink.libraries.log.LogUtils;

public class YubicoAuthorizationHeaderPredicate implements Predicate<String> {

    private AuthorizationHeaderPredicate delegate;

    public YubicoAuthorizationHeaderPredicate(int yubicoClientId, Collection<String> yubikeyIds) {
        delegate = new AuthorizationHeaderPredicate("yubikey", new YubikeyPredicate(yubicoClientId,
                ImmutableSet.copyOf(yubikeyIds)));
    }

    @Override
    public boolean apply(String authorizationHeader) {
        return delegate.apply(authorizationHeader);
    }

}

class YubikeyPredicate implements Predicate<String> {

    private static final LogUtils log = new LogUtils(YubikeyPredicate.class);

    private final ImmutableSet<String> keyIds;
    private final int yubicoClientId;

    public YubikeyPredicate(final int yubicoClientId, Collection<String> keyIds) {
        for (String keyId : keyIds) {
            Preconditions.checkArgument(keyId.length() == 12, "Incorrect Yubikey ID length: " + keyId);
        }

        this.keyIds = ImmutableSet.copyOf(keyIds);
        this.yubicoClientId = yubicoClientId;
    }

    @Override
    public boolean apply(String otp) {
        boolean outcome = evaluteAuthentication(otp);
        if (outcome) {
            log.info("Succesful authentication using Yubikey: " + otp);
        } else {
            log.error("Unsuccesful authentication using Yubikey: " + otp);
        }
        return outcome;
    }

    private boolean evaluteAuthentication(String otp) {
        if (otp == null) {
            return false;
        }

        String keyId = otp.substring(0, 12);
        boolean approvedKey = keyIds.contains(keyId);

        if (!approvedKey) {
            return false;
        }

        try {
            return validateOTP(otp, yubicoClientId);
        } catch (YubicoValidationException | YubicoValidationFailure e) {
            log.warn("Could not authenticate OTP.", e);
            return false;
        }
    }

    private boolean validateOTP(String otp, int clientId) throws YubicoValidationException, YubicoValidationFailure {
        YubicoClient client = YubicoClient.getClient(clientId);

        YubicoResponse response = client.verify(otp);

        if (response != null && response.getStatus() == YubicoResponseStatus.OK) {
            return true;
        }

        return false;
    }

}
