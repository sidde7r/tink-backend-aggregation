/*
 * Copyright 2011-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package se.tink.libraries.queue.sqs.auth;

import com.amazonaws.AmazonClientException;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Credentials provider implementation that loads credentials from the Amazon EC2 Instance Metadata
 * Service.
 *
 * <p>When using {@link RetryableInstanceProfileCredentialsProvider} with asynchronous refreshing it
 * is <b>strongly</b> recommended to explicitly call {@link #close()} to release the async thread.
 */
public class RetryableInstanceProfileCredentialsProvider
        implements AWSCredentialsProvider, Closeable {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RetryableInstanceProfileCredentialsProvider.class);

    /**
     * The wait time, after which the background thread initiates a refresh to load latest
     * credentials if needed.
     */
    private static final int ASYNC_REFRESH_INTERVAL_TIME_MINUTES = 1;

    /**
     * The default RetryableInstanceProfileCredentialsProvider that can be shared by multiple
     * CredentialsProvider instance threads to shrink the amount of requests to EC2 metadata
     * service.
     */
    private static final RetryableInstanceProfileCredentialsProvider INSTANCE =
            new RetryableInstanceProfileCredentialsProvider(false);

    private final RetryableEC2CredentialsFetcher credentialsFetcher;

    /** The executor service used for refreshing the credentials in the background. */
    private ScheduledExecutorService executor;

    private volatile boolean shouldRefresh = false;

    /** @deprecated for the singleton method {@link #getInstance()}. */
    @Deprecated
    public RetryableInstanceProfileCredentialsProvider() {
        this(false);
    }

    /**
     * Spins up a new thread to refresh the credentials asynchronously if refreshCredentialsAsync is
     * set to true, otherwise the credentials will be refreshed from the instance metadata service
     * synchronously,
     *
     * <p>It is <b>strongly</b> recommended to reuse instances of this credentials provider,
     * especially when async refreshing is used since a background thread is created.
     *
     * @param refreshCredentialsAsync true if credentials needs to be refreshed asynchronously else
     *     false.
     */
    public RetryableInstanceProfileCredentialsProvider(boolean refreshCredentialsAsync) {
        this(refreshCredentialsAsync, true);
    }

    /**
     * Spins up a new thread to refresh the credentials asynchronously.
     *
     * <p>It is <b>strongly</b> recommended to reuse instances of this credentials provider,
     * especially when async refreshing is used since a background thread is created.
     *
     * @param eagerlyRefreshCredentialsAsync when set to false will not attempt to refresh
     *     credentials asynchronously until after a call has been made to {@link #getCredentials()}
     *     - ensures that {@link RetryableEC2CredentialsFetcher#getCredentials()} is only hit when
     *     this CredentialProvider is actually required
     */
    public static RetryableInstanceProfileCredentialsProvider createAsyncRefreshingProvider(
            final boolean eagerlyRefreshCredentialsAsync) {
        return new RetryableInstanceProfileCredentialsProvider(
                true, eagerlyRefreshCredentialsAsync);
    }

    private RetryableInstanceProfileCredentialsProvider(
            boolean refreshCredentialsAsync, final boolean eagerlyRefreshCredentialsAsync) {

        credentialsFetcher =
                new RetryableEC2CredentialsFetcher(
                        new RetryableInstanceMetadataCredentialsEndpointProvider());

        if (!SDKGlobalConfiguration.isEc2MetadataDisabled()) {
            if (refreshCredentialsAsync) {
                executor =
                        Executors.newScheduledThreadPool(
                                1,
                                r -> {
                                    Thread t = Executors.defaultThreadFactory().newThread(r);
                                    t.setName("instance-profile-credentials-refresh");
                                    t.setDaemon(true);
                                    return t;
                                });
                executor.scheduleWithFixedDelay(
                        () -> {
                            try {
                                if (shouldRefresh) credentialsFetcher.getCredentials();
                            } catch (RuntimeException re) {
                                handleError(re);
                            }
                        },
                        0,
                        ASYNC_REFRESH_INTERVAL_TIME_MINUTES,
                        TimeUnit.MINUTES);
            }
        }
    }

    /**
     * Returns a singleton {@link RetryableInstanceProfileCredentialsProvider} that does not refresh
     * credentials asynchronously.
     *
     * <p>See {@link #RetryableInstanceProfileCredentialsProvider(boolean)} or {@link
     * #createAsyncRefreshingProvider(boolean)} for asynchronous credentials refreshing.
     */
    public static RetryableInstanceProfileCredentialsProvider getInstance() {
        return INSTANCE;
    }

    private void handleError(Throwable t) {
        refresh();
        LOGGER.error(t.getMessage(), t);
    }

    @Override
    protected void finalize() throws Throwable {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws AmazonClientException if {@link SDKGlobalConfiguration#isEc2MetadataDisabled()} is
     *     true
     */
    @Override
    public AWSCredentials getCredentials() {
        if (SDKGlobalConfiguration.isEc2MetadataDisabled()) {
            throw new AmazonClientException(
                    "AWS_EC2_METADATA_DISABLED is set to true, not loading credentials from EC2 Instance "
                            + "Metadata service");
        }
        AWSCredentials creds = credentialsFetcher.getCredentials();
        shouldRefresh = true;
        return creds;
    }

    @Override
    public void refresh() {
        if (credentialsFetcher != null) {
            credentialsFetcher.refresh();
        }
    }

    @Override
    public void close() throws IOException {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
