package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EngagementResponse extends Response<EngagementResponseBody> {}
