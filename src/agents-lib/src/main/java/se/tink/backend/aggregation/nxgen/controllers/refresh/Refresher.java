package se.tink.backend.aggregation.nxgen.controllers.refresh;

import se.tink.libraries.metrics.MetricId;

public interface Refresher {
    MetricId REFRESHER_METRIC_ID = MetricId.newId("refresh_controller");
    String METRIC_ITEM_TYPE = "item";
}
