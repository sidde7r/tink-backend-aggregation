/*
 * Copyright (c) 2016. Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.auth;

import com.amazonaws.SignableRequest;

/**
 * Used to explicitly indicate that a {@link com.amazonaws.auth.Signer} can gracefully handle
 * credentials being null when calling {@link com.amazonaws.auth.Signer#sign(SignableRequest,
 * AWSCredentials)}
 */
public interface CanHandleNullCredentials {}
