package se.tink.backend.aggregation.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class TagMdcLoggerTest {

    @Test
    public void log() {
        // given
        String msg = "test message";
        Logger log = mock(Logger.class);
        List<Tag> tagList = Collections.singletonList(Tag.CREDENTIALS);

        // when
        TagMdcLogger.info(tagList, log, msg);

        // then
        assertThat(MDC.get(TagMdcLogger.TAG_MDC_KEY)).isNull();
        verify(log).info(msg);
    }

    @Test
    public void setTags() {
        // given
        List<Tag> tagList = Arrays.asList(Tag.CREDENTIALS, Tag.AUTHENTICATION);

        // when
        TagMdcLogger.setTags(tagList);

        // then
        assertThat(MDC.get(TagMdcLogger.TAG_MDC_KEY)).isEqualTo("CREDENTIALS AUTHENTICATION");
    }

    @Test
    public void removeTags() {
        // given
        List<Tag> tagList = Arrays.asList(Tag.CREDENTIALS, Tag.AUTHENTICATION);

        // when
        TagMdcLogger.setTags(tagList);
        TagMdcLogger.removeTags();

        // then
        assertThat(MDC.get(TagMdcLogger.TAG_MDC_KEY)).isNull();
    }
}
