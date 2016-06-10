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
import com.cdancy.etcd.rest.domain.auth.User;
import com.cdancy.etcd.rest.domain.keys.Key;
import com.cdancy.etcd.rest.domain.members.Member;
import com.cdancy.etcd.rest.error.ErrorMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class EtcdFallbacks {

    private static final JsonParser PARSER = new JsonParser();

    private EtcdFallbacks() {
    }

    public static final class FalseOn503 implements Fallback<Boolean> {
        public Boolean createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null
                    && throwable.getMessage().contains("{\"health\": \"false\"}")
                    && returnValueOnCodeOrNull(throwable, true, equalTo(503)) != null) {
                return Boolean.FALSE;
            }
            throw propagate(throwable);
        }
    }

    public static final class MemberOnIllegalRequest implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null && throwable.getMessage().contains("message")) {
                return createMemberFromErrorMessage(throwable.getMessage());
            }
            throw propagate(throwable);
        }
    }

    public static final class KeyOnAlreadyExists implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null && throwable.getMessage().contains("Not a file")) {
                return createKeyFromErrorMessage(throwable.getMessage());
            }
            throw propagate(throwable);
        }
    }

    public static final class KeyOnNonFound implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null && throwable.getMessage().contains("Key not found")) {
                return createKeyFromErrorMessage(throwable.getMessage());
            }
            throw propagate(throwable);
        }
    }

    public static final class AuthStateOnNoRootUserOrAlreadyEnabled implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null) {
                AuthState authState = createAuthStateFromErrorMessage(throwable.getMessage());
                if (authState != null) {
                    return authState;
                }
            }
            throw propagate(throwable);
        }
    }

    public static final class KeyOnCompareFailed implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null && (throwable.getMessage().contains("Compare failed")
                    || throwable.getMessage().contains("Key already exists"))) {
                return createKeyFromErrorMessage(throwable.getMessage());
            }
            throw propagate(throwable);
        }
    }

    public static final class RoleOnAlreadyExists implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null && (throwable.getMessage().contains("already exists"))) {
                Role role = createRoleFromErrorMessage(throwable.getMessage());
                if (role != null) {
                    return role;
                }
            }
            throw propagate(throwable);
        }
    }

    public static final class UserOnAlreadyExists implements Fallback<Object> {
        public Object createOrPropagate(Throwable throwable) throws Exception {
            if (checkNotNull(throwable, "throwable") != null && (throwable.getMessage().contains("already exists"))) {
                User user = createUserFromErrorMessage(throwable.getMessage());
                if (user != null) {
                    return user;
                }
            }
            throw propagate(throwable);
        }
    }

    /**
     * Create a User instance from the returned Error message.
     * 
     * @param message
     *            error message from etcd
     * @return User instance
     */
    public static User createUserFromErrorMessage(String message) {
        JsonElement element = PARSER.parse(message);
        JsonObject object = element.getAsJsonObject();
        ErrorMessage error = ErrorMessage.create(-1, object.get("message").getAsString(), null, -1);

        Pattern pattern = Pattern.compile(".*User (.+) already exists.*");
        Matcher matcher = pattern.matcher(error.message());
        if (matcher.find() && matcher.groupCount() == 1) {
            return User.create(matcher.group(1), null, error);
        } else {
            return null;
        }
    }

    /**
     * Create a Role instance from the returned Error message.
     * 
     * @param message
     *            error message from etcd
     * @return Role instance
     */
    public static Role createRoleFromErrorMessage(String message) {
        JsonElement element = PARSER.parse(message);
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

    /**
     * Create a Auth instance from the returned Error message.
     * 
     * @param message
     *            error message from etcd
     * @return Auth instance
     */
    public static AuthState createAuthStateFromErrorMessage(String message) {
        if (message.contains("auth: No root user available")) {
            JsonElement element = PARSER.parse(message);
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

    /**
     * Create a Key instance from the returned Error message.
     * 
     * @param message
     *            error message from etcd
     * @return Key instance
     */
    public static Key createKeyFromErrorMessage(String message) {
        JsonElement element = PARSER.parse(message);
        JsonObject object = element.getAsJsonObject();
        ErrorMessage error = ErrorMessage.create(object.get("errorCode").getAsInt(),
                object.get("message").getAsString(), object.get("cause").getAsString(), object.get("index").getAsInt());
        return Key.create(null, null, null, error);
    }

    /**
     * Create a Member instance from the returned Error message.
     * 
     * @param message
     *            error message from etcd
     * @return Member instance
     */
    public static Member createMemberFromErrorMessage(String message) {
        JsonElement element = PARSER.parse(message);
        JsonObject object = element.getAsJsonObject();
        ErrorMessage error = ErrorMessage.create(-1, object.get("message").getAsString(), null, -1);
        return Member.create(null, null, null, null, error);
    }
}
