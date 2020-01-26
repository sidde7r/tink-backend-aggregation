package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class HttpResponseExceptionRequestRepeaterTest {

    @Test
    public void shouldReturnResponseAfterRepeatingRequest() {
        // given
        final String expectedResponse = "Response";
        HttpResponseExceptionRequestRepeater<String> objectUnderTest =
                new TestHttpResponseExceptionRequestRepeater(3, expectedResponse);
        // when
        String result = objectUnderTest.execute();
        // then
        Assert.assertEquals(expectedResponse, result);
    }

    @Test
    public void shouldReturnCorrectResponseWithNoRepeating() {
        // given
        final String expectedResponse = "Response";
        HttpResponseExceptionRequestRepeater<String> objectUnderTest =
                new TestHttpResponseExceptionRequestRepeater(0, expectedResponse);
        // when
        String result = objectUnderTest.execute();
        // then
        Assert.assertEquals(expectedResponse, result);
    }

    @Test(expected = HttpResponseException.class)
    public void shouldThrowHttpResponseExceptionWhenNumberOfRepetitionsExceeded() {
        // given
        final String expectedResponse = "Response";
        HttpResponseExceptionRequestRepeater<String> objectUnderTest =
                new TestHttpResponseExceptionRequestRepeater(
                        TestHttpResponseExceptionRequestRepeater.MAX_NUMBER_OF_REPETITIONS,
                        expectedResponse);
        // when
        String result = objectUnderTest.execute();
        // then
        // exception
    }

    private class TestHttpResponseExceptionRequestRepeater
            extends HttpResponseExceptionRequestRepeater<String> {

        static final int MAX_NUMBER_OF_REPETITIONS = 5;

        private final int numberOfRepetitionsOnException;
        private int repetitionCounter = 1;
        private final String expectedResponse;

        public TestHttpResponseExceptionRequestRepeater(
                int numberOfRepetitionsOnException, final String expectedResponse) {
            super(MAX_NUMBER_OF_REPETITIONS);
            this.numberOfRepetitionsOnException = numberOfRepetitionsOnException;
            this.expectedResponse = expectedResponse;
        }

        @Override
        public String request() {
            if (repetitionCounter++ <= numberOfRepetitionsOnException) {
                throw Mockito.mock(HttpResponseException.class);
            }
            return expectedResponse;
        }

        @Override
        public boolean checkIfRepeat(HttpResponseException ex) {
            return true;
        }
    }
}
