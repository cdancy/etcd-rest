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

package com.cdancy.etcd.rest.features;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SelectJson;

import com.cdancy.etcd.rest.domain.auth.AuthState;
import com.cdancy.etcd.rest.fallbacks.EtcdFallbacks.AuthStateOnNoRootUserOrAlreadyEnabled;
import com.cdancy.etcd.rest.filters.EtcdAuthentication;
import com.cdancy.etcd.rest.parsers.DisabledAuthState;
import com.cdancy.etcd.rest.parsers.EnabledAuthState;

@RequestFilters(EtcdAuthentication.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/{jclouds.api-version}/auth")
public interface AuthApi {

    @Named("auth:is-enabled")
    @Path("/enable")
    @SelectJson("enabled")
    @GET
    boolean isEnabled();

    @Named("auth:enable")
    @Path("/enable")
    @Fallback(AuthStateOnNoRootUserOrAlreadyEnabled.class)
    @ResponseParser(EnabledAuthState.class)
    @PUT
    AuthState enable();

    @Named("auth:disable")
    @Path("/enable")
    @Fallback(AuthStateOnNoRootUserOrAlreadyEnabled.class)
    @ResponseParser(DisabledAuthState.class)
    @DELETE
    AuthState disable();
}
