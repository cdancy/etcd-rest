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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.jclouds.rest.annotations.RequestFilters;

import com.cdancy.etcd.rest.domain.statistics.Leader;
import com.cdancy.etcd.rest.domain.statistics.Self;
import com.cdancy.etcd.rest.domain.statistics.Store;
import com.cdancy.etcd.rest.filters.EtcdAuthentication;

@RequestFilters(EtcdAuthentication.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/{jclouds.api-version}/stats")
public interface StatisticsApi {

    /**
     * Information on leader and entire cluster but only if WE are the leader.
     * 
     * @return instance of Leader
     */
    @Named("statistics:leader")
    @Path("/leader")
    @GET
    Leader leader();

    /**
     * Information on node we are currently pointing at.
     * 
     * @return Instance of Self
     */
    @Named("statistics:self")
    @Path("/self")
    @GET
    Self self();

    /**
     * Information about operations this node has handled.
     * 
     * @return instance of Store
     */
    @Named("statistics:store")
    @Path("/store")
    @GET
    Store store();
}
