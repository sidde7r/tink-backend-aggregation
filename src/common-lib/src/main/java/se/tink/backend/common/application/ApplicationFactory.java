package se.tink.backend.common.application;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.property.Property;

public class ApplicationFactory {

    @VisibleForTesting
    static final Comparator<Property> PROPERTY_BY_DATE_ASC = Comparator.comparing(Property::getCreated,
            Comparator.nullsFirst(Comparator.naturalOrder()));

    private final PropertyRepository propertyRepository;

    public ApplicationFactory(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public Application buildFromType(User user, ApplicationType type) {

        ApplicationStatus status = new ApplicationStatus();
        status.setKey(ApplicationStatusKey.CREATED);
        status.setUpdated(new Date());

        Application application = new Application();
        application.setType(type);
        application.setCreated(new Date());
        application.setForms(Lists.<ApplicationForm>newArrayList());
        application.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        application.setStatus(status);
        application.setProperties(getApplicationProperties(user, type));

        return application;
    }

    private HashMap<ApplicationPropertyKey, Object> getApplicationProperties(User user, ApplicationType type) {
        HashMap<ApplicationPropertyKey, Object> applicationProperties = Maps.newHashMap();

        switch (type) {
        case RESIDENCE_VALUATION:
            String propertyId = getPropertyId(user);
            if (!Strings.isNullOrEmpty(propertyId)) {
                applicationProperties.put(ApplicationPropertyKey.PROPERTY_ID, propertyId);
            }
            break;
        default:
            break;
        }

        return applicationProperties;
    }

    private String getPropertyId(User user) {
        List<Property> properties = propertyRepository.findByUserId(user.getId());

        if (properties.isEmpty()) {
            return null;
        }

        Property mostRecentProperty = properties.stream().max(PROPERTY_BY_DATE_ASC).get();

        return mostRecentProperty.getId();
    }
}
