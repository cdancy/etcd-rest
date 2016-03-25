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
import com.cdancy.etcd.rest.domain.auth.KeyValue;
import com.cdancy.etcd.rest.domain.auth.Permission;
import com.cdancy.etcd.rest.domain.auth.Role;
import com.google.common.collect.Lists;

@Test(groups = "live", testName = "RolesApiLiveTest", singleThreaded = true)
public class RolesApiLiveTest extends BaseEtcdApiLiveTest {

   private String roleName;

   @BeforeClass
   public void init() {
      roleName = randomString();
   }

   @Test
   public void testCreateRole() {
      Permission permission = Permission.create(KeyValue.create(Lists.newArrayList("*"), Lists.newArrayList("*")));
      Role createRole = Role.create(roleName, permission, null, null, null);
      Role role = api().create(roleName, createRole);
      assertNotNull(role);
      assertTrue(role.role().equals(roleName));
      assertNotNull(role.permissions());
      assertNotNull(role.permissions().kv().read().contains("*"));
      assertNotNull(role.permissions().kv().write().contains("*"));
   }

   @Test(dependsOnMethods = "testCreateRole")
   public void testCreateRoleWithExistingUser() {
      Permission permission = Permission.create(KeyValue.create(Lists.newArrayList("*"), Lists.newArrayList("*")));
      Role createRole = Role.create(roleName, permission, null, null, null);
      Role role = api().create(roleName, createRole);
      assertNotNull(role);
      assertTrue(role.role().equals(roleName));
      assertNotNull(role.errorMessage());
      assertTrue(role.errorMessage().message().contains("already exists"));
   }

   @Test(dependsOnMethods = "testCreateRoleWithExistingUser")
   public void testRoleDetails() {
      Role role = api().get(roleName);
      assertNotNull(role);
      assertTrue(role.role().equals(roleName));
      assertNotNull(role.permissions());
      assertNotNull(role.permissions().kv().read().contains("*"));
      assertNotNull(role.permissions().kv().write().contains("*"));
   }

   @Test(dependsOnMethods = "testRoleDetails")
   public void testListRoles() {
      List<Role> roles = api().list();
      assertNotNull(roles);
      assertTrue(roles.size() > 0);
      boolean found = false;
      for (Role role : roles) {
         if (role.role().equals(roleName)) {
            found = true;
            break;
         }
      }
      assertTrue(found);
   }

   @Test(dependsOnMethods = "testListRoles")
   public void testDeleteRole() {
      boolean success = api().delete(roleName);
      assertTrue(success);
   }

   @Test
   public void testRoleDetailsNotFound() {
      Role role = api().get(randomString());
      assertNull(role);
   }

   @Test
   public void testDeleteRoleNotFound() {
      boolean success = api().delete(randomString());
      assertFalse(success);
   }

   @AfterClass
   public void finalize() {
      api().delete(roleName);
   }

   private RolesApi api() {
      return api.rolesApi();
   }
}
