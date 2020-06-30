package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationOverride;

public class DanskebankConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonProperty @Secret private String organizationId;
    @JsonProperty @Secret private String clientId;
    @JsonProperty @Secret private String softwareStatementAssertion;
    @JsonProperty @Secret private String softwareId;

    @Override
    public ProviderConfiguration getProviderConfiguration() {
        Preconditions.checkState(!Strings.isNullOrEmpty(clientId), "ClientId is null or empty.");
        return new ProviderConfiguration(organizationId, new ClientInfo(clientId, ""));
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertions() {
        return new SoftwareStatementAssertion(softwareStatementAssertion, softwareId);
    }

    @Override
    public Optional<TlsConfigurationOverride> getTlsConfigurationOverride() {
        return Optional.empty();
    }

    @Override
    public Optional<JwtSigner> getSignerOverride() {
        return Optional.empty();
    }
}
