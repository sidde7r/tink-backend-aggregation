package se.tink.backend.main.transports;

import com.google.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.api.FollowService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.main.controllers.FollowServiceController;
import se.tink.backend.rpc.FollowItemListResponse;

@Path("/api/v1/follow")
public class FollowServiceJerseyTransport implements FollowService {

    @Context
    private HttpHeaders headers;

    private final FollowServiceController followServiceController;

    @Inject
    public FollowServiceJerseyTransport(FollowServiceController followServiceController) {
        this.followServiceController = followServiceController;
    }

    @Override
    public FollowItem create(AuthenticatedUser authenticatedUser, FollowItem createFollowItem) {
        try {
            return followServiceController
                    .create(authenticatedUser.getUser(), createFollowItem, RequestHeaderUtils.getRemoteIp(headers));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (DuplicateException e) {
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    @Override
    public List<FollowItem> create(AuthenticatedUser authenticatedUser, List<FollowItem> createFollowItems) {
        try {
            return followServiceController
                    .create(authenticatedUser.getUser(), createFollowItems, RequestHeaderUtils.getRemoteIp(headers));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (DuplicateException e) {
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    @Override
    public void delete(AuthenticatedUser authenticatedUser, String id) {
        try {
            followServiceController.delete(authenticatedUser.getUser(), id, RequestHeaderUtils.getRemoteIp(headers));
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public FollowItem get(AuthenticatedUser authenticatedUser, String id, String period) {
        try {
            return followServiceController.get(authenticatedUser.getUser(), id, period);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public List<FollowItem> list(AuthenticatedUser authenticatedUser, String period) {
        return followServiceController.list(authenticatedUser.getUser(), false, period);
    }

    @Override
    public FollowItemListResponse getFollowList(AuthenticatedUser authenticatedUser, String period) {
        FollowItemListResponse response = new FollowItemListResponse();
        response.setFollowItems(list(authenticatedUser, period));
        return response;
    }

    @Override
    public List<FollowItem> suggestByType(final AuthenticatedUser authenticatedUser, FollowTypes type) {
        return followServiceController
                .suggestByType(authenticatedUser.getUser(), type, RequestHeaderUtils.getUserAgent(headers));
    }

    @Override
    public FollowItem suggestByTypeAndCriteria(AuthenticatedUser authenticatedUser, FollowTypes type, String filter) {
        try {
            return followServiceController.suggestByTypeAndCriteria(authenticatedUser.getUser(), type, filter);
        } catch (DuplicateException e) {
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    @Override
    public FollowItem update(AuthenticatedUser authenticatedUser, String id, FollowItem updateFollowItem) {
        try {
            return followServiceController
                    .update(authenticatedUser.getUser(), id, updateFollowItem, RequestHeaderUtils.getRemoteIp(headers));
        } catch (DuplicateException e) {
            throw new WebApplicationException(Status.CONFLICT);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
}
