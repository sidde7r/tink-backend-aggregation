package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;

public class CheckableHealthCheck extends HealthCheck {
    
    public static final class FailingCheckException extends Exception {

        private static final long serialVersionUID = 3834417777406422866L;
        
        public FailingCheckException(Checkable pingable, Exception reason) {
            super(String.format("Unable to check %s", pingable.toString()), reason);
        }

    }

    private final Checkable checkable;
    
    public CheckableHealthCheck(Checkable checkable) {
        this.checkable = checkable;
    }
    
    @Override
    protected Result check() throws FailingCheckException {
        try {
            checkable.check();
        } catch (Exception e) {
            throw new FailingCheckException(checkable, e);
        }
        return Result.healthy();
    }
}
