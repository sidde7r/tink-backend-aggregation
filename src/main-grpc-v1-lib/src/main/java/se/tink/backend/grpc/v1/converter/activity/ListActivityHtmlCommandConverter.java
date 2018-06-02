package se.tink.backend.grpc.v1.converter.activity;

import java.util.List;
import se.tink.backend.core.Activity;
import se.tink.backend.rpc.ListActivityHtmlCommand;
import se.tink.grpc.v1.rpc.ListActivityHtmlRequest;

public class ListActivityHtmlCommandConverter {
    public static ListActivityHtmlCommand convertFrom(ListActivityHtmlRequest request, List<Activity> activities, String userAgent) {
        ListActivityHtmlCommand command = new ListActivityHtmlCommand();
        command.setActivityList(activities);
        command.setUserAgent(userAgent);
        command.setLimit(request.getLimit());
        command.setOffset(request.getOffset());
        command.setScreenPpi(request.getScreenPpi());
        command.setScreenWidthl(request.getScreenWidth());
        return command;
    }
}
