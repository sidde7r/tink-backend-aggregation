package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.property.Property;
import se.tink.backend.core.property.PropertyEvent;
import se.tink.backend.core.property.PropertyStatus;
import se.tink.backend.main.controllers.exceptions.PropertyNotFoundException;
import se.tink.backend.rpc.properties.UpdatePropertyCommand;
import se.tink.backend.utils.guavaimpl.Predicates;

public class PropertyServiceController {
    private static final Function<Property, Property> CLEAN_ADDRESS = property -> {
        property.setAddress(PropertyUtils.cleanStreetAddress(property.getAddress()));
        return property;
    };

    private final PropertyRepository propertyRepository;
    
    @Inject
    public PropertyServiceController(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> list(User user) {
        return propertyRepository.findByUserId(user.getId())
                .stream()
                .filter(Predicates.ACTIVE_PROPERTY::apply)
                .map(CLEAN_ADDRESS)
                .collect(Collectors.toList());
    }

    public Property get(User user, String propertyId) throws PropertyNotFoundException {
        Property property = getProperty(user.getId(), propertyId);

        return CLEAN_ADDRESS.apply(property);
    }

    public Property update(UpdatePropertyCommand command) throws PropertyNotFoundException {
        Property property = getProperty(command.getUserId(), command.getPropertyId());

        property.setNumberOfRooms(command.getNumberOfRooms());
        property.setNumberOfSquareMeters(command.getNumberOfSquareMeters());

        return propertyRepository.save(property);
    }

    public Property deleteValuation(User user, String propertyId) throws PropertyNotFoundException {
        Property property = getProperty(user.getId(), propertyId);

        property.setMostRecentValuation(null);
        property.setBooliEstimateId(null);
        return propertyRepository.save(property);
    }

    private Property getProperty(String userId, String propertyId) throws PropertyNotFoundException {
        Property property = propertyRepository.findByUserIdAndId(userId, propertyId);

        if (property == null || Objects.equals(PropertyStatus.EXPIRED, property.getStatus())) {
            throw new PropertyNotFoundException();
        }

        return property;
    }

    public List<PropertyEvent> getEvents(User user) {
        List<PropertyEvent> dummyPropertyEvents = Lists.newArrayList();

        PropertyEvent event1 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1496065891), PropertyEvent.Type.VALUATION_INCREASE);
        Map<String, Object> event1Properties = new HashMap<String, Object>();
        event1Properties.put("currentValuation", 5000000);
        event1Properties.put("change", 30000);
        event1Properties.put("percentChange", 0.006036217303822937);
        event1.setProperties(event1Properties);
        dummyPropertyEvents.add(event1);

        PropertyEvent event2 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1494856291), PropertyEvent.Type.VALUATION_DECREASE);
        Map<String, Object> event2Properties = new HashMap<String, Object>();
        event2Properties.put("currentValuation", 4970000);
        event2Properties.put("change", 10000);
        event2Properties.put("percentChange", 0.002008032128514056);
        event2.setProperties(event2Properties);
        dummyPropertyEvents.add(event2);

        PropertyEvent event3 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1489585891), PropertyEvent.Type.VALUATION_INCREASE);
        Map<String, Object> event3Properties = new HashMap<String, Object>();
        event3Properties.put("currentValuation", 4980000);
        event3Properties.put("change", 480000);
        event3Properties.put("percentChange", 0.10666666666666667);
        event3.setProperties(event3Properties);
        dummyPropertyEvents.add(event3);

        PropertyEvent event4 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1488721891), PropertyEvent.Type.VALUATION_INCREASE);
        Map<String, Object> event4Properties = new HashMap<String, Object>();
        event4Properties.put("currentValuation", 4500000);
        event4Properties.put("change", 200000);
        event4Properties.put("percentChange", 0.046511627906976744);
        event4.setProperties(event4Properties);
        dummyPropertyEvents.add(event4);

        PropertyEvent event5 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1478721891), PropertyEvent.Type.VALUATION_INCREASE);
        Map<String, Object> event5Properties = new HashMap<String, Object>();
        event5Properties.put("currentValuation", 4300000);
        event5Properties.put("change", 100000);
        event5Properties.put("percentChange", 0.023809523809523808);
        event5.setProperties(event5Properties);
        dummyPropertyEvents.add(event5);

        PropertyEvent event6 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1468721891), PropertyEvent.Type.VALUATION_INCREASE);
        Map<String, Object> event6Properties = new HashMap<String, Object>();
        event6Properties.put("currentValuation", 4200000);
        event6Properties.put("change", 80000);
        event6Properties.put("percentChange", 0.019417475728155338);
        event6.setProperties(event6Properties);
        dummyPropertyEvents.add(event6);

        PropertyEvent event7 = new PropertyEvent("f687389130994f35839755537d78e46e", new Date(1458721891), PropertyEvent.Type.VALUATION_INCREASE);
        Map<String, Object> event7Properties = new HashMap<String, Object>();
        event7Properties.put("currentValuation", 4120000);
        event7Properties.put("change", 120000);
        event7Properties.put("percentChange", 0.02912621359223301);
        event7.setProperties(event7Properties);
        dummyPropertyEvents.add(event7);

        return dummyPropertyEvents;
    }
}
