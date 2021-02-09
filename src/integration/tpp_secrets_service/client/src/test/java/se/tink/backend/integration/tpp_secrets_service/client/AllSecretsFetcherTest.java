package se.tink.backend.integration.tpp_secrets_service.client;

import io.grpc.Channel;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.secretservice.grpc.InternalSecretsServiceGrpc;

public class AllSecretsFetcherTest {

    private boolean enabled;
    private Channel channel;
    private InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;
    private AllSecretsFetcher allSecretsFetcher;
    private String financialInstitutionId;
    private String appId;
    private String clusterId;
    private String providerId;
    private String certId;

    @Rule public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void init() {
        enabled = true;
        channel = Mockito.mock(Channel.class);
        internalSecretsServiceStub = InternalSecretsServiceGrpc.newBlockingStub(channel);
        financialInstitutionId = "financialInstitutionId";
        appId = "appId";
        clusterId = "clusterId";
        providerId = "providerId";
        certId = "";
    }

    @Test
    public void shouldGetAllSecretsReturnOptionalEmptyWhenEnabledIsFalse() {
        // given
        enabled = false;
        allSecretsFetcher = new AllSecretsFetcher(enabled, internalSecretsServiceStub);

        // when
        Optional<SecretsEntityCore> resp =
                allSecretsFetcher.getAllSecrets(appId, clusterId, certId, providerId);

        // then
        Assert.assertFalse(resp.isPresent());
    }

    @Test
    public void shouldGetAllSecretsReturnOptionalEmptyWhenAppIdIsNull() {
        // given
        allSecretsFetcher = new AllSecretsFetcher(enabled, internalSecretsServiceStub);
        appId = null;

        // when
        Optional<SecretsEntityCore> resp =
                allSecretsFetcher.getAllSecrets(appId, clusterId, certId, providerId);

        // then
        Assert.assertFalse(resp.isPresent());
    }

    @Test
    public void shouldGetAllSecretsReturnOptionalEmptyWhenFinancialInstitutionIdIsNull() {
        // given
        allSecretsFetcher = new AllSecretsFetcher(enabled, internalSecretsServiceStub);

        // when
        Optional<SecretsEntityCore> resp =
                allSecretsFetcher.getAllSecrets(appId, clusterId, certId, null);

        // then
        Assert.assertFalse(resp.isPresent());
    }

    @Test
    public void shouldGetAllSecretsThrowNullPointerExceptionWhenClusterIdIsNull() {
        // given
        allSecretsFetcher = new AllSecretsFetcher(enabled, internalSecretsServiceStub);
        clusterId = null;
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("clusterId must not be null");

        // when
        Optional<SecretsEntityCore> resp =
                allSecretsFetcher.getAllSecrets(appId, clusterId, certId, providerId);

        // then, expected exception is thrown
    }
}
