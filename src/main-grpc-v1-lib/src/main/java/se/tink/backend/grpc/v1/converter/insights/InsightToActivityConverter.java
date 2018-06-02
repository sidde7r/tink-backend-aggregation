package se.tink.backend.grpc.v1.converter.insights;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.rpc.ListHtmlResponse;
import se.tink.grpc.v1.rpc.ListActivityHtmlRequest;

public class InsightToActivityConverter  {

    public static ListHtmlResponse getActivityResponse(ListActivityHtmlRequest request,
            List<String> htmls) {
        int screenWidth = 0;
        if (request.getScreenWidth()== 0) {
            screenWidth = 480;
        }

        int screenPpi = 0;
        if (request.getScreenPpi()== 0) {
            screenPpi = 160;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"page\">");
        htmls.forEach(html -> builder.append(html));
        builder.append("</div>");



        ListHtmlResponse response = new ListHtmlResponse();
        response.setHtmlPage(builder.toString());
        response.setNextPageOffset(request.getOffset() + htmls.size());
        response.setActivityKeys(Lists.newArrayList());
        response.setFeedActivityIdentifiersList(Lists.newArrayList());
        return response;

    }
}
