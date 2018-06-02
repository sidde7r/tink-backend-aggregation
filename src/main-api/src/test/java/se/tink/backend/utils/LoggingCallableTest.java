package se.tink.backend.utils;

import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class LoggingCallableTest {
    
    private static class HappyCallable implements Callable<Boolean> {

        @Override
        public Boolean call() {
            return true;
        }
        
    }
    
    private static class SadCallable implements Callable<Boolean> {

        private RuntimeException exception;

        public SadCallable(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public Boolean call() {
            throw exception;
        }
        
        @Override
        public String toString() {
            return "SadCallable";
        }
        
    }
    
    @Test
    public void testSuccesfulExecution() throws Exception {
        LogUtils log = Mockito.mock(LogUtils.class);
        LoggingCallable<Boolean> loggingRunnable = new LoggingCallable<Boolean>(new HappyCallable(), log);
        loggingRunnable.call();
        Mockito.verifyZeroInteractions(log);
    }
    
    @Test
    public void testFailingExecution() throws Exception {
        RuntimeException exception = new RuntimeException("Expected error.");
        LogUtils log = Mockito.mock(LogUtils.class);
        LoggingCallable<Boolean> loggingRunnable = new LoggingCallable<Boolean>(new SadCallable(exception), log);
        
        boolean thrown = false;
        try {
            loggingRunnable.call();
        } catch (RuntimeException e) {
            thrown = true;
            Assert.assertTrue(exception == e);
        }
        
        Assert.assertTrue(thrown);
        Mockito.verify(log).error("Could not execute delegated callable 'SadCallable'.", exception);
        Mockito.verifyNoMoreInteractions(log);
    }
}
