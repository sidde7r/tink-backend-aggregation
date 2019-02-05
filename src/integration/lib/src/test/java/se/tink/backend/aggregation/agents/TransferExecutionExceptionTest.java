package se.tink.backend.aggregation.agents;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@RunWith(Enclosed.class)
public class TransferExecutionExceptionTest {
    public static class Builder {
        private static final String TEST_EXCEPTION_MESSAGE = "Test exception message.";
        private static final String TEST_USER_MESSAGE = "Test user message.";

        @Test
        public void testBasicConstruction() {
            TransferExecutionException e = TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .build();

            Assert.assertEquals(SignableOperationStatuses.CANCELLED, e.getSignableOperationStatus());
            Assert.assertNull(e.getMessage());
            Assert.assertNull(e.getUserMessage());
            Assert.assertNull(e.getCause());
        }

        @Test
        public void testConstructionWithMessage() {
            TransferExecutionException e = TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(TEST_EXCEPTION_MESSAGE).build();

            Assert.assertEquals(SignableOperationStatuses.FAILED, e.getSignableOperationStatus());
            Assert.assertEquals(TEST_EXCEPTION_MESSAGE, e.getMessage());
            Assert.assertNull(e.getUserMessage());
            Assert.assertNull(e.getCause());
        }

        @Test
        public void testConstructionWithMessageAndException() {
            RuntimeException cause = new RuntimeException();
            TransferExecutionException e = TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setException(cause)
                    .setMessage(TEST_EXCEPTION_MESSAGE).build();

            Assert.assertEquals(SignableOperationStatuses.FAILED, e.getSignableOperationStatus());
            Assert.assertEquals(TEST_EXCEPTION_MESSAGE, e.getMessage());
            Assert.assertNull(e.getUserMessage());
            Assert.assertEquals(cause, e.getCause());
        }

        @Test
        public void testConstructionWithMessageAndExceptionAndUserMessage() {
            RuntimeException cause = new RuntimeException();
            TransferExecutionException e = TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setException(cause)
                    .setEndUserMessage(TEST_USER_MESSAGE)
                    .setMessage(TEST_EXCEPTION_MESSAGE).build();

            Assert.assertEquals(SignableOperationStatuses.FAILED, e.getSignableOperationStatus());
            Assert.assertEquals(TEST_EXCEPTION_MESSAGE, e.getMessage());
            Assert.assertEquals(TEST_USER_MESSAGE, e.getUserMessage());
            Assert.assertEquals(cause, e.getCause());
        }

        @Test
        public void testConstructionExceptionAndUserMessage() {
            RuntimeException cause = new RuntimeException();
            TransferExecutionException e = TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setException(cause).setEndUserMessage(TEST_USER_MESSAGE).build();

            Assert.assertEquals(SignableOperationStatuses.FAILED, e.getSignableOperationStatus());
            Assert.assertEquals(cause.getClass().getCanonicalName(), e.getMessage());
            Assert.assertEquals(TEST_USER_MESSAGE, e.getUserMessage());
            Assert.assertEquals(cause, e.getCause());
        }

        @Test
        public void testConstructionException() {
            RuntimeException cause = new RuntimeException();
            TransferExecutionException e = TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setException(cause).build();

            Assert.assertEquals(SignableOperationStatuses.FAILED, e.getSignableOperationStatus());
            Assert.assertEquals(cause.getClass().getCanonicalName(), e.getMessage());
            Assert.assertNull(e.getUserMessage());
            Assert.assertEquals(cause, e.getCause());
        }
    }
}
