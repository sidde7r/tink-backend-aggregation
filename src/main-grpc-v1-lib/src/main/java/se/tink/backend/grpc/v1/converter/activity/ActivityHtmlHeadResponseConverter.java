package se.tink.backend.grpc.v1.converter.activity;

import se.tink.backend.grpc.v1.converter.ConverterUtils;
import se.tink.backend.rpc.HtmlHeadResponse;
import se.tink.grpc.v1.rpc.ActivityHtmlHeadResponse;

public class ActivityHtmlHeadResponseConverter {
    public static ActivityHtmlHeadResponse convertFrom(HtmlHeadResponse response) {
        ActivityHtmlHeadResponse.Builder builder = ActivityHtmlHeadResponse.newBuilder();
        ConverterUtils.setIfPresent(response::getCss, builder::setCss);
        ConverterUtils.setIfPresent(response::getMetaData, builder::setMetadata);
        ConverterUtils.setIfPresent(response::getScripts, builder::setScripts);
        return builder.build();
    }
}
