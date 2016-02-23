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

package com.github.etcd.rest.features;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.QueryParams;

import com.github.etcd.rest.domain.keys.Key;
import com.github.etcd.rest.fallbacks.EtcdFallbacks.NullOnKeyNonFoundAnd404;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/{jclouds.api-version}/keys")
public interface KeysApi {

   @Named("keys:create")
   @PUT
   @Path("/{key}")
   Key createKey(@PathParam("key") String key, @FormParam("value") String value);

   @Named("keys:create-with-options")
   @PUT
   @Path("/{key}")
   Key createKey(@PathParam("key") String key, @FormParam("value") String value, @FormParam("ttl") int seconds);

   @Named("keys:get")
   @GET
   @Path("/{key}")
   @Fallback(NullOnKeyNonFoundAnd404.class)
   Key getKey(@PathParam("key") String key);

   @Named("keys:delete")
   @DELETE
   @Path("/{key}")
   @Fallback(NullOnKeyNonFoundAnd404.class)
   Key deleteKey(@PathParam("key") String key);

   @Named("keys:wait")
   @GET
   @Path("/{key}")
   @QueryParams(keys = { "wait" }, values = { "true" })
   @Fallback(NullOnKeyNonFoundAnd404.class)
   Key waitKey(@PathParam("key") String key);

   @Named("keys:wait-with-options")
   @GET
   @Path("/{key}")
   @QueryParams(keys = { "wait" }, values = { "true" })
   @Fallback(NullOnKeyNonFoundAnd404.class)
   Key waitKey(@PathParam("key") String key, @QueryParam("waitIndex") int waitIndex);
}
