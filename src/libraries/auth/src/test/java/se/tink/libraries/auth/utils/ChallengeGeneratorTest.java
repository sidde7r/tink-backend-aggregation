package se.tink.libraries.auth.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ChallengeGeneratorTest {
    // Number of bits / 64 rounded up to nearest multiple of 4.
    private static final int LENGTH_OF_BASE64_CHALLENGE = 44;
    private static final int ENTROPY_TEST_SIZE = 100;

    private ChallengeGenerator generator = new ChallengeGenerator();

    @Test
    public void generateChallenge_correctLenght() {
        String challenge = generator.getRandomChallenge();
        assertThat(challenge.length()).isEqualTo(LENGTH_OF_BASE64_CHALLENGE);
    }
}
