package se.tink.backend.main.transports;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.ConsentService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.consent.controllers.ConsentServiceController;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.core.exceptions.ConsentNotFoundException;
import se.tink.backend.consent.core.exceptions.ConsentRequestInvalid;
import se.tink.backend.consent.core.exceptions.InvalidChecksumException;
import se.tink.backend.consent.core.exceptions.UserConsentNotFoundException;
import se.tink.backend.consent.rpc.ConsentListResponse;
import se.tink.backend.consent.rpc.ConsentRequest;
import se.tink.backend.consent.rpc.UserConsentListResponse;
import se.tink.backend.core.User;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.LogUtils;

@Produces({ MediaType.APPLICATION_JSON, TinkMediaType.APPLICATION_PROTOBUF })
@Path("/api/v1/consents")
@Api(hidden = true, value = "Consent Service", description = "A service managing users's consents.")
public class ConsentServiceJerseyTransport implements ConsentService {
    private final ConsentServiceController consentServiceController;

    private static final LogUtils log = new LogUtils(ConsentServiceJerseyTransport.class);

    private static final ModelMapper mapper = new ModelMapper();

    static {
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.createTypeMap(User.class, se.tink.backend.consent.core.User.class);
    }

    private static se.tink.backend.consent.core.User map(User user) {
        return mapper.map(user, se.tink.backend.consent.core.User.class);
    }

    @Inject
    public ConsentServiceJerseyTransport(ConsentServiceController consentServiceController) {
        this.consentServiceController = consentServiceController;
    }

    @GET
    @Path("/available")
    @TeamOwnership(Team.PFM)
    @ApiOperation(hidden = true, value = "List all available consents that the user can accept or decline.")
    @Override
    public ConsentListResponse available(@Authenticated User user) {
        return new ConsentListResponse(consentServiceController.available(map(user)));
    }

    @GET
    @TeamOwnership(Team.PFM)
    @ApiOperation(hidden = true, value = "List all consents that a user has accepted or declined.")
    @Override
    public UserConsentListResponse list(@Authenticated User user) {
        return new UserConsentListResponse(consentServiceController.list(map(user)));
    }

    @POST
    @TeamOwnership(Team.PFM)
    @ApiOperation(hidden = true, value = "Accept or decline a consent.")
    @Override
    public UserConsent consent(@Authenticated User user, ConsentRequest request) {
        try {
            return consentServiceController.consent(map(user), request);
        } catch (ConsentRequestInvalid e) {
            log.error(user.getId(), e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (ConsentNotFoundException e) {
            log.error(user.getId(), e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (InvalidChecksumException e) {
            log.error(user.getId(), e);
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
    }

    @GET
    @Path("/{id}")
    @TeamOwnership(Team.PFM)
    @ApiOperation(hidden = true, value = "List details about a user consent.")
    @Override
    public UserConsent details(@Authenticated User user, @PathParam("id") String id) {
        try {
            return consentServiceController.details(map(user), id);
        } catch (UserConsentNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/describe/{key}")
    @TeamOwnership(Team.PFM)
    @ApiOperation(hidden = true, value = "Describe a consent. Will always return the latest available version.")
    @Override
    public Consent describe(@Authenticated User user, @PathParam("key") String key) {
        try {
            return consentServiceController.describe(map(user), key);
        } catch (ConsentNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public static ModelMapper getModelMapper() {
        return mapper;
    }
}
