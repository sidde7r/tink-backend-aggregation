package se.tink.backend.aggregation.log;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class TagMdcLogger {

    public static final String TAG_MDC_KEY = "tag";

    public static void info(List<Tag> tags, Logger log, String msg) {
        setTags(tags);
        log.info(msg);
        removeTags();
    }

    static void setTags(List<Tag> tags) {
        String tagsValue = tags.stream().map(Tag::getValue).collect(Collectors.joining(" "));
        MDC.put(TAG_MDC_KEY, tagsValue);
    }

    static void removeTags() {
        MDC.remove(TAG_MDC_KEY);
    }
}
