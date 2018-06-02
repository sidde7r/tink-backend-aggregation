package se.tink.backend.main.transports;

import com.google.inject.Inject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import se.tink.api.annotations.Team;
import se.tink.api.annotations.TeamOwnership;
import se.tink.backend.api.CategoryService;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.core.Category;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.main.controllers.CategoryController;

@Path("/api/v1/categories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Category Service",
        description = "List of categories with parent/child relationships using their id and parent fields.")
public class CategoryJerseyTransport implements CategoryService {
    private final CategoryController categoryController;

    @Inject
    public CategoryJerseyTransport(CategoryController categoryController) {
        this.categoryController = categoryController;
    }

    @GET
    @ApiOperation(value = "List categories for a given locale.",
            notes = "Returns all categories for the given locale. The locale is either taken from the authenticated "
                    + "user or from a query parameter, if no user is authenticated. If no user and no query parameter "
                    + "is given, a default locale is used.",
            response = Category.class,
            responseContainer = "List")
    @TeamOwnership(Team.DATA)
    @Override
    public List<Category> list(@Authenticated(required = false, scopes = OAuth2AuthorizationScopeTypes.USER_READ) @ApiParam(hidden = true)
                               User user,
                               @QueryParam("locale")
                               @ApiParam(value = "The locale for which to fetch categories.", example = "sv_SE")
                               String locale) {
        return categoryController.list(
                Optional.ofNullable(user).map(User::getLocale).map(Optional::of)
                        .orElse(Optional.ofNullable(locale)));
    }
}
