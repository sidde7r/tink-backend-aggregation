package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Strings;
import se.tink.backend.api.AbnAmroService;
import se.tink.backend.client.ServiceFactory;

public class AbnAmroServiceHealthCheck extends HealthCheck {

    private ServiceFactory serviceFactory;
    
    public AbnAmroServiceHealthCheck(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
    
    @Override
    protected Result check() throws Exception {
        if (Strings.isNullOrEmpty(getService().ping())) {
            return Result.unhealthy("No answer");
        } else {
            return Result.healthy();
        }
    }
    
    private AbnAmroService getService() {
        return serviceFactory.getAbnAmroService();
    }
}
