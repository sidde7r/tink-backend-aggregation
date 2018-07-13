package se.tink.backend.aggregation.agents;

import java.util.Map;
import java.util.UUID;
import se.tink.backend.aggregation.rpc.CreateProductResponse;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.application.RefreshApplicationParameterKey;
import se.tink.backend.aggregation.rpc.ProductType;

public interface CreateProductExecutor extends HttpLoggableExecutor {

    CreateProductResponse create(GenericApplication application) throws Exception;

    void fetchProductInformation(ProductType type, UUID productInstanceId,
            Map<FetchProductInformationParameterKey, Object> parameters);

    void refreshApplication(ProductType type, UUID applicationId, Map<RefreshApplicationParameterKey, Object> parameters)
            throws Exception;
}
