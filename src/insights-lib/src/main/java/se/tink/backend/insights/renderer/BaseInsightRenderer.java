package se.tink.backend.insights.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import org.rythmengine.RythmEngine;
import se.tink.backend.insights.core.domain.model.HasExternalId;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.valueobjects.HtmlData;
import se.tink.backend.insights.core.valueobjects.InsightAction;
import se.tink.backend.insights.renderer.data.Button;
import se.tink.backend.insights.renderer.data.RenderData;
import se.tink.backend.insights.renderer.strings.ActionTypeToDeeplinkMapper;
import se.tink.backend.insights.renderer.strings.InsightDeeplinkBuilderFactory;

public class BaseInsightRenderer {

    protected static RythmEngine rythmEngine;
    private static InsightDeeplinkBuilderFactory deeplinkBuilderFactory;

    @Inject
    public BaseInsightRenderer() {
        deeplinkBuilderFactory = new InsightDeeplinkBuilderFactory();

        Map<String, Object> config = Maps.newHashMap();

        // tell rythm where to find the template files
        String userDir = System.getProperty("user.dir");
        config.put("home.template", userDir + "/data/templates/insights/");
        rythmEngine = new RythmEngine(config);
    }

    public String render(Insight insight) {

        Map<String, Object> params = Maps.newHashMap();

        HtmlData htmlData = insight.getHtmlData();

        List<Button> buttons = getButtons(insight);
        RenderData renderData = new RenderData.Builder()
                .setActivityClass(htmlData.getActivityDivClass()) //Fixme: generalize later
                .setTitleMessage(insight.composeTitle())
                .setBodyMessage(insight.composeMessage())
                .setIcon(htmlData.getIcon())
                .setButtons(buttons)
                .build();

        params.put("renderData", renderData);
        return rythmEngine.render("insight-layout.html", params);

    }

    public List<Button> getButtons(Insight insight) {
        List<Button> buttons = Lists.newArrayList();
        for (InsightAction insightAction : insight.getActions()) {
            String deeplink = createDeeplink(insight, insightAction);
            String message = insightAction.getDescription();
            String divClass = insightAction.getButtonDivType().getValue();

            buttons.add(Button.of(divClass, message, deeplink));
        }
        return buttons;
    }

    public String createDeeplink(Insight insight, InsightAction action) {

        final String id = (insight instanceof HasExternalId) ? ((HasExternalId) insight).getExternalId() : "";

        String baseDeeplink = ActionTypeToDeeplinkMapper.getDeeplinkByInsightActionType(action.getType());
        return deeplinkBuilderFactory
                .setBaseDeeplink(baseDeeplink, id)
                .withInsightId(insight.getId().value())
                .withOptionKey(action.getId().value())
                .build();
    }
}
