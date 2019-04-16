package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Strings;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.libraries.pair.Pair;

@JsonObject
public class OpenBankProjectConfiguration implements ClientConfiguration {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(OpenBankProjectConfiguration.class);

    @JsonIgnore
    private static final LogTag MISSING_CONFIG = LogTag.from("OpenBankProject_MISSING_CONFIG");

    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;
    @JsonProperty private String baseUrl;
    @JsonProperty private String bankId;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBankId() {
        return bankId;
    }

    public boolean isValid() {
        final List<String> list =
                Stream.of(
                                new Pair<>("clientSecret", clientSecret),
                                new Pair<>("clientId", clientId),
                                new Pair<>("bankId", bankId),
                                new Pair<>("baseUrl", baseUrl),
                                new Pair<>("redirectUrl", redirectUrl))
                        .filter(item -> Strings.isNullOrEmpty(item.second))
                        .map(item -> item.first)
                        .collect(Collectors.toList());

        return list.stream()
                .findAny()
                .map(
                        item -> {
                            logger.error(
                                    "{} - Missing OpenBankProject configuration: {}",
                                    MISSING_CONFIG,
                                    Arrays.toString(list.toArray()));
                            return false;
                        })
                .orElse(true);
    }
}
