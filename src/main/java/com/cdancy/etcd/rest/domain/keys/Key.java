/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cdancy.etcd.rest.domain.keys;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.cdancy.etcd.rest.error.ErrorMessage;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Key {

    @Nullable
    public abstract String action();

    @Nullable
    public abstract Node node();

    @Nullable
    public abstract Node prevNode();

    @Nullable
    public abstract ErrorMessage errorMessage();

    Key() {
    }

    @SerializedNames({ "action", "node", "prevNode", "errorMessage" })
    public static Key create(String action, Node node, Node prevNode, ErrorMessage errorMessage) {
        return new AutoValue_Key(action, node, prevNode, errorMessage);
    }
}
