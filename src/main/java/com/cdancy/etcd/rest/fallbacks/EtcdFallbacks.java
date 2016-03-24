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
package com.cdancy.etcd.rest.fallbacks;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Throwables.propagate;
import static org.jclouds.http.HttpUtils.returnValueOnCodeOrNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jclouds.Fallback;

import com.cdancy.etcd.rest.domain.auth.AuthState;
import com.cdancy.etcd.rest.domain.auth.Role;
import com.cdancy.etcd.rest.domain.keys.Key;
import com.cdancy.etcd.rest.domain.members.Member;
import com.cdancy.etcd.rest.error.ErrorMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class EtcdFallbacks {

   private static final JsonParser parser = new JsonParser();

   public static final class FalseOn503 implements Fallback<Boolean> {
      public Boolean createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null && t.getMessage().contains("{\"health\": \"false\"}")
               && returnValueOnCodeOrNull(t, true, equalTo(503)) != null) {
            return Boolean.FALSE;
         }
         throw propagate(t);
      }
   }

   public static final class MemberOnIllegalRequest implements Fallback<Object> {
      public Object createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null && t.getMessage().contains("message")) {
            return createMemberFromErrorMessage(t.getMessage());
         }
         throw propagate(t);
      }
   }

   public static final class KeyOnAlreadyExists implements Fallback<Object> {
      public Object createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null && t.getMessage().contains("Not a file")) {
            return createKeyFromErrorMessage(t.getMessage());
         }
         throw propagate(t);
      }
   }

   public static final class KeyOnNonFound implements Fallback<Object> {
      public Object createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null && t.getMessage().contains("Key not found")) {
            return createKeyFromErrorMessage(t.getMessage());
         }
         throw propagate(t);
      }
   }

   public static final class AuthStateOnNoRootUserOrAlreadyEnabled implements Fallback<Object> {
      public Object createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null) {
            AuthState authState = createAuthStateFromErrorMessage(t.getMessage());
            if (authState != null) {
               return authState;
            }
         }
         throw propagate(t);
      }
   }

   public static final class KeyOnCompareFailed implements Fallback<Object> {
      public Object createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null
               && (t.getMessage().contains("Compare failed") || t.getMessage().contains("Key already exists"))) {
            return createKeyFromErrorMessage(t.getMessage());
         }
         throw propagate(t);
      }
   }

   public static final class RoleOnAlreadyExists implements Fallback<Object> {
      public Object createOrPropagate(Throwable t) throws Exception {
         if (checkNotNull(t, "throwable") != null && (t.getMessage().contains("already exists"))) {
            Role role = createRoleFromErrorMessage(t.getMessage());
            if (role != null) {
               return role;
            }
         }
         throw propagate(t);
      }
   }

   public static Role createRoleFromErrorMessage(String message) {
      JsonElement element = parser.parse(message);
      JsonObject object = element.getAsJsonObject();
      ErrorMessage error = ErrorMessage.create(-1, object.get("message").getAsString(), null, -1);

      Pattern pattern = Pattern.compile(".*Role (.+) already exists.*");
      Matcher matcher = pattern.matcher(error.message());
      if (matcher.find() && matcher.groupCount() == 1) {
         return Role.create(matcher.group(1), null, null, null, error);
      } else {
         return null;
      }
   }

   public static AuthState createAuthStateFromErrorMessage(String message) {
      if (message.contains("auth: No root user available")) {
         JsonElement element = parser.parse(message);
         JsonObject object = element.getAsJsonObject();
         ErrorMessage error = ErrorMessage.create(-1, object.get("message").getAsString(), null, -1);
         return AuthState.create(false, error);
      } else if (message.contains("auth: already disabled")) {
         return AuthState.create(false, null);
      } else if (message.contains("auth: already enabled")) {
         return AuthState.create(true, null);
      } else {
         return null;
      }
   }

   public static Key createKeyFromErrorMessage(String message) {
      JsonElement element = parser.parse(message);
      JsonObject object = element.getAsJsonObject();
      ErrorMessage error = ErrorMessage.create(object.get("errorCode").getAsInt(), object.get("message").getAsString(),
            object.get("cause").getAsString(), object.get("index").getAsInt());
      return Key.create(null, null, null, error);
   }

   public static Member createMemberFromErrorMessage(String message) {
      JsonElement element = parser.parse(message);
      JsonObject object = element.getAsJsonObject();
      ErrorMessage error = ErrorMessage.create(-1, object.get("message").getAsString(), null, -1);
      return Member.create(null, null, null, null, error);
   }
}
