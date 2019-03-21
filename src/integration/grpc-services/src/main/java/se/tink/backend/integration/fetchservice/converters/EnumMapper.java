package se.tink.backend.integration.fetchservice.converters;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.integration.api.models.IntegrationCredentials;
import se.tink.backend.integration.fetchservice.controller.Credentials;


public class EnumMapper {

    public static final Map<IntegrationCredentials.Type, Credentials.Type> CREDENTIALS_TYPE = new
            ImmutableMap.Builder<IntegrationCredentials.Type, Credentials.Type>()
            .put(IntegrationCredentials.Type.TYPE_PASSWORD, Credentials.Type.PASSWORD)
            .put(IntegrationCredentials.Type.TYPE_THIRD_PARTY_AUTHENTICATION, Credentials.Type.THIRD_PARTY_AUTHENTICATION)
            .put(IntegrationCredentials.Type.TYPE_KEYFOB, Credentials.Type.KEYFOB)
            .put(IntegrationCredentials.Type.UNRECOGNIZED, Credentials.Type.UNKNOWN)
            .put(IntegrationCredentials.Type.TYPE_UNKNOWN, Credentials.Type.UNKNOWN)
            .build();

}
