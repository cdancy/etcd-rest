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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cdancy.etcd.rest.BaseEtcdApiLiveTest;
import com.cdancy.etcd.rest.domain.auth.User;
import com.cdancy.etcd.rest.options.CreateUserOptions;
import com.google.common.collect.Lists;

@Test(groups = "live", testName = "UsersApiLiveTest", singleThreaded = true)
public class UsersApiLiveTest extends BaseEtcdApiLiveTest {

   private String userName;

   @BeforeClass
   public void init() {
      userName = randomString();
   }

   @Test
   public void testCreateUser() {
      CreateUserOptions options = CreateUserOptions.create(userName, "world", Lists.newArrayList("fish", "bear"), null,
            null);
      User user = api().create(userName, options);
      assertNotNull(user);
      assertTrue(user.user().equals(userName));
      assertNotNull(user.roles().size() == 0);
      assertNull(user.errorMessage());
   }

   @Test(dependsOnMethods = "testCreateUser")
   public void testCreateUserWithExistingRoles() {
      CreateUserOptions options = CreateUserOptions.create(userName, "world", Lists.newArrayList("hello", "world"),
            null, null);
      User user = api().create(userName, options);
      assertNotNull(user);
      assertTrue(user.user().equals(userName));
      assertNotNull(user.errorMessage());
   }

   @Test(dependsOnMethods = "testCreateUserWithExistingRoles")
   public void testUserDetails() {
      User user = api().get(userName);
      assertNotNull(user);
      assertTrue(user.user().equals(userName));
      assertNotNull(user.roles().size() == 0);
      assertNull(user.errorMessage());
   }

   @Test(dependsOnMethods = "testUserDetails")
   public void testListUsers() {
      List<String> users = api().list();
      assertNotNull(users);
      assertTrue(users.size() > 0);
      assertTrue(users.contains(userName));
   }

   @Test(dependsOnMethods = "testListUsers")
   public void testDeleteUser() {
      boolean success = api().delete(userName);
      assertTrue(success);
   }

   @Test
   public void testUserDetailsNotFound() {
      User user = api().get(randomString());
      assertNull(user);
   }

   @Test
   public void testDeleteUserNotFound() {
      boolean success = api().delete(randomString());
      assertFalse(success);
   }

   @AfterClass
   public void finalize() {
      api().delete(userName);
   }

   private UsersApi api() {
      return api.usersApi();
   }
}
