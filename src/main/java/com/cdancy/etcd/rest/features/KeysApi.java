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
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.FormParams;
import org.jclouds.rest.annotations.QueryParams;

import com.cdancy.etcd.rest.domain.keys.Key;
import com.cdancy.etcd.rest.fallbacks.EtcdFallbacks.KeyOnAlreadyExists;
import com.cdancy.etcd.rest.fallbacks.EtcdFallbacks.KeyOnCompareFailed;
import com.cdancy.etcd.rest.fallbacks.EtcdFallbacks.KeyOnNonFound;

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

   @Named("keys:create-in-order")
   @POST
   @Path("/{key}")
   Key createInOrderKey(@PathParam("key") String key, @FormParam("value") String value);

   @Named("keys:create-in-order-with-options")
   @POST
   @Path("/{key}")
   Key createInOrderKey(@PathParam("key") String key, @FormParam("value") String value, @FormParam("ttl") int seconds);

   @Named("keys:list-in-order")
   @GET
   @QueryParams(keys = { "recursive", "sorted" }, values = { "true", "true" })
   @Path("/{key}")
   @Fallback(KeyOnNonFound.class)
   Key listInOrderKey(@PathParam("key") String key);

   @Named("keys:get")
   @GET
   @Path("/{key}")
   @Fallback(KeyOnNonFound.class)
   Key getKey(@PathParam("key") String key);

   @Named("keys:delete")
   @DELETE
   @Path("/{key}")
   @Fallback(KeyOnNonFound.class)
   Key deleteKey(@PathParam("key") String key);

   @Named("keys:wait")
   @GET
   @Path("/{key}")
   @QueryParams(keys = { "wait" }, values = { "true" })
   @Fallback(KeyOnNonFound.class)
   Key waitKey(@PathParam("key") String key);

   @Named("keys:wait-with-options")
   @GET
   @Path("/{key}")
   @QueryParams(keys = { "wait" }, values = { "true" })
   @Fallback(KeyOnNonFound.class)
   Key waitKey(@PathParam("key") String key, @QueryParam("waitIndex") int waitIndex);

   @Named("keys:compare-and-delete-value")
   @DELETE
   @Path("/{key}")
   @Fallback(KeyOnCompareFailed.class)
   Key compareAndDeleteKey(@PathParam("key") String key, @QueryParam("prevValue") String prevValue);

   @Named("keys:compare-and-delete-index")
   @DELETE
   @Path("/{key}")
   @Fallback(KeyOnCompareFailed.class)
   Key compareAndDeleteKey(@PathParam("key") String key, @QueryParam("prevIndex") int prevIndex);

   @Named("keys:compare-and-swap-value")
   @PUT
   @Path("/{key}")
   @Fallback(KeyOnCompareFailed.class)
   Key compareAndSwapKeyValue(@PathParam("key") String key, @QueryParam("prevValue") String prevValue,
         @FormParam("value") String value);

   @Named("keys:compare-and-swap-index")
   @PUT
   @Path("/{key}")
   @Fallback(KeyOnCompareFailed.class)
   Key compareAndSwapKeyIndex(@PathParam("key") String key, @QueryParam("prevIndex") int prevIndex,
         @FormParam("value") String value);

   @Named("keys:dir-create")
   @PUT
   @FormParams(keys = { "dir" }, values = { "true" })
   @Path("/{dir}")
   @Fallback(KeyOnAlreadyExists.class)
   Key createDir(@PathParam("dir") String dir);

   @Named("keys:dir-create-with-options")
   @PUT
   @FormParams(keys = { "dir" }, values = { "true" })
   @Path("/{dir}")
   @Fallback(KeyOnAlreadyExists.class)
   Key createDir(@PathParam("dir") String dir, @FormParam("ttl") int seconds);

   @Named("keys:dir-list")
   @GET
   @Path("/{dir}/")
   @Fallback(KeyOnNonFound.class)
   Key listDir(@PathParam("dir") String dir, @QueryParam("recursive") boolean recursive);

   @Named("keys:dir-delete")
   @DELETE
   @Path("/{dir}/")
   @QueryParams(keys = { "recursive" }, values = { "true" })
   @Fallback(KeyOnNonFound.class)
   Key deleteDir(@PathParam("dir") String dir);
}
