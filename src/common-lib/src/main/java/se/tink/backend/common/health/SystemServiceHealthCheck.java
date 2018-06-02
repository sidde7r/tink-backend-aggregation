package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Strings;
import se.tink.backend.system.client.SystemServiceFactory;

public class SystemServiceHealthCheck extends HealthCheck {

    private SystemServiceFactory systemServiceFactory;

    public SystemServiceHealthCheck(SystemServiceFactory systemServiceFactory) {
        this.systemServiceFactory = systemServiceFactory;
    }
    
    @Override
    protected Result check() throws Exception {
        if (Strings.isNullOrEmpty(systemServiceFactory.getUpdateService().ping())) {
            return Result.unhealthy("No answer");
        } else {
            return Result.healthy();
        }
    }
}
