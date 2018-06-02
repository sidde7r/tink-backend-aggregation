package se.tink.backend.common.dao;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import se.tink.backend.common.repository.mysql.main.BankIdAuthenticationRepository;
import se.tink.backend.core.auth.bankid.BankIdAuthentication;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;
import se.tink.backend.core.exceptions.BankIdAuthenticationExpiredException;
import se.tink.backend.core.exceptions.BankIdAuthenticationNotFoundException;

public class BankIdAuthenticationDao {
    private static final Seconds AUTHENTICATION_TIME_TO_LIVE = Minutes.minutes(5).toStandardSeconds();

    private BankIdAuthenticationRepository bankIdAuthenticationRepository;

    @Inject
    public BankIdAuthenticationDao(BankIdAuthenticationRepository bankIdAuthenticationRepository) {
        this.bankIdAuthenticationRepository = bankIdAuthenticationRepository;
    }

    public BankIdAuthentication save(BankIdAuthentication authentication) {
        return bankIdAuthenticationRepository.save(authentication);
    }

    public BankIdAuthenticationStatus getStatus(String authenticationToken)
            throws BankIdAuthenticationNotFoundException, BankIdAuthenticationExpiredException {
        return findOne(authenticationToken).getStatus();
    }

    public BankIdAuthentication consume(String authenticationToken) throws BankIdAuthenticationNotFoundException,
            BankIdAuthenticationExpiredException {

        BankIdAuthentication authentication = findOne(authenticationToken);

        bankIdAuthenticationRepository.delete(authenticationToken);

        return authentication;
    }

    private BankIdAuthentication findOne(String authenticationToken) throws BankIdAuthenticationNotFoundException,
            BankIdAuthenticationExpiredException {
        BankIdAuthentication authentication = bankIdAuthenticationRepository.findOne(authenticationToken);

        if (authentication == null) {
            throw new BankIdAuthenticationNotFoundException();
        }

        Seconds timeSinceUpdated = Seconds.secondsBetween(new DateTime(authentication.getUpdated()), DateTime.now());

        if (timeSinceUpdated.isGreaterThan(AUTHENTICATION_TIME_TO_LIVE)) {
            bankIdAuthenticationRepository.delete(authentication);
            throw new BankIdAuthenticationExpiredException();
        }

        return authentication;
    }

    /**
     * Delete all the mobile bankid authentications that have expired.
     */
    public int deleteExpired() {
        return bankIdAuthenticationRepository.deleteExpiredTokens(2 * AUTHENTICATION_TIME_TO_LIVE.getSeconds());
    }
}
