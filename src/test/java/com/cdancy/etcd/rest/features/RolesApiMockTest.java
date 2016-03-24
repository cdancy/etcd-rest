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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.cdancy.etcd.rest.EtcdApi;
import com.cdancy.etcd.rest.EtcdApiMetadata;
import com.cdancy.etcd.rest.domain.auth.KeyValue;
import com.cdancy.etcd.rest.domain.auth.Permission;
import com.cdancy.etcd.rest.domain.auth.Role;
import com.cdancy.etcd.rest.internal.BaseEtcdMockTest;
import com.google.common.collect.Lists;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

/**
 * Mock tests for the {@link com.cdancy.etcd.rest.features.RolesApi} class.
 */
@Test(groups = "unit", testName = "RolesApiMockTest")
public class RolesApiMockTest extends BaseEtcdMockTest {

   public void testCreateRole() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-roles-create.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         Permission permission = Permission.create(KeyValue.create(Lists.newArrayList("*"), Lists.newArrayList("*")));
         Role createRole = Role.create("rkt", permission, null, null, null);
         Role roles = api.create("rkt", createRole);
         assertNotNull(roles);
         assertTrue(roles.role().equals("rkt"));
         assertNotNull(roles.permissions());
         assertNotNull(roles.permissions().kv().read().contains("*"));
         assertNotNull(roles.permissions().kv().write().contains("*"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles/rkt");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCreateRoleAlreadyExists() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/auth-roles-create-exists.json")).setResponseCode(409));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         Permission permission = Permission.create(KeyValue.create(Lists.newArrayList("*"), Lists.newArrayList("*")));
         Role createRole = Role.create("rkt", permission, null, null, null);
         Role roles = api.create("rkt", createRole);
         assertNotNull(roles);
         assertTrue(roles.role().equals("rkt"));
         assertNotNull(roles.errorMessage());
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles/rkt");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testListRoles() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-roles-list.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         List<String> roles = api.list();
         assertNotNull(roles);
         assertTrue(roles.size() == 3);
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testGetRoleDetails() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-roles-details.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         Role roles = api.get("fleet");
         assertNotNull(roles);
         assertTrue(roles.role().equals("fleet"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles/fleet");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testGetRoleDetailsNotFound() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         Role roles = api.get("random");
         assertNull(roles);
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles/random");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteRole() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         boolean success = api.delete("random");
         assertTrue(success);
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles/random");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteRoleNotFound() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      RolesApi api = etcdApi.rolesApi();
      try {
         boolean success = api.delete("random");
         assertFalse(success);
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/auth/roles/random");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }
}
