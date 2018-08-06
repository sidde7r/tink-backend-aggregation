package se.tink.backend.aggregation.resources;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.aggregation.agents.fraud.CreditSafeAgent;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.SeedPersonDataRequest;
import se.tink.backend.aggregation.rpc.SeedPersonDataResponse;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class CreditSafeServiceResource implements CreditSafeService {
    /**
     * Helper method to create a non-user/credentials-based agent instance for using the CreditSafe maintenance methods.
     */
    private static CreditSafeAgent createCreditSafeAgent() {
        return new CreditSafeAgent(new CreateCredentialsRequest(null, null, null), null);
    }

    private ServiceContext serviceContext;

    public CreditSafeServiceResource(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    @Override
    public void removeConsumerMonitoring(RemoveMonitoredConsumerCreditSafeRequest request) {
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        CreditSafeAgent agent = createCreditSafeAgent();
        agent.setConfiguration(serviceContext.getConfiguration());

        agent.removeConsumerMonitoring(request);
    }

    @Override
    public Response addConsumerMonitoring(AddMonitoredConsumerCreditSafeRequest request) {
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        CreditSafeAgent agent = createCreditSafeAgent();
        agent.setConfiguration(serviceContext.getConfiguration());

        agent.addConsumerMonitoring(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public PortfolioListResponse listPortfolios() {
        CreditSafeAgent agent = createCreditSafeAgent();
        agent.setConfiguration(serviceContext.getConfiguration());

        return agent.listPortfolios();
    }

    @Override
    public PageableConsumerCreditSafeResponse listChangedConsumers(ChangedConsumerCreditSafeRequest request) {
        CreditSafeAgent agent = createCreditSafeAgent();
        agent.setConfiguration(serviceContext.getConfiguration());

        return agent.listChangedConsumers(request);
    }

    @Override
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(PageableConsumerCreditSafeRequest request) {
        CreditSafeAgent agent = createCreditSafeAgent();
        agent.setConfiguration(serviceContext.getConfiguration());

        return agent.listMonitoredConsumers(request);
    }

    @Override
    public SeedPersonDataResponse seedPersonData(SeedPersonDataRequest request) {
        CreditSafeAgent agent = createCreditSafeAgent();
        agent.setConfiguration(serviceContext.getConfiguration());

        SeedPersonDataResponse response = new SeedPersonDataResponse();
        response.setFraudDetailsContent(agent.seedPersonData(request.getPersonNumner()));

        return response;
    }
}
