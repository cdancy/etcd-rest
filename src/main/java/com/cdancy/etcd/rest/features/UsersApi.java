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

import java.util.List;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.FalseOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.binders.BindToJsonPayload;

import com.cdancy.etcd.rest.domain.auth.User;
import com.cdancy.etcd.rest.domain.auth.UserDetails;
import com.cdancy.etcd.rest.fallbacks.EtcdFallbacks.UserOnAlreadyExists;
import com.cdancy.etcd.rest.filters.EtcdAuthentication;
import com.cdancy.etcd.rest.options.CreateUserOptions;

@RequestFilters(EtcdAuthentication.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/{jclouds.api-version}/auth/users")
public interface UsersApi {

   @Named("auth-user:create")
   @Path("/{user}")
   @Fallback(UserOnAlreadyExists.class)
   @PUT
   User create(@PathParam("user") String user, @BinderParam(BindToJsonPayload.class) CreateUserOptions options);

   @Named("auth-user:list")
   @SelectJson("users")
   @GET
   List<UserDetails> list();

   @Named("auth-user:get")
   @Path("/{user}")
   @Fallback(NullOnNotFoundOr404.class)
   @GET
   UserDetails get(@PathParam("user") String user);

   @Named("auth-user:delete")
   @Path("/{user}")
   @Fallback(FalseOnNotFoundOr404.class)
   @DELETE
   boolean delete(@PathParam("user") String user);
}
