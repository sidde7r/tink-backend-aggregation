package se.tink.backend.aggregation.mocks;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ResultCaptor<T> implements Answer<T> {
    private T actual;

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        actual = (T) invocation.callRealMethod();
        return actual;
    }

    public T getActual() {
        return actual;
    }
}
