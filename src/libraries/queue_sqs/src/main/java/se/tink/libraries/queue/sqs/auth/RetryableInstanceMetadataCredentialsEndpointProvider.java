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

import com.amazonaws.SdkClientException;
import com.amazonaws.internal.CredentialsEndpointProvider;
import com.amazonaws.internal.EC2CredentialsUtils;
import com.amazonaws.retry.internal.CredentialsEndpointRetryPolicy;
import com.amazonaws.util.EC2MetadataUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RetryableInstanceMetadataCredentialsEndpointProvider
        extends CredentialsEndpointProvider {

    @Override
    public URI getCredentialsEndpoint() throws URISyntaxException, IOException {
        String host = EC2MetadataUtils.getHostAddressForEC2MetadataService();

        String securityCredentialsList =
                EC2CredentialsUtils.getInstance()
                        .readResource(
                                new URI(host + EC2MetadataUtils.SECURITY_CREDENTIALS_RESOURCE),
                                getRetryPolicy(),
                                getHeaders());
        String[] securityCredentials = securityCredentialsList.trim().split("\n");
        if (securityCredentials.length == 0) {
            throw new SdkClientException("Unable to load credentials path");
        }

        return new URI(
                host + EC2MetadataUtils.SECURITY_CREDENTIALS_RESOURCE + securityCredentials[0]);
    }

    @Override
    public CredentialsEndpointRetryPolicy getRetryPolicy() {
        return RetryableInstanceMetadataCredentialsEndpointRetryPolicy.getInstance();
    }
}
