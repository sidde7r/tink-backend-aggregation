package se.tink.libraries.creditsafe.consumermonitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.soap.Account;
import se.tink.libraries.creditsafe.consumermonitoring.soap.LANGUAGE;
import se.tink.libraries.creditsafe.consumermonitoring.soap.MonitoredObject;
import se.tink.libraries.creditsafe.consumermonitoring.soap.Monitoring;
import se.tink.libraries.creditsafe.consumermonitoring.soap.MonitoringRequest;
import se.tink.libraries.creditsafe.consumermonitoring.soap.MonitoringResponse;
import se.tink.libraries.creditsafe.consumermonitoring.soap.MonitoringSoap;
import se.tink.libraries.creditsafe.consumermonitoring.soap.PortfolioObject;
import se.tink.libraries.creditsafe.consumermonitoring.soap.ResultCountersReqObject;
import se.tink.libraries.creditsafe.consumermonitoring.soap.STATUS;

public class ConsumerMonitoringWrapper {

    private static final Logger log = LoggerFactory.getLogger(ConsumerMonitoringWrapper.class);
    private static final int PAGE_SIZE = 8000;

    private static final ObjectMapper XML_MAPPER = new XmlMapper();
    private final boolean debug = true;

    private Account account;
    private MonitoringSoap client;

    private LANGUAGE language;

    public ConsumerMonitoringWrapper(String tinkUsername, String tinkPassword, boolean logTraffic) {
        this.language = LANGUAGE.SWE;

        account = new Account();
        account.setUserID(tinkUsername);
        account.setPassword(tinkPassword);

        Monitoring monitoring = new Monitoring();
        if (logTraffic) {
            monitoring.setHandlerResolver(new HandlerResolverImpl());
        }
        client = monitoring.getMonitoringSoap();
    }

    @SuppressWarnings("rawtypes")
    public static class HandlerResolverImpl implements HandlerResolver {
        public List<Handler> getHandlerChain(PortInfo portInfo) {
            List<Handler> handlers = new ArrayList<>();
            handlers.add(new SOAPLogHandler());
            return handlers;
        }
    }

    private MonitoringRequest createRequest(String portfolio) {
        return createRequest(PAGE_SIZE, 1, portfolio);
    }

    private MonitoringRequest createRequest(int pageSize, int startPos, String portfolio) {
        MonitoringRequest request = new MonitoringRequest();
        request.setAccount(account);
        request.setLanguage(language);
        request.setPortfolioName(portfolio);

        ResultCountersReqObject results = new ResultCountersReqObject();
        results.setPageSize(pageSize);
        results.setStartPosition(startPos);
        request.setResultCounters(results);

        return request;
    }

    private void logIfRequestFailed(String request, MonitoringResponse response) {
        if (response.getStatus() == STATUS.NOTOK && response.getError() != null) {
            log.error(
                    request
                            + " to CreditSafe failed with code: "
                            + response.getError().getRejectCode()
                            + " and message: "
                            + response.getError().getRejectText());
        }
    }

    private void log(String name, MonitoringRequest request, MonitoringResponse response) {

        if (debug) {
            try {
                String requestAsString = XML_MAPPER.writeValueAsString(request);
                if (request.getAccount().getPassword() != null) {
                    requestAsString =
                            requestAsString.replace(request.getAccount().getPassword(), "***");
                }
                if (request.getAccount().getUserID() != null) {
                    requestAsString =
                            requestAsString.replace(request.getAccount().getUserID(), "***");
                }
                if (request.getPnr() != null) {
                    requestAsString = requestAsString.replace(request.getPnr(), "***");
                }

                log.debug("Traffic with CreditSafe > " + name + " > " + requestAsString);

                // Create shallow copy without the "senstive" data
                MonitoringResponse responseToPrint = new MonitoringResponse();
                responseToPrint.setError(response.getError());
                responseToPrint.setResultCounters(response.getResultCounters());
                responseToPrint.setStatus(response.getStatus());

                String responseAsString = XML_MAPPER.writeValueAsString(responseToPrint);
                log.debug("Traffic with CreditSafe < " + name + " < " + responseAsString);

            } catch (Exception e) {
                log.warn("Was not able to log request and response from CreditSafe", e);
            }
        } else {
            logIfRequestFailed(name, response);
        }
    }

    public PortfolioListResponse listPortfolios() {
        MonitoringRequest request = createRequest(null);
        MonitoringResponse response = client.getPortfolioList(request);

        log("ListPortfolio", request, response);
        if (response == null || response.getStatus() != STATUS.OK) {
            return null;
        }

        List<PortfolioObject> portfolios = Lists.newArrayList();
        for (Object object : response.getObjectList().getAnyType()) {
            if (object instanceof PortfolioObject) {
                PortfolioObject portfolio = (PortfolioObject) object;
                if (!"TINK_TEST".equals(portfolio.getPortfolioName())) {
                    portfolios.add(portfolio);
                }
            } else {
                log.warn("Got unexpected object in PortofolioList");
            }
        }

        PortfolioListResponse portfolioListResponse = new PortfolioListResponse();
        portfolioListResponse.setPortfolios(getPortfolioList(portfolios));
        return portfolioListResponse;
    }

    public PageableConsumerCreditSafeResponse listMonitoredConsumers(
            PageableConsumerCreditSafeRequest creditSafeRequest) {

        MonitoringRequest request =
                createRequest(
                        creditSafeRequest.getPageSize(),
                        creditSafeRequest.getPageStart(),
                        creditSafeRequest.getPortfolio());

        MonitoringResponse response = client.getMonitoredConsumers(request);
        log("GetMonitoredConsumers", request, response);

        return buildPageableCreditSafeResponse(response);
    }

    public PageableConsumerCreditSafeResponse listChangedConsumers(
            ChangedConsumerCreditSafeRequest creditSafeRequest) {

        MonitoringRequest request =
                createRequest(
                        creditSafeRequest.getPageSize(),
                        creditSafeRequest.getPageStart(),
                        creditSafeRequest.getPortfolio());
        request.setNumberOfDays(creditSafeRequest.getChangedDays());

        MonitoringResponse response = client.getChangedConsumers(request);
        log("GetChangedConsumer", request, response);

        return buildPageableCreditSafeResponse(response);
    }

    private PageableConsumerCreditSafeResponse buildPageableCreditSafeResponse(
            MonitoringResponse response) {

        PageableConsumerCreditSafeResponse creditSafeResponse =
                new PageableConsumerCreditSafeResponse();
        List<MonitoredObject> consumers = Lists.newArrayList();

        if (response.getStatus() == STATUS.OK) {

            for (Object object : response.getObjectList().getAnyType()) {
                if (object instanceof MonitoredObject) {
                    consumers.add((MonitoredObject) object);
                } else {
                    log.warn("Got unexpected object in CreditSafe Response page");
                }
            }

            creditSafeResponse.setConsumers(getPnrList(consumers));
            creditSafeResponse.setPageEnd(response.getResultCounters().getEndPosition());
            creditSafeResponse.setTotalPortfolioSize(response.getResultCounters().getTotalCount());
        }

        creditSafeResponse.setErrorCode(
                response.getError() != null ? response.getError().getRejectCode() : null);
        creditSafeResponse.setErrorMessage(
                response.getError() != null ? response.getError().getRejectText() : null);
        creditSafeResponse.setStatus(response.getStatus().name());

        return creditSafeResponse;
    }

    private List<String> getPnrList(List<MonitoredObject> consumers) {
        if (consumers == null) {
            return null;
        }

        List<String> pnrs =
                Lists.newArrayList(
                        Iterables.transform(
                                consumers,
                                new Function<MonitoredObject, String>() {
                                    @Nullable
                                    @Override
                                    public String apply(MonitoredObject o) {
                                        return o.getPnr();
                                    }
                                }));
        return pnrs;
    }

    private List<String> getPortfolioList(List<PortfolioObject> portfolios) {
        if (portfolios == null) {
            return null;
        }

        return Lists.newArrayList(
                Iterables.transform(
                        portfolios,
                        new Function<PortfolioObject, String>() {
                            @Nullable
                            @Override
                            public String apply(PortfolioObject o) {
                                return o.getPortfolioName();
                            }
                        }));
    }

    public void addMonitoring(AddMonitoredConsumerCreditSafeRequest addMonitoredRequest) {
        MonitoringRequest request = createRequest(addMonitoredRequest.getPortfolio());
        request.setPnr(addMonitoredRequest.getPnr());
        MonitoringResponse response = client.monitorConsumer(request);

        log("MonitorConsumer", request, response);
    }

    public void removeMonitoring(RemoveMonitoredConsumerCreditSafeRequest monitoredRequest) {

        // Remove pnr from all portfolios
        for (String portfolio : monitoredRequest.getPortfolios()) {
            MonitoringRequest request = createRequest(portfolio);
            request.setPnr(monitoredRequest.getPnr());
            MonitoringResponse response = client.removeConsumer(request);
            log("RemoveConsumer", request, response);
        }
    }
}
