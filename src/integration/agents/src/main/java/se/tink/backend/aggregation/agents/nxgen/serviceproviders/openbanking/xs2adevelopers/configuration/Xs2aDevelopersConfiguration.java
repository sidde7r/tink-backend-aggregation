package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
// This class is meant to be empty and exist, it is needed for secrets & their schema
// validation/generation. There are no true secrets utilized by this agent.
public class Xs2aDevelopersConfiguration implements ClientConfiguration {}
