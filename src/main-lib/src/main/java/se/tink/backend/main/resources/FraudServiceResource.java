package se.tink.backend.main.resources;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.api.FraudService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.FraudItemRepository;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.common.template.Template;
import se.tink.backend.common.utils.TemplateUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.rpc.FraudActivationRequest;
import se.tink.backend.rpc.FraudActivationResponse;
import se.tink.backend.rpc.FraudChangeStatusRequest;
import se.tink.backend.rpc.FraudDetailsHelpResponse;
import se.tink.backend.rpc.FraudDetailsListResponse;
import se.tink.backend.rpc.FraudItemsResponse;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.identity.model.IdentityEventDocumentation;
import se.tink.libraries.identity.utils.IdentityTextUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

@Path("/api/v1/fraud")
public class FraudServiceResource implements FraudService {

    @Context
    private HttpHeaders headers;

    private static final LogUtils log = new LogUtils(FraudServiceResource.class);
    private static final MetricId DETAILS_OK = MetricId.newId("fraud_details_ok");
    private static final MetricId DETAILS_FRAUDULENT = MetricId.newId("fraud_details_fraudulent");
    private final Counter okFraudDetailsMeter;
    private final Counter fraudulentFraudDetailsMeter;

    private final ServiceContext serviceContext;
    private final ServiceFactory serviceFactory;
    private final SystemServiceFactory systemServiceFactory;
    private final AnalyticsController analyticsController;
    private final DeleteController deleteController;

    private final CredentialsRepository credentialsRepository;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final FraudItemRepository fraudItemRepository;

    private final CurrenciesByCodeProvider currenciesByCodeProvider;

    private final HttpResponseHelper httpResponseHelper;

    public FraudServiceResource(ServiceContext serviceContext, MetricRegistry metricRegistry) {
        this.serviceContext = serviceContext;

        this.serviceFactory = serviceContext.getServiceFactory();
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();
        this.analyticsController = new AnalyticsController(serviceContext.getEventTracker());
        this.deleteController = new DeleteController(serviceContext);

        this.credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        this.fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);
        this.fraudItemRepository = serviceContext.getRepository(FraudItemRepository.class);

        this.currenciesByCodeProvider = new CurrenciesByCodeProvider(
                serviceContext.getRepository(CurrencyRepository.class));

        this.okFraudDetailsMeter = metricRegistry.meter(DETAILS_OK);
        this.fraudulentFraudDetailsMeter = metricRegistry.meter(DETAILS_FRAUDULENT);
        this.httpResponseHelper = new HttpResponseHelper(log);
    }

    @Override
    public FraudDetailsListResponse details(User user, String fraudItemId) {
        if (user.getProfile().getFraudPersonNumber() == null) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        FraudDetailsListResponse response = new FraudDetailsListResponse();

        List<FraudDetails> details = Lists.newArrayList();

        // Populate data on description based on locale.

        for (FraudDetails fraudDetail : fraudDetailsRepository.findAllByUserIdAndFraudItemId(user.getId(), fraudItemId)) {
            FraudUtils.setDescriptionsFromContent(fraudDetail, user,
                    currenciesByCodeProvider.get().get(user.getProfile().getCurrency()));
            FraudUtils.setTitleFromContentType(user, fraudDetail);
            FraudUtils.setQuestionFromContentType(user, fraudDetail);
            FraudUtils.setAnswersFromContentType(user, fraudDetail);

            details.add(fraudDetail);
        }

        log.info(user.getId(), "Returning " + details.size() + " fraud details for fraudItemId: " + fraudItemId);

        // Hack to handle when we have no details.

        TinkUserAgent userAgent = new TinkUserAgent(RequestHeaderUtils.getUserAgent(headers));
        if (details.isEmpty() && !FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE_V2.hasValidClientVersion(userAgent)) {
            FraudItem item = fraudItemRepository.findOne(fraudItemId);

            FraudDetails fraudDetail = FraudUtils.getEmptyStateContentFromType(user,
                    FraudUtils.getDefaultContentTypeForItemType(item.getType()));
            FraudUtils.setDescriptionsFromContent(fraudDetail, user,
                    currenciesByCodeProvider.get().get(user.getProfile().getCurrency()));
            FraudUtils.setTitleFromContentType(user, fraudDetail);

            details.add(fraudDetail);
        }

        response.setFraudDetails(details);
        response.setFraudItemId(fraudItemId);

        return response;
    }

    @Override
    public FraudItemsResponse list(final User user) {
        if (!FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE.isFlagInGroup(user.getFlags())) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        FraudItemsResponse response = new FraudItemsResponse();

        List<FraudItem> items = Lists.newArrayList();

        for (FraudItem fraudItem : fraudItemRepository.findAllByUserId(user.getId())) {
            FraudUtils.setItemTitleFromItemType(user, fraudItem);
            items.add(fraudItem);
        }

        if (items.isEmpty() && RequestHeaderUtils.isIosRequest(headers)) {
            log.info(user.getId(), "Creating basic fraud items.");
            items.addAll(FraudUtils.createBasicFraudItems(user));
            fraudItemRepository.save(items);
        }

        log.info(user.getId(), "Returning " + items.size() + " fraud items.");

        response.setFraudItems(items);
        return response;
    }

    @Override
    public FraudActivationResponse activation(AuthenticatedUser authenticatedUser, FraudActivationRequest request) {
        User user = authenticatedUser.getUser();

        if (!FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE.isFlagInGroup(user.getFlags())) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        List<Credentials> existingFraudCredentials = credentialsRepository.findAllByUserIdAndType(user.getId(),
                CredentialsTypes.FRAUD);

        FraudActivationResponse response = new FraudActivationResponse();

        if (request.isActivate()) {

            if (existingFraudCredentials != null && !existingFraudCredentials.isEmpty()) {

                // Update the existing fraud credentials.

                Credentials credentials = existingFraudCredentials.get(0);
                response.setCredentials(serviceFactory.getCredentialsService().update(authenticatedUser, credentials.getId(),
                        credentials));
            } else {

                Credentials credentials = new Credentials();
                credentials.setProviderName("creditsafe");
                credentials.setType(CredentialsTypes.FRAUD);
                credentials.setUsername(request.getPersonIdentityNumber());

                response.setCredentials(serviceFactory.getCredentialsService().create(
                        authenticatedUser, null, credentials, Collections.emptySet()));
            }
        } else if (!request.isActivate()) {
            for (Credentials fraudCredentials : existingFraudCredentials) {
                deleteController.deleteCredentials(user, fraudCredentials.getId(), true,
                        RequestHeaderUtils.getRemoteIp(headers));
            }
        }
        return response;
    }

    @Override
    public FraudDetailsListResponse status(User user, FraudChangeStatusRequest request) {
        if (request.getStatus() == null || Strings.isNullOrEmpty(request.getStatus().name())) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        List<FraudDetails> fraudDetails = Lists.newArrayList();
        String itemId = null;

        if (request.getFraudDetailIds() != null && request.getFraudDetailIds().size() != 0) {

            // Authenticate.

            fraudDetails.addAll(fraudDetailsRepository.findAllForIds(request.getFraudDetailIds()));

            for (FraudDetails details : fraudDetails) {
                if (!Objects.equals(details.getUserId(), user.getId())) {
                    HttpResponseHelper.error(Status.UNAUTHORIZED);
                }
            }

            if (fraudDetails.size() != 0) {

                ImmutableListMultimap<String, FraudDetails> detaildByItemId = Multimaps.index(fraudDetails,
                        FraudDetails::getFraudItemId);

                if (detaildByItemId.keySet().size() > 1) {
                    log.error(user.getId(), "Fraud details don't belong to the same fraud item.");
                    HttpResponseHelper.error(Status.BAD_REQUEST);
                }

                itemId = Iterables.get(detaildByItemId.keySet(), 0);

                // Handle different statuses.

                switch (request.getStatus()) {
                case OK:
                case FRAUDULENT:
                case CRITICAL:
                    updateStatusForDetails(request, fraudDetails);
                    break;
                case SEEN:
                    updateUnseenCountOnItem(user, itemId);
                    break;
                default:
                    log.info(user.getId(), "Cannot handle status " + request.getStatus());
                    break;
                }

                // Track.

                Map<String, Object> properties = Maps.newHashMap();
                properties.put("Status", request.getStatus());
                properties.put("Count", request.getFraudDetailIds().size());

                analyticsController.trackUserEvent(user, "fraud.status", properties,
                        RequestHeaderUtils.getRemoteIp(headers));

                // Regenerate the activities.

                systemServiceFactory.getProcessService()
                        .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.SIMPLE);
            }
        }

        // Set text fields on details objects.

        for (FraudDetails detail : fraudDetails) {
            FraudUtils.setDescriptionsFromContent(detail, user,
                    currenciesByCodeProvider.get().get(user.getProfile().getCurrency()));
            FraudUtils.setTitleFromContentType(user, detail);
            FraudUtils.setQuestionFromContentType(user, detail);
            FraudUtils.setAnswersFromContentType(user, detail);
        }

        FraudDetailsListResponse response = new FraudDetailsListResponse();
        response.setFraudDetails(fraudDetails);
        response.setFraudItemId(itemId);

        return response;
    }

    /**
     * Sets all details for this item as seen.
     * 
     * @param user
     * @param itemId
     */
    private void updateUnseenCountOnItem(User user, String itemId) {
        FraudItem item = fraudItemRepository.findOne(itemId);

        if (item == null) {
            log.error(user.getId(), "Could not find fraudItem for itemID: " + itemId);
            return;
        }

        item.setUnseenDetailsCount(0);
        fraudItemRepository.save(item);
    }

    /**
     * Updates the status on the fraud details.
     * 
     * @param request
     * @param fraudDetails
     */
    private void updateStatusForDetails(FraudChangeStatusRequest request, List<FraudDetails> fraudDetails) {
        Date now = new Date();
        for (FraudDetails details : fraudDetails) {
            if (details.getStatus() == FraudStatus.EMPTY) {
                continue;
            }

            details.setStatus(request.getStatus());
            details.setUpdated(now);

            registerMetric(request.getStatus());
        }

        fraudDetailsRepository.save(fraudDetails);
    }

    private void registerMetric(FraudStatus status) {
        if (status == FraudStatus.OK) {
            okFraudDetailsMeter.inc();
        } else if (status == FraudStatus.FRAUDULENT) {
            fraudulentFraudDetailsMeter.inc();
        }
    }

    @Override
    public FraudDetailsHelpResponse help(User user, FraudDetailsContentType type) {
        String format = TemplateUtils.getTemplate("data/templates/html-activity-format.html");
        String defaultStyle = TemplateUtils.getTemplate("data/templates/default-style.html");

        IdentityEventDocumentation doc = IdentityTextUtils.getDocumentation(user.getLocale(), type,
                IdentityTextUtils.Format.HTML);

        String head = TemplateUtils.getTemplate("data/templates/fraud/fraud-details-help-head.html");
        String body = String.format(TemplateUtils.getTemplate("data/templates/fraud/fraud-details-help-body.html"),
                doc.getHelpTitle(), doc.getHelpText(),
                doc.getInfoTitle(), doc.getInfoText(),
                doc.getSourceTitle(), doc.getSourceText());

        return new FraudDetailsHelpResponse(Catalog.format(format, defaultStyle, head, body));
    }

    @Override
    public FraudDetailsHelpResponse extendedInformation(User user, String detailsId) {

        if (Strings.isNullOrEmpty(detailsId)) {
            httpResponseHelper.error(Status.BAD_REQUEST, "Details Id was not provided");
        }

        FraudDetails details = fraudDetailsRepository.findOne(detailsId);
        if (details == null) {
            httpResponseHelper.error(Status.NOT_FOUND, "Could not find any FraudDetails with id: " + detailsId);
        }

        if (!user.getId().equals(details.getUserId())) {
            httpResponseHelper.error(Status.FORBIDDEN, "User asked for someone else FraudDetails");
        }

        String defaultStyle = TemplateUtils.getTemplate("data/templates/default-style.html");

        Map<String, Object> context = new HashMap<>();

        context.put("defaultStyle", defaultStyle);

        IdentityEventDocumentation documentation = IdentityTextUtils
                .getDocumentation(user.getLocale(), details.getType(), IdentityTextUtils.Format.HTML);

        context.put("whatIfFraudulentTitle", documentation.getFraudText());
        context.put("whatIfFraudulentDescription", documentation.getHelpText());
        context.put("sourceTitle", documentation.getInfoTitle());
        context.put("sourceDescription", documentation.getInfoText());
        context.put("sourceLabel", documentation.getSourceTitle());
        context.put("sourceTypeSource", documentation.getSourceText());

        return new FraudDetailsHelpResponse(serviceContext.getTemplateRenderer().render(
                Template.FRAUD_FRAUD_EXTENDED_INFO_HTML, context));
    }
}
