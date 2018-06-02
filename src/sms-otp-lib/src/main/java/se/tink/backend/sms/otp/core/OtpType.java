package se.tink.backend.sms.otp.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.security.SecureRandom;
import java.util.List;

public enum OtpType {
    NUMERIC,
    ALPHA;

    private static SecureRandom random = new SecureRandom();

    // The alphabet without vowels
    private final static List<Character> ALPHA_CHARACTER_LIST = ImmutableList.of('B', 'C', 'D', 'F', 'G', 'H',
            'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z');
    // Arabic digits
    private final static List<Character> NUMERIC_CHARACTER_LIST = ImmutableList.of('0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9');

    public Character getRandom() {
        List<Character> characterList;
        switch (this) {
        case ALPHA:
            characterList = ALPHA_CHARACTER_LIST;
            break;
        case NUMERIC:
        default:
            characterList = NUMERIC_CHARACTER_LIST;
            break;
        }

        return characterList.get(random.nextInt(characterList.size()));
    }
}
