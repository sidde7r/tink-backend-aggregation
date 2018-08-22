package se.tink.backend.aggregation.resources;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.ConsumerMonitoringWrapper;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class CreditSafeServiceResource implements CreditSafeService {

    private ConsumerMonitoringWrapper consumerMonitoringWrapper;

    public CreditSafeServiceResource(ServiceContext serviceContext) {
        String user = serviceContext.getConfiguration().getCreditSafe().getUsername();
        String pass = serviceContext.getConfiguration().getCreditSafe().getPassword();
        boolean logTraffic = serviceContext.getConfiguration().getCreditSafe().isLogConsumerMonitoringTraffic();

        consumerMonitoringWrapper = new ConsumerMonitoringWrapper(user, pass, logTraffic);
    }

    @Override
    public void removeConsumerMonitoring(RemoveMonitoredConsumerCreditSafeRequest request) {
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        consumerMonitoringWrapper.removeMonitoring(request);
    }

    @Override
    public Response addConsumerMonitoring(AddMonitoredConsumerCreditSafeRequest request) {
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        consumerMonitoringWrapper.addMonitoring(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public PortfolioListResponse listPortfolios() {
        return consumerMonitoringWrapper.listPortfolios();
    }

    @Override
    public PageableConsumerCreditSafeResponse listChangedConsumers(ChangedConsumerCreditSafeRequest request) {
        return consumerMonitoringWrapper.listChangedConsumers(request);
    }

    @Override
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(PageableConsumerCreditSafeRequest request) {
        return consumerMonitoringWrapper.listMonitoredConsumers(request);
    }
}
