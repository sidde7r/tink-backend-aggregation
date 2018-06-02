package se.tink.backend.product.execution.unit.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.core.application.ApplicationState;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.product.execution.model.CreateProductResponse;
import se.tink.backend.product.execution.model.CredentialsUpdate;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.model.RefreshApplicationParameterKey;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateApplicationRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.rpc.UpdateProductInformationRequest;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.metrics.MetricId;

public interface CreateProductExecutor {

    CreateProductResponse create(GenericApplication application, CredentialsUpdate credentialsUpdate) throws Exception;

    void fetchProductInformation(ProductType type, UUID productInstanceId, UUID userId,
            Map<FetchProductInformationParameterKey, Object> parameters);

    void refreshApplication(ProductType type, UUID applicationId, UUID userId,
            Map<RefreshApplicationParameterKey, Object> parameters) throws Exception;

    default void updateProductInformation(SystemServiceFactory systemServiceFactory, UUID userId,
            UUID productInstanceId, HashMap<ProductPropertyKey, Object> properties) {
        UpdateProductInformationRequest request = new UpdateProductInformationRequest(
                userId.toString(),
                productInstanceId,
                properties);

        systemServiceFactory.getUpdateService().updateProductInformation(request);
    }

    default void updateApplication(SystemServiceFactory systemServiceFactory, UUID userId, UUID applicationId,
            ApplicationState applicationState) {
        UpdateApplicationRequest request = new UpdateApplicationRequest(
                userId,
                applicationId,
                applicationState);

        systemServiceFactory.getUpdateService().updateApplication(request);
    }

    String getProviderName();

    default MetricId getMetricId() {
        return MetricId.newId("create_product")
                .label("provider", getProviderName())
                .label("type", ApplicationType.SWITCH_MORTGAGE_PROVIDER.name());
    }

    default void updateCredentialStatus(SystemServiceFactory systemServiceFactory,
            CredentialsUpdate credentialsUpdate) {
        UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();
        updateCredentialsStatusRequest.setCredentials(credentialsUpdate.getCredentials());
        updateCredentialsStatusRequest.setUserId(credentialsUpdate.getCredentials().getUserId());
        updateCredentialsStatusRequest.setUpdateContextTimestamp(true);
        updateCredentialsStatusRequest.setManual(true);
        updateCredentialsStatusRequest.setUserDeviceId(credentialsUpdate.getUserDeviceId());

        systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsStatusRequest);
    }
}
