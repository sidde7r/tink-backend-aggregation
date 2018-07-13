package se.tink.backend.aggregation.agents.utils;

import com.google.inject.Inject;
import se.tink.backend.aggregation.rpc.ProductType;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class CreateProductExecutorTracker {
    private static final MetricId METRIC_CREATE_PRODUCT_SIGNATURE = MetricId.newId("create_product_signature");
    private static final MetricId METRIC_FETCH_PRODUCT_DISCOUNT = MetricId.newId("fetch_product_discount");
    private static final MetricId METRIC_FETCH_PRODUCT_INFORMATION = MetricId.newId("fetch_product_information");
    private static final MetricId METRIC_FETCH_APPLICATION_STATUS = MetricId.newId("fetch_application_status");
    private static final MetricId METRIC_FETCH_APPLICATION_SIGN_STATUS = MetricId
            .newId("fetch_application_sign_status");
    private static final MetricId METRIC_SUBMIT_APPLICATION = MetricId.newId("submit_application");

    private final MetricRegistry registry;

    @Inject
    public CreateProductExecutorTracker(MetricRegistry registry) {
        this.registry = registry;
    }

    private Counter getMeter(MetricId metricId, String providerName, ProductType type, int httpStatus) {
        return registry.meter(metricId
                .label("type", type.name())
                .label("provider", providerName)
                .label("http_status", Integer.toString(httpStatus))
        );
    }

    public void trackFetchProductInformation(String providerName, ProductType type, int httpStatus) {
        getMeter(METRIC_FETCH_PRODUCT_INFORMATION, providerName, type, httpStatus).inc();
    }

    public void trackSubmitApplication(String providerName, ProductType type, int httpStatus) {
        getMeter(METRIC_SUBMIT_APPLICATION, providerName, type, httpStatus).inc();
    }

    public void trackFetchApplicationStatus(String providerName, ProductType type, int httpStatus) {
        getMeter(METRIC_FETCH_APPLICATION_STATUS, providerName, type, httpStatus).inc();
    }

    public void trackFetchApplicationSignStatus(String providerName, ProductType type, int httpStatus) {
        getMeter(METRIC_FETCH_APPLICATION_SIGN_STATUS, providerName, type, httpStatus).inc();
    }

    public void trackFetchProductDiscount(String providerName, ProductType type, int httpStatus) {
        getMeter(METRIC_FETCH_PRODUCT_DISCOUNT, providerName, type, httpStatus).inc();
    }

    public void trackCreateProductSignature(String providerName, ProductType type, int httpStatus) {
        getMeter(METRIC_CREATE_PRODUCT_SIGNATURE, providerName, type, httpStatus).inc();
    }
}
