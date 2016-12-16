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

package com.cdancy.etcd.rest.options;

import java.util.List;

import org.jclouds.json.SerializedNames;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class CreateUserOptions {

    public abstract String user();

    public abstract String password();

    public abstract List<String> roles();

    public abstract List<String> grant();

    public abstract List<String> revoke();

    CreateUserOptions() {
    }

    @SerializedNames({ "user", "password", "roles", "grant", "revoke" })
    public static CreateUserOptions create(String user, String password, List<String> roles, List<String> grant,
            List<String> revoke) {
        return new AutoValue_CreateUserOptions(user, password,
                roles != null ? ImmutableList.copyOf(roles) : ImmutableList.<String> of(),
                grant != null ? ImmutableList.copyOf(grant) : ImmutableList.<String> of(),
                revoke != null ? ImmutableList.copyOf(revoke) : ImmutableList.<String> of());
    }
}
