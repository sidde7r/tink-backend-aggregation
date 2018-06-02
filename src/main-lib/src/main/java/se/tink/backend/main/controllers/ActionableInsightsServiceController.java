package se.tink.backend.main.controllers;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.insights.http.dto.GetInsightsRequest;
import se.tink.backend.insights.http.dto.GetRenderedRequest;
import se.tink.backend.insights.http.dto.GetRenderedResponse;
import se.tink.backend.insights.http.dto.InsightDTO;
import se.tink.backend.insights.http.dto.SelectActionRequest;
import se.tink.backend.insights.http.dto.SelectActionResponse;
import se.tink.backend.rpc.actionableinsights.GetInsightsCommand;
import se.tink.backend.rpc.actionableinsights.GetRenderedInsightsCommand;
import se.tink.backend.rpc.actionableinsights.SelectOptionCommand;

public class ActionableInsightsServiceController {

    private InsightsServiceFactory insightsServiceFactory;

    @Inject
    public ActionableInsightsServiceController(@Nullable InsightsServiceFactory insightsServiceFactory) {
        this.insightsServiceFactory = insightsServiceFactory;
    }

    public List<String> getRendered(GetRenderedInsightsCommand command) {
        GetRenderedResponse htmlInsights = insightsServiceFactory.getInsightsService()
                .getRendered(new GetRenderedRequest(command.getUserId(), command.getOffset(), command.getLimit()));
        return htmlInsights.getHtml();
    }

    public void selectOption(SelectOptionCommand command) {
        SelectActionResponse response = insightsServiceFactory.getInsightsService().selectAction(
                new SelectActionRequest(command.getUserId(), command.getInsightsId(), command.getOptionId()));
        if (Objects.isNull(response)) {
            throw new RuntimeException();
        }
    }

    public List<InsightDTO> getInsights(GetInsightsCommand command) {
        return insightsServiceFactory.getInsightsService()
                .getInsights(new GetInsightsRequest(command.getUserId())).getInsights();
    }
}
