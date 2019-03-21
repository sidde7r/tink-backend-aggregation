package se.tink.backend.integration.fetchservice.converters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.integration.api.models.IntegrationCredentials;
import se.tink.backend.integration.fetchservice.controller.Type;

public class EnumMapper {

    public static final Map<IntegrationCredentials.Type, Type> CREDENTIALS_TYPE = new
            ImmutableMap.Builder<IntegrationCredentials.Type, Type>()
            .put(IntegrationCredentials.Type.TYPE_PASSWORD, Type.PASSWORD)
            .put(IntegrationCredentials.Type.TYPE_THIRD_PARTY_AUTHENTICATION, Type.THIRD_PARTY_AUTHENTICATION)
            .put(IntegrationCredentials.Type.TYPE_KEYFOB, Type.KEYFOB)
            .put(IntegrationCredentials.Type.UNRECOGNIZED, Type.UNKNOWN)
            .put(IntegrationCredentials.Type.TYPE_UNKNOWN, Type.UNKNOWN)
            .build();

}
