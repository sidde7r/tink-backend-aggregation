package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.serializer.ScreenUpdateResponseDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonDeserialize(using = ScreenUpdateResponseDeserializer.class)
public class ScreenUpdateResponse extends ResponseEntity {

    private static final Logger log = LoggerFactory.getLogger(ScreenUpdateResponse.class);
    private final List<Widget> widgets;

    public ScreenUpdateResponse(List<Widget> widgets) {
        this.widgets = widgets;
    }

    public List<Widget> getWidgets() {
        return this.widgets != null ? this.widgets : Collections.emptyList();
    }

    public static Optional<Widget> findWidget(BelfiusResponse response, String widgetId) {
        return response.filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> widgetId.equalsIgnoreCase(widget.getWidgetId()))
                .findFirst();
    }

    public static Stream<Widget> streamWidgetsWithId(BelfiusResponse response, String widgetId) {
        return response.filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> widgetId.equalsIgnoreCase(widget.getWidgetId()));
    }

    public static Widget findWidgetOrElseThrow(BelfiusResponse response, String widgetId) {
        return response.filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> widgetId.equalsIgnoreCase(widget.getWidgetId()))
                .findFirst()
                .orElseThrow(
                        () ->
                                BankServiceError.BANK_SIDE_FAILURE.exception(
                                        "Could not find widget with widgetId: " + widgetId));
    }

    public static List<Widget> findWidgets(BelfiusResponse response, String widgetId) {
        return response.filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> widgetId.equalsIgnoreCase(widget.getWidgetId()))
                .collect(Collectors.toList());
    }

    public static Widget widgetContains(BelfiusResponse response, String widgetId) {
        List<Widget> collect =
                response.filter(ScreenUpdateResponse.class)
                        .flatMap(r -> r.getWidgets().stream())
                        .collect(Collectors.toList());
        for (Widget w : collect) {
            if (w.getWidgetId().contains(widgetId)) {
                return w;
            }
        }
        return null;
    }

    public static List<Widget> widgetsContains(BelfiusResponse response, String widgetId) {
        return response.filter(ScreenUpdateResponse.class)
                .flatMap(r -> r.getWidgets().stream())
                .filter(widget -> widget.getWidgetId().contains(widgetId))
                .collect(Collectors.toList());
    }
}
