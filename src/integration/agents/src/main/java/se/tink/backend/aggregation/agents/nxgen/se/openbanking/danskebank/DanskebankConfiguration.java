package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInject;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaInt;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaString;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls.TlsConfigurationOverride;

public class DanskebankConfiguration implements UkOpenBankingClientConfigurationAdapter {

    @JsonProperty(required = true)
    @JsonSchemaDescription("0015800000jf7AeAAI (Danske Bank’s OrgId)")
    @JsonSchemaTitle("Organization ID")
    @JsonSchemaInject(strings = {@JsonSchemaString(path = "pattern", value = "0015800000jf7AeAAI")})
    @JsonSchemaExamples("0015800000jf7AeAAI")
    @Secret
    private String organizationId;

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty(required = true)
    @JsonSchemaDescription("JSON of the software statement, encoded in base64")
    @JsonSchemaTitle("Software Statement Assertion")
    @JsonSchemaExamples("eyJpc3MiOiJQU0RTR...IndlYiJ9")
    @JsonSchemaInject(ints = {@JsonSchemaInt(path = "minLength", value = 100)})
    @Secret
    private String softwareStatementAssertion;

    @JsonProperty(required = true)
    @JsonSchemaDescription("The one you used in software statement")
    @JsonSchemaTitle("Software ID")
    @JsonSchemaExamples("12345678-3edf-1234-9a89-123294950c15")
    @JsonSchemaInject(
            strings = {
                @JsonSchemaString(
                        path = "pattern",
                        value =
                                "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")
            })
    @Secret
    private String softwareId;

    @JsonProperty @Secret private String tokenEndpointAuthSigningAlg;

    @Override
    public ProviderConfiguration getProviderConfiguration() {
        Preconditions.checkState(!Strings.isNullOrEmpty(clientId), "ClientId is null or empty.");
        return new ProviderConfiguration(organizationId, new ClientInfo(clientId, ""));
    }

    @Override
    public SoftwareStatementAssertion getSoftwareStatementAssertion() {
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
