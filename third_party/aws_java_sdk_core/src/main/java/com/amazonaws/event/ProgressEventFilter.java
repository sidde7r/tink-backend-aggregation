/*
 * Copyright 2014-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.event;

/**
 * An interface that filters the incoming events before passing them into the registered listeners.
 */
public interface ProgressEventFilter {

    /**
     * Returns the filtered event object that will be actually passed into the listeners. Returns
     * null if the event should be completely blocked.
     */
    public ProgressEvent filter(ProgressEvent progressEvent);
}
