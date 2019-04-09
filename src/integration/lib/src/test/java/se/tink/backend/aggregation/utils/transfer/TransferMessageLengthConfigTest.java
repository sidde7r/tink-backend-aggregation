package se.tink.backend.aggregation.utils.transfer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TransferMessageLengthConfigTest {
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void expect() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Null thresholds are not allowed!");
    }

    @Test
    public void ensureExceptionIsThrown_whenSourceMessageMaxLength_isNull() {
        TransferMessageLengthConfig.createWithMaxLength(null, 12, 25);
    }

    @Test
    public void ensureExceptionIsThrown_whenDestinationMessageMaxLength_isNull() {
        TransferMessageLengthConfig.createWithMaxLength(25, null, 25);
    }

    @Test
    public void
            ensureExceptionIsThrown_whenDestinationMessageMaxLength_isNull_forExternalTransfer() {
        TransferMessageLengthConfig.createWithMaxLength(25, 12, null);
    }
}
