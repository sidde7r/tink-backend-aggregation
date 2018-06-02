package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.BiMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.stub.StreamObserver;
import java.util.List;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.statistic.CoreStatisticsToGrpcStatisticTreeConverter;
import se.tink.backend.grpc.v1.converter.statistic.InsightsResponseConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.InsightsController;
import se.tink.backend.main.controllers.StatisticsServiceController;
import se.tink.grpc.v1.models.StatisticTree;
import se.tink.grpc.v1.rpc.GetInsightsRequest;
import se.tink.grpc.v1.rpc.GetStatisticsRequest;
import se.tink.grpc.v1.rpc.InsightsResponse;
import se.tink.grpc.v1.rpc.QueryStatisticsRequest;
import se.tink.grpc.v1.rpc.QueryStatisticsResponse;
import se.tink.grpc.v1.rpc.StatisticsResponse;
import se.tink.grpc.v1.services.StatisticServiceGrpc;

public class StatisticGrpcTransport extends StatisticServiceGrpc.StatisticServiceImplBase {
    private final StatisticsServiceController statisticsServiceController;
    private final InsightsController insightsController;
    private final BiMap<String, String> categoryCodeById;

    @Inject
    public StatisticGrpcTransport(
            StatisticsServiceController statisticsServiceController,
            InsightsController insightsController,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById) {
        this.statisticsServiceController = statisticsServiceController;
        this.insightsController = insightsController;
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.STATISTICS_READ)
    public void queryStatistics(QueryStatisticsRequest request,
            StreamObserver<QueryStatisticsResponse> responseObserver) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();
        try {
            List<Statistic> statistics = statisticsServiceController
                    .getContextStatistics(user.getId(), user.getProfile().getPeriodMode(), false);
            StatisticTree statisticTree = new CoreStatisticsToGrpcStatisticTreeConverter(user.getProfile(),
                    categoryCodeById)
                    .convertFrom(statistics);
            responseObserver.onNext(QueryStatisticsResponse.newBuilder()
                    .setStatistics(statisticTree)
                    .build());
            responseObserver.onCompleted();
        } catch (LockException e) {
            throw ApiError.Statistics.UNAVAILABLE.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getInsights(GetInsightsRequest getInsightsRequest, StreamObserver<InsightsResponse> streamObserver) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();
        se.tink.backend.core.insights.InsightsResponse response = insightsController.getInsights(user);

        InsightsResponseConverter converter = new InsightsResponseConverter(user.getProfile().getCurrency());
        streamObserver.onNext(converter.convertFrom(response));
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.STATISTICS_READ)
    public void getStatistics(GetStatisticsRequest getStatisticsRequest,
            StreamObserver<StatisticsResponse> streamObserver) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        try {
            List<Statistic> statistics = statisticsServiceController
                    .list(user.getId(), user.getProfile().getPeriodMode());
            StatisticTree statisticTree = new CoreStatisticsToGrpcStatisticTreeConverter(user.getProfile(),
                    categoryCodeById).convertFrom(statistics);
            streamObserver.onNext(StatisticsResponse.newBuilder().setStatistics(statisticTree).build());
            streamObserver.onCompleted();

        } catch (LockException e) {
            throw ApiError.Statistics.UNAVAILABLE.withCause(e).exception();
        }

    }
}
