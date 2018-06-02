package se.tink.backend.common.utils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Map;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.rpc.UserPropertiesBuilderCommand;
import se.tink.backend.core.User;
import se.tink.backend.core.UserOrigin;

public class TinkUserPropertiesBuilder extends UserPropertiesBuilder {
    @Inject
    public TinkUserPropertiesBuilder(
            CredentialsRepository credentialsRepository,
            UserStateRepository userStateRepository,
            UserDemographicsRepository userDemographicsRepository) {
        super(credentialsRepository, userStateRepository, userDemographicsRepository);
    }

    @Override
    public Map<String, Object> build(UserPropertiesBuilderCommand command) {
        final Map<String, Object> properties = Maps.newHashMap();
        populateClientProperties(properties, command.getUserAgent());
        populateUserProperties(properties, command.getUser());
        populateCredentialsProperties(properties, command.getUser());
        populateCategorizationProperties(properties, command.getUser().getId());
        populateUserDemographicsProperties(properties, command.getUser().getId());

        if (command.getUserOrigin().isPresent()) {
            populateUserOriginProperties(properties, command.getUserOrigin().get());
        }

        return properties;
    }

    @Override
    protected void populateUserProperties(Map<String, Object> properties, User user) {
        super.populateUserProperties(properties, user);
    }
}
