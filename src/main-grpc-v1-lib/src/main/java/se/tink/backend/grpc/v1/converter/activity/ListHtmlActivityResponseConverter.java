package se.tink.backend.grpc.v1.converter.activity;

import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.rpc.ListHtmlResponse;
import se.tink.grpc.v1.rpc.ListActivityHtmlResponse;

public class ListHtmlActivityResponseConverter {
    public static ListActivityHtmlResponse convertFrom(ListHtmlResponse response) {
        ListActivityHtmlResponse.Builder builder = ListActivityHtmlResponse.newBuilder()
                .setNextPageOffset(response.getNextPageOffset());
        ConverterUtils.setIfPresent(response::getHtmlPage, builder::setHtmlPage);
        ConverterUtils.setIfPresent(response::getActivityKeys, builder::addAllActivityKeys);
        ConverterUtils
                .setIfPresent(response::getFeedActivityIdentifiersList, builder::addAllFeedActivityIdentifiersList);
        return builder.build();
    }
}
