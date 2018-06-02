package se.tink.backend.consent.controllers;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.List;
import se.tink.backend.consent.config.ConsentConfiguration;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.User;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.core.exceptions.ConsentNotFoundException;
import se.tink.backend.consent.core.exceptions.ConsentRequestInvalid;
import se.tink.backend.consent.core.exceptions.InvalidChecksumException;
import se.tink.backend.consent.core.exceptions.UserConsentNotFoundException;
import se.tink.backend.consent.dao.ConsentDAO;
import se.tink.backend.consent.rpc.ConsentRequest;
import se.tink.libraries.cryptography.RSAUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class ConsentServiceController {
    private final ConsentDAO consentDAO;
    private final Signature signature;

    @Inject
    public ConsentServiceController(ConsentDAO consentDAO, ConsentConfiguration consentConfiguration) {
        this.consentDAO = consentDAO;

        if (consentConfiguration.getSigningKeyPath().isPresent()) {
            try {
                PrivateKey privateKey = RSAUtils.getPrivateKey(consentConfiguration.getSigningKeyPath().get());
                this.signature = RSAUtils.getSignature(privateKey);
            } catch (Exception e) {
                throw new RuntimeException("Could not create signature for consent signing.", e);
            }
        } else {
            this.signature = null;
        }
    }

    public List<Consent> available(User user) {
        user.validate();
        return consentDAO.findAllAvailableByUserIdAndLocale(user.getId(), user.getLocale());
    }

    public List<UserConsent> list(User user) {
        user.validate();
        return consentDAO.findLatestByUserId(user.getId());
    }

    public UserConsent consent(User user, ConsentRequest request) throws ConsentNotFoundException,
            InvalidChecksumException, ConsentRequestInvalid {
        user.validate();
        request.validate();

        try {
            return consentDAO.saveUserConsent(user, request, signature);
        } catch (SignatureException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UserConsent details(User user, String id) throws UserConsentNotFoundException {
        Preconditions.checkState(UUIDUtils.isValidTinkUUID(id), "Id must be a valid UUID.");
        user.validate();

        return consentDAO.findByUserIdAndId(user.getId(), id);
    }

    public Consent describe(User user, String key) throws ConsentNotFoundException {
        return consentDAO.getLatestByKeyAndLocale(key, user.getLocale());
    }
}
