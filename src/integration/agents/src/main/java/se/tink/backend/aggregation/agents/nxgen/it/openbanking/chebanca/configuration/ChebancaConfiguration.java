package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChebancaConfiguration implements ClientConfiguration {
    @Secret
    @JsonProperty
    @JsonSchemaTitle("Application Id for manual refresh")
    @JsonSchemaExamples("TEST_TPP_APP_01")
    @JsonSchemaDescription(
            "Application Id registered in CheBanca portal with OAuth2 grant type Authorization Code")
    private String manualRefreshApplicationId;

    @Secret
    @JsonProperty
    @JsonSchemaTitle("Client Id")
    @JsonSchemaDescription("Client Id of application for background refresh")
    @JsonSchemaExamples("555d5513cb123456789050b90f06a18b21234567890a9cce977c5513734d3f41")
    private String manualRefreshClientId;

    @SensitiveSecret
    @JsonProperty
    @JsonSchemaTitle("Client Secret of application for manual refresh")
    @JsonSchemaDescription(
            "The client secret is a secret known only to the application and the authorization server.")
    @JsonSchemaExamples("555d5513cb123456789050b90f06a18b21234567890a9cce977c5513734d3f41")
    private String manualRefreshClientSecret;

    @Secret
    @JsonProperty
    @JsonSchemaTitle("Application Id for background refresh")
    @JsonSchemaExamples("TEST_TPP_APP_02")
    @JsonSchemaDescription(
            "Application Id registered in CheBanca portal with OAuth2 grant type Client Credentials")
    private String autoRefreshApplicationId;

    @Secret
    @JsonProperty
    @JsonSchemaTitle("Client Id")
    @JsonSchemaDescription("Client Id of application for background refresh")
    @JsonSchemaExamples("555d5513cb123456789050b90f06a18b21234567890a9cce977c5513734d3f41")
    private String autoRefreshClientId;

    @SensitiveSecret
    @JsonProperty
    @JsonSchemaDescription(
            "The client secret is a secret known only to the application and the authorization server.")
    @JsonSchemaTitle("Client Secret of application for background refresh")
    @JsonSchemaExamples("555d5513cb123456789050b90f06a18b21234567890a9cce977c5513734d3f41")
    private String autoRefreshClientSecret;
}
