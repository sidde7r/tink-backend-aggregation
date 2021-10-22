package se.tink.backend.aggregation.storage.logs.handlers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import java.net.URL;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.AgentDebugLogBucket;

@RunWith(JUnitParamsRunner.class)
public class AgentDebugLogS3StorageHandlerTest {

    private static final String AAP_BUCKET_NAME = "aap_bucket";
    private static final String JSON_BUCKET_NAME = "json_bucket";

    private S3StorageConfiguration s3StorageConfiguration;
    private S3ClientFactory s3ClientFactory;
    private AmazonS3 s3Client;

    private AgentHttpLogsS3StorageHandler logStorageHandler;

    @Before
    public void setup() {
        s3StorageConfiguration = mock(S3StorageConfiguration.class);

        s3ClientFactory = mock(S3ClientFactory.class);
        s3Client = mock(AmazonS3.class);
        when(s3ClientFactory.createAWSClient()).thenReturn(s3Client);

        recreateStorageHandler();
    }

    private void recreateStorageHandler() {
        logStorageHandler =
                new AgentHttpLogsS3StorageHandler(s3StorageConfiguration, s3ClientFactory);
    }

    @Test
    @Parameters(method = "invalid_s3_configurations")
    public void should_not_be_enabled_when_s3_config_is_not_enabled_or_invalid(
            S3StorageConfiguration storageConfiguration) {
        // given
        s3StorageConfiguration = storageConfiguration;
        recreateStorageHandler();

        // when
        boolean isEnabled = logStorageHandler.isEnabled();

        // then
        assertThat(isEnabled).isFalse();
    }

    @SuppressWarnings("unused")
    private static Object[] invalid_s3_configurations() {
        S3StorageConfiguration valid = validS3Configuration();

        S3StorageConfiguration disabled = valid.toBuilder().enabled(false).build();
        S3StorageConfiguration missingUrl = valid.toBuilder().url(null).build();
        S3StorageConfiguration missingRegion = valid.toBuilder().region(null).build();
        S3StorageConfiguration missingAapBucket =
                valid.toBuilder().agentHttpAapLogsBucketName(null).build();
        S3StorageConfiguration missingJsonBucket =
                valid.toBuilder().agentHttpJsonLogsBucketName(null).build();

        return Stream.of(
                        null,
                        disabled,
                        missingUrl,
                        missingRegion,
                        missingAapBucket,
                        missingJsonBucket)
                .map(config -> new Object[] {config})
                .toArray();
    }

    private static S3StorageConfiguration validS3Configuration() {
        return S3StorageConfiguration.builder()
                .enabled(true)
                .url("sample_url")
                .region("sample_region")
                .agentHttpAapLogsBucketName(AAP_BUCKET_NAME)
                .agentHttpJsonLogsBucketName(JSON_BUCKET_NAME)
                .build();
    }

    @Test
    public void should_be_enabled_when_s3_config_is_valid() {
        // given
        s3StorageConfiguration = validS3Configuration();
        recreateStorageHandler();

        // when
        boolean isEnabled = logStorageHandler.isEnabled();

        // then
        assertThat(isEnabled).isTrue();
    }

    @Test
    @Parameters(method = "all_buckets")
    public void should_throw_exception_when_trying_to_use_disabled_storage(
            AgentDebugLogBucket bucket) {
        // given
        s3StorageConfiguration = validS3Configuration().toBuilder().enabled(false).build();
        recreateStorageHandler();

        // when
        Throwable throwable = catchThrowable(() -> logStorageHandler.storeLog("", "", bucket));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid attempt to use S3 storage - not enabled");
    }

    @SuppressWarnings("unused")
    private static Object[] all_buckets() {
        return Stream.of(AgentDebugLogBucket.values()).toArray();
    }

    @Test
    @Parameters(method = "blank_strings")
    public void should_throw_exception_when_file_path_is_blank(String blankFilePath) {
        // given
        s3StorageConfiguration = validS3Configuration().toBuilder().enabled(false).build();
        recreateStorageHandler();

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                logStorageHandler.storeLog(
                                        "some content",
                                        blankFilePath,
                                        AgentDebugLogBucket.AAP_FORMAT_LOGS));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid attempt to use S3 storage - not enabled");
    }

    @SuppressWarnings("unused")
    private static Object[] blank_strings() {
        return Stream.of(null, "  ").toArray();
    }

    @Test
    @SneakyThrows
    @Parameters(method = "all_buckets_with_their_expected_configuration_name")
    public void should_store_logs_in_correct_bucket(
            AgentDebugLogBucket bucket, String expectedBucketName) {
        // given
        s3StorageConfiguration = validS3Configuration();
        recreateStorageHandler();

        when(s3Client.getUrl(any(), any())).thenReturn(new URL("https://aws.com/some/path"));

        // when
        String storageDescription =
                logStorageHandler.storeLog("some content", "some/path/to/file.log", bucket);

        // then
        assertThat(storageDescription)
                .isEqualTo(
                        format(
                                "AWS CLI: s3://%s/some/path/to/file.log"
                                        + "\n"
                                        + "AWS HTTP: https://aws.com/some/path",
                                expectedBucketName));

        verify(s3Client).putObject(expectedBucketName, "some/path/to/file.log", "some content");
        verify(s3Client).getUrl(expectedBucketName, "some/path/to/file.log");
        verifyNoMoreInteractions(s3Client);
    }

    @SuppressWarnings("unused")
    private Object[] all_buckets_with_their_expected_configuration_name() {
        return new Object[] {
            new Object[] {AgentDebugLogBucket.AAP_FORMAT_LOGS, AAP_BUCKET_NAME},
            new Object[] {AgentDebugLogBucket.JSON_FORMAT_LOGS, JSON_BUCKET_NAME}
        };
    }

    @Test
    @SneakyThrows
    public void should_allow_to_save_empty_content() {
        // given
        s3StorageConfiguration = validS3Configuration();
        recreateStorageHandler();

        when(s3Client.getUrl(any(), any())).thenReturn(new URL("https://aws.com/some/path123"));

        // when
        String storageDescription =
                logStorageHandler.storeLog(
                        "", "some/path/to/file123.log", AgentDebugLogBucket.AAP_FORMAT_LOGS);

        // then
        assertThat(storageDescription)
                .isEqualTo(
                        format(
                                "AWS CLI: s3://%s/some/path/to/file123.log"
                                        + "\n"
                                        + "AWS HTTP: https://aws.com/some/path123",
                                AAP_BUCKET_NAME));

        verify(s3Client).putObject(AAP_BUCKET_NAME, "some/path/to/file123.log", "");
        verify(s3Client).getUrl(AAP_BUCKET_NAME, "some/path/to/file123.log");
        verifyNoMoreInteractions(s3Client);
    }
}
