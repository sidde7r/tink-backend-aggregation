package se.tink.libraries.account.identifiers;

import org.junit.Assert;
import org.junit.Test;

public class NorwegianIdentifierTest {

    private static final String SHORT_NUMBER = "020564396";
    private static final String LONG_NUMBER = "0205643960000";
    private static final String VALID_NUMBER = "02056439652";
    private static final String INVALID_CHECK_NUMBER = "02056439653";
    private static final String SKIP_CHECK_NUMBER = "02050039653";
    private static final String WITH_ALPHAS = "A025003c965";

    @Test
    public void ensureValidNumber_passes() {
        NorwegianIdentifier id = new NorwegianIdentifier(VALID_NUMBER);
        Assert.assertTrue(id.isValid());
    }

    @Test
    public void ensureNumber_tooShort_fails() {
        NorwegianIdentifier id = new NorwegianIdentifier(SHORT_NUMBER);
        Assert.assertTrue(!id.isValid());
    }

    @Test
    public void ensureNumber_tooLong_fails() {
        NorwegianIdentifier id = new NorwegianIdentifier(LONG_NUMBER);
        Assert.assertTrue(!id.isValid());
    }

    @Test
    public void ensureNumber_withInvalidCheckDigit_fails() {
        NorwegianIdentifier id = new NorwegianIdentifier(INVALID_CHECK_NUMBER);
        Assert.assertTrue(!id.isValid());
    }

    @Test
    public void ensureNumber_withSkipCheckSignatureButInvaldidCheckDigit_passes() {
        NorwegianIdentifier id = new NorwegianIdentifier(SKIP_CHECK_NUMBER);
        Assert.assertTrue(id.isValid());
    }

    @Test
    public void ensureNumber_withAlphas_fails() {
        NorwegianIdentifier id = new NorwegianIdentifier(WITH_ALPHAS);
        Assert.assertTrue(!id.isValid());
    }

    @Test
    public void ensureNumber_empty_fails() {
        NorwegianIdentifier id = new NorwegianIdentifier("");
        Assert.assertTrue(!id.isValid());
    }

    @Test
    public void ensureNumber_null_fails() {
        NorwegianIdentifier id = new NorwegianIdentifier(null);
        Assert.assertTrue(!id.isValid());
    }
}
