package se.tink.backend.insights.http;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.core.Response;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.TriggerHandler;
import se.tink.backend.insights.app.commands.ArchiveInsightCommand;
import se.tink.backend.insights.app.commands.RemovePreviousInsightsCommand;
import se.tink.backend.insights.app.commands.SetInsightChoiceCommand;
import se.tink.backend.insights.app.queryservices.InsightQueryService;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.http.dto.CreateInsightsRequest;
import se.tink.backend.insights.http.dto.GetInsightsRequest;
import se.tink.backend.insights.http.dto.GetInsightsResponse;
import se.tink.backend.insights.http.dto.GetRenderedRequest;
import se.tink.backend.insights.http.dto.GetRenderedResponse;
import se.tink.backend.insights.http.dto.SelectActionRequest;
import se.tink.backend.insights.http.dto.SelectActionResponse;
import se.tink.backend.insights.renderer.controller.InsightRenderer;
import se.tink.backend.insights.utils.LogUtils;

public class InsightServiceResource implements InsightService {

    private InsightRenderer insightRenderer;
    private CommandGateway gateway;
    private InsightQueryService insightQueryService;

    private static final LogUtils log = new LogUtils(InsightServiceResource.class);

    private TriggerHandler triggerHandler;

    @Inject
    public InsightServiceResource(InsightRenderer insightRenderer,
            InsightQueryService insightQueryService,
            CommandGateway gateway,
            TriggerHandler triggerHandler) {
        this.insightRenderer = insightRenderer;
        this.insightQueryService = insightQueryService;
        this.gateway = gateway;
        this.triggerHandler = triggerHandler;
    }

    @Override
    public GetRenderedResponse getRendered(GetRenderedRequest request) {

        if (!insightQueryService.userExists(UserId.of(request.getUserId()))) {
            throw new RuntimeException();
        }

        List<Insight> insights = insightQueryService.fetchInsightsFromOffsetWithLimit(
                UserId.of(request.getUserId()),
                request.getOffset(),
                request.getLimit());
        return new GetRenderedResponse(insightRenderer.renderAllInsights(insights));
    }

    @Override
    public Response create(CreateInsightsRequest request) {
        String userId = request.getUserId();

        if (!insightQueryService.userExists(UserId.of(userId))) {
            throw new RuntimeException();
        }

        // Todo: Remove/Change this when we have strong persistence of Insights in the future
        gateway.on(new RemovePreviousInsightsCommand(userId));

        triggerHandler.handle(userId);
        return Response.ok().build();
    }

    @Override
    public SelectActionResponse selectAction(SelectActionRequest request) {
        if (!insightQueryService.userExists(UserId.of(request.getUserId()))) {
            throw new RuntimeException();
        }

        Preconditions.checkArgument(!Strings.isNullOrEmpty(request.getUserId()));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(request.getInsightId()));

        gateway.on(new SetInsightChoiceCommand(request.getUserId(), request.getInsightId(), request.getActionId()));
        gateway.on(new ArchiveInsightCommand(request.getUserId(), request.getInsightId()));

        return new SelectActionResponse("Success");
    }

    @Override
    public GetInsightsResponse getInsights(GetInsightsRequest request) {
        // TODO: expose insights
        return null;
    }

}
