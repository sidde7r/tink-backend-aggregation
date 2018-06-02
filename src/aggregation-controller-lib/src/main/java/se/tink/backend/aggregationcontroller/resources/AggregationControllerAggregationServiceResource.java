package se.tink.backend.aggregationcontroller.resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregationcontroller.client.AggregationServiceClient;
import se.tink.backend.aggregationcontroller.configuration.AggregationClusterConfiguration;
import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerAggregationService;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CreateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CreateProductRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.DeleteCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.KeepAliveRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ProductInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ReencryptionRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.RefreshApplicationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.RefreshInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.SupplementInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.TransferRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.UpdateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class AggregationControllerAggregationServiceResource implements AggregationControllerAggregationService {
    private final AggregationServiceClient serviceClient;
    private final boolean useAggregationCluster;

    @Inject
    public AggregationControllerAggregationServiceResource(AggregationServiceClient serviceClient,
            AggregationClusterConfiguration aggregationConfiguration) {
        this.serviceClient = serviceClient;
        this.useAggregationCluster = aggregationConfiguration.isEnabled();
    }

    private boolean shouldUseAggregationCluster(CredentialsRequest request) {
        // Determine if the aggregation cluster can be used by checking:
        // a) if the new field for sensitive data is set (meaning it was encrypted in the new cluster)
        // b) if the secretKey is NOT set (meaning it was not encrypted in the internal cluster)

        Credentials credentials = Preconditions.checkNotNull(request.getCredentials(),
                "Credentials were not set in a CredentialsRequest.");

        return useAggregationCluster &&
                (!Strings.isNullOrEmpty(credentials.getSensitiveDataSerialized()) ||
                Strings.isNullOrEmpty(credentials.getSecretKey()));
    }

    @Override
    public Credentials createCredentials(CreateCredentialsRequest request) {
        // Always use the new aggregation cluster for newly created credentials (if enabled)
        return serviceClient.createCredentials(useAggregationCluster, request);
    }

    @Override
    public Credentials reencryptCredentials(ReencryptionRequest request) {
        // This is a legacy command that will be removed/changed once all credentials are migrated to
        // the new method.
        return serviceClient.reencryptCredentials(false, request);
    }

    @Override
    public void deleteCredentials(DeleteCredentialsRequest request) {
        // There's nothing to delete in the aggregation cluster.
        serviceClient.deleteCredentials(false, request);
    }

    @Override
    public String ping() {
        return serviceClient.ping(useAggregationCluster);
    }

    @Override
    public void refreshInformation(RefreshInformationRequest request) throws Exception {
        serviceClient.refreshInformation(shouldUseAggregationCluster(request), request);
    }

    @Override
    public void transfer(TransferRequest request) throws Exception {
        serviceClient.transfer(shouldUseAggregationCluster(request), request);
    }

    @Override
    public void keepAlive(KeepAliveRequest request) throws Exception {
        serviceClient.keepAlive(shouldUseAggregationCluster(request), request);
    }

    @Override
    public Credentials updateCredentials(UpdateCredentialsRequest request) {
        // Always send update credentials to the new cluster since it will re-encrypt the credential (if enabled)
        return serviceClient.updateCredentials(useAggregationCluster, request);
    }

    @Override
    public void updateRateLimits(ChangeProviderRateLimitsRequest request) {
        // Send to both internally and aggregation cluster (if enabled)
        serviceClient.updateRateLimits(false, request);
        if (useAggregationCluster) {
            serviceClient.updateRateLimits(true, request);
        }
    }

    @Override
    public void createProduct(CreateProductRequest request) throws Exception {
        serviceClient.createProduct(shouldUseAggregationCluster(request), request);
    }

    @Override
    public void fetchProductInformation(ProductInformationRequest request) throws Exception {
        serviceClient.fetchProductInformation(shouldUseAggregationCluster(request), request);
    }

    @Override
    public void refreshApplication(RefreshApplicationRequest request) throws Exception {
        serviceClient.refreshApplication(shouldUseAggregationCluster(request), request);
    }

    @Override
    public void setSupplementalInformation(SupplementInformationRequest request) {
        // Send to both internally and aggregation cluster since we cannot determine if the credential is new or not
        // (if useAggregationCluster is enabled).
        serviceClient.setSupplementalInformation(false, request);
        if (useAggregationCluster) {
            serviceClient.setSupplementalInformation(true, request);
        }
    }

    @Override
    public Response migrateCredentials(MigrateCredentialsRequest request) {
        Credentials credentials = request.getCredentials();

        // If the credentials has this field it means that it already is migrated
        if (credentials.getSensitiveDataSerialized() != null) {
            return HttpResponseHelper.ok();
        }

        Credentials decryptedCredentials = serviceClient.migrateCredentialsDecrypt(
                new MigrateCredentialsDecryptRequest(request.getUser(), request.getProvider(), credentials));

        serviceClient.migrateCredentialsReencrypt(new MigrateCredentialsReencryptRequest(
                request.getUser(), request.getProvider(), decryptedCredentials));

        return HttpResponseHelper.ok();
    }
}
