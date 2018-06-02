package se.tink.backend.api;

import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.Activity;
import se.tink.backend.core.User;
import se.tink.backend.rpc.ActivityQuery;
import se.tink.backend.rpc.ActivityQueryResponse;
import se.tink.backend.rpc.HtmlDetailsResponse;
import se.tink.backend.rpc.HtmlHeadResponse;
import se.tink.backend.rpc.ListHtmlResponse;

public interface ActivityService {
    ListHtmlResponse listHtml(User user, int offset, int limit, double screenWidth, double screenPpi);

    HtmlDetailsResponse activityDetails(User user, String id, double screenWidth, double screenPpi);

    HtmlHeadResponse htmlHead(User user);

    void feedback(User user, String id, String opinion);

    ActivityQueryResponse query(AuthenticatedUser authenticatedUser, ActivityQuery query);

    Activity get(AuthenticatedUser authenticatedUser, String key);
}
