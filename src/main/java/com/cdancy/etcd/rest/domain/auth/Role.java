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

package com.cdancy.etcd.rest.domain.auth;

import org.jclouds.javax.annotation.Nullable;
import org.jclouds.json.SerializedNames;

import com.cdancy.etcd.rest.error.ErrorMessage;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Role {

   public abstract String role();

   @Nullable
   public abstract Permission permissions();

   @Nullable
   public abstract Grant grant();

   @Nullable
   public abstract Revoke revoke();

   @Nullable
   public abstract ErrorMessage errorMessage();

   Role() {
   }

   @SerializedNames({ "role", "permissions", "grant", "revoke", "errorMessage" })
   public static Role create(String role, Permission permissions, Grant grant, Revoke revoke,
         ErrorMessage errorMessage) {
      return new AutoValue_Role(role, permissions, grant, revoke, errorMessage);
   }
}
