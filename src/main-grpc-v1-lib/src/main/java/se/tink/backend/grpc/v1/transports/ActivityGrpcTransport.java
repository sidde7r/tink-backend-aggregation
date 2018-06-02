package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import java.util.List;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.grpc.v1.converter.activity.ActivityHtmlHeadResponseConverter;
import se.tink.backend.grpc.v1.converter.activity.ActivityQueryConverter;
import se.tink.backend.grpc.v1.converter.activity.ListActivityHtmlCommandConverter;
import se.tink.backend.grpc.v1.converter.activity.ListHtmlActivityResponseConverter;
import se.tink.backend.grpc.v1.converter.insights.InsightToActivityConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.ActionableInsightsServiceController;
import se.tink.backend.main.controllers.ActivityServiceController;
import se.tink.backend.main.utils.ActivityHtmlHelper;
import se.tink.backend.rpc.ActivityQueryResponse;
import se.tink.backend.rpc.HtmlHeadResponse;
import se.tink.backend.rpc.ListHtmlResponse;
import se.tink.backend.rpc.actionableinsights.GetRenderedInsightsCommand;
import se.tink.grpc.v1.rpc.ActivityHtmlHeadRequest;
import se.tink.grpc.v1.rpc.ActivityHtmlHeadResponse;
import se.tink.grpc.v1.rpc.ListActivityHtmlRequest;
import se.tink.grpc.v1.rpc.ListActivityHtmlResponse;
import se.tink.grpc.v1.services.ActivityServiceGrpc;

public class ActivityGrpcTransport extends ActivityServiceGrpc.ActivityServiceImplBase {
    private final ActivityServiceController activityServiceController;
    private final ActivityHtmlHelper activityHtmlHelper;
    private final ActionableInsightsServiceController actionableInsightsServiceController;

    @Inject
    public ActivityGrpcTransport(ActivityServiceController activityServiceController,
            ActivityHtmlHelper activityHtmlHelper,
            ActionableInsightsServiceController actionableInsightsServiceController) {
        this.activityServiceController = activityServiceController;
        this.activityHtmlHelper = activityHtmlHelper;
        this.actionableInsightsServiceController = actionableInsightsServiceController;
    }

    @Override
    @Authenticated
    public void listHtml(ListActivityHtmlRequest request,
            StreamObserver<ListActivityHtmlResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        String userAgent = authenticationContext.getUserAgent().orElse(null);
        try {
            ListHtmlResponse htmlResponse;

            if(user.getFlags().contains(FeatureFlags.TEST_ACTIONABLE_INSIGHTS)) {
                List<String> htmls = actionableInsightsServiceController.getRendered(
                        new GetRenderedInsightsCommand(user.getId(), request.getOffset(), request.getLimit()));
                htmlResponse = InsightToActivityConverter.getActivityResponse(request, htmls);
            } else {
                ActivityQueryResponse activityResponse = activityServiceController
                        .query(authenticationContext.getUser().getId(),
                                userAgent,
                                ActivityQueryConverter.convertFrom(request));
                htmlResponse = activityHtmlHelper.listHtml(authenticationContext.getUser(),
                        ListActivityHtmlCommandConverter
                                .convertFrom(request, activityResponse.getActivities(), userAgent));
            }
            streamObserver.onNext(ListHtmlActivityResponseConverter.convertFrom(htmlResponse));
            streamObserver.onCompleted();


        } catch (LockException e) {
            throw ApiError.Activities.UNAVAILABLE.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void htmlHead(ActivityHtmlHeadRequest request, StreamObserver<ActivityHtmlHeadResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        HtmlHeadResponse htmlHeadResponse = activityHtmlHelper
                .htmlHead(authenticationContext.getUser(), authenticationContext.getUserAgent().orElse(null));
        streamObserver.onNext(ActivityHtmlHeadResponseConverter.convertFrom(htmlHeadResponse));
        streamObserver.onCompleted();
    }


}
