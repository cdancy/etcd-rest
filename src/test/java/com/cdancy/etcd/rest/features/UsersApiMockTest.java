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
import com.cdancy.etcd.rest.domain.auth.User;
import com.cdancy.etcd.rest.domain.auth.UserDetails;
import com.cdancy.etcd.rest.internal.BaseEtcdMockTest;
import com.cdancy.etcd.rest.options.CreateUserOptions;
import com.google.common.collect.Lists;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

/**
 * Mock tests for the {@link com.cdancy.etcd.rest.features.UsersApi} class.
 */
@Test(groups = "unit", testName = "UsersApiMockTest")
public class UsersApiMockTest extends BaseEtcdMockTest {

   public void testCreateUser() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-users-create.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         CreateUserOptions options = CreateUserOptions.create("alice", "world", Lists.newArrayList("role1", "role2"),
               null, null);
         User user = api.create("alice", options);
         assertNotNull(user);
         assertTrue(user.user().equals("alice"));
         assertNotNull(user.roles().size() == 2);
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/auth/users/alice");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCreateUserAlreadyExists() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/auth-users-create-exists.json")).setResponseCode(409));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         CreateUserOptions options = CreateUserOptions.create("rkt", "world", Lists.newArrayList("role1", "role2"),
               null, null);
         User user = api.create("rkt", options);
         assertNotNull(user);
         assertTrue(user.user().equals("rkt"));
         assertNotNull(user.errorMessage());
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/auth/users/rkt");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testListUsers() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-users-list.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         List<UserDetails> users = api.list();
         assertNotNull(users);
         assertTrue(users.size() == 2);
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/users");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testGetUserDetails() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-users-details.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         UserDetails user = api.get("alice");
         assertNotNull(user);
         assertTrue(user.user().equals("alice"));
         assertTrue(user.roles().size() == 2);
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/users/alice");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testGetUserDetailsNotFound() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         UserDetails user = api.get("random");
         assertNull(user);
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/users/random");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteUser() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         boolean success = api.delete("random");
         assertTrue(success);
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/auth/users/random");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteUserNotFound() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      UsersApi api = etcdApi.usersApi();
      try {
         boolean success = api.delete("random");
         assertFalse(success);
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/auth/users/random");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }
}
