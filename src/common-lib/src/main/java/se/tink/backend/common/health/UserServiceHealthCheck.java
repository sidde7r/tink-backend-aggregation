package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Strings;
import se.tink.backend.api.UserService;
import se.tink.backend.client.ServiceFactory;

public class UserServiceHealthCheck extends HealthCheck {

    private ServiceFactory serviceFactory;

    public UserServiceHealthCheck(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
    
    @Override
    protected Result check() throws Exception {
        if (Strings.isNullOrEmpty(getService().ping(null))) {
            return Result.unhealthy("No answer");
        } else {
            return Result.healthy();
        }
    }
    
    private UserService getService() {
        return serviceFactory.getUserService();
    }
}
