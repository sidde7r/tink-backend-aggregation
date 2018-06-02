package se.tink.backend.main.transports;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import se.tink.backend.api.PropertyService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.property.ListPropertiesResponse;
import se.tink.backend.core.property.Property;
import se.tink.backend.core.property.PropertyEventsResponse;
import se.tink.backend.core.property.PropertyResponse;
import se.tink.backend.core.property.UpdatePropertyRequest;
import se.tink.backend.main.controllers.PropertyServiceController;
import se.tink.backend.main.controllers.exceptions.PropertyNotFoundException;
import se.tink.backend.rpc.properties.UpdatePropertyCommand;

public class PropertyServiceJerseyTransport implements PropertyService {
    private final PropertyServiceController propertyServiceController;

    @Context
    private HttpHeaders headers;

    @Inject
    public PropertyServiceJerseyTransport(PropertyServiceController propertyServiceController) {
        this.propertyServiceController = propertyServiceController;
    }

    @Override
    public ListPropertiesResponse list(AuthenticatedUser authenticatedUser) {
        List<Property> properties = propertyServiceController.list(authenticatedUser.getUser());

        // Apps before iOS 2.5.15 doesn't support having empty loan account ids.
        if (!isIos2515OrLater()) {
            properties = properties.stream()
                    .filter(p -> !p.getLoanAccountIds().isEmpty())
                    .collect(Collectors.toList());
        }

        return new ListPropertiesResponse(properties);
    }

    @Override
    public PropertyResponse get(AuthenticatedUser authenticatedUser, String propertyId) {
        try {
            return new PropertyResponse(propertyServiceController.get(authenticatedUser.getUser(), propertyId));
        } catch (PropertyNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    public PropertyResponse update(AuthenticatedUser authenticatedUser, String propertyId, UpdatePropertyRequest request) {
        try {
            UpdatePropertyCommand command = UpdatePropertyCommand.builder()
                    .withUser(authenticatedUser.getUser().getId())
                    .withPropertyId(propertyId)
                    .withNumberOfRooms(request.getProperty().getNumberOfRooms())
                    .withNumberOfSquareMeters(request.getProperty().getNumberOfSquareMeters())
                    .build();

            return new PropertyResponse(propertyServiceController.update(command));
        } catch (PropertyNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    public PropertyResponse deleteValuation(AuthenticatedUser authenticatedUser, String propertyId) {
        try {
            return new PropertyResponse(
                    propertyServiceController.deleteValuation(authenticatedUser.getUser(), propertyId));
        } catch (PropertyNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    public PropertyEventsResponse getEvents(AuthenticatedUser authenticatedUser) {
        return new PropertyEventsResponse(propertyServiceController.getEvents(authenticatedUser.getUser()));
    }

    private boolean isIos2515OrLater() {
        TinkUserAgent tinkUserAgent = new TinkUserAgent(RequestHeaderUtils.getUserAgent(headers));
        return tinkUserAgent.hasValidVersion("2.5.15", null);
    }

}
