package se.tink.backend.grpc.v1.converter.activity;

import se.tink.backend.rpc.ActivityQuery;
import se.tink.grpc.v1.rpc.ListActivityHtmlRequest;

public class ActivityQueryConverter {
    public static ActivityQuery convertFrom(ListActivityHtmlRequest request) {
        ActivityQuery activityQuery = new ActivityQuery();
        activityQuery.setOffset(request.getOffset());
        activityQuery.setLimit(request.getLimit());
        return activityQuery;
    }
}
