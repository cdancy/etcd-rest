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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cdancy.etcd.rest.BaseEtcdApiLiveTest;
import com.cdancy.etcd.rest.domain.keys.Key;
import com.google.common.base.Throwables;

@Test(groups = "live", testName = "KeysApiLiveTest", singleThreaded = true)
public class KeysApiLiveTest extends BaseEtcdApiLiveTest {

   private String key;
   private String value;
   private String dir;
   private String dirWithTTL;
   private String inOrderKey;
   private String inOrderKeyValueOne;
   private String inOrderKeyValueTwo;
   private String compareAndDeleteKeyValue;
   private String compareAndDeleteKeyValueFail;
   private String compareAndDeleteKeyIndex;
   private String compareAndDeleteKeyIndexFail;
   private String compareAndSwapKeyValue;
   private String compareAndSwapKeyValueFail;
   private String compareAndSwapKeyIndex;
   private String compareAndSwapKeyIndexFail;

   @BeforeClass
   protected void init() {
      key = randomString();
      value = randomString();
      dir = randomString();
      dirWithTTL = randomString();
      inOrderKey = randomString();
      inOrderKeyValueOne = randomString();
      inOrderKeyValueTwo = randomString();
      compareAndDeleteKeyValue = randomString();
      compareAndDeleteKeyValueFail = randomString();
      compareAndDeleteKeyIndex = randomString();
      compareAndDeleteKeyIndexFail = randomString();
      compareAndSwapKeyValue = randomString();
      compareAndSwapKeyValueFail = randomString();
      compareAndSwapKeyIndex = randomString();
      compareAndSwapKeyIndexFail = randomString();
   }

   @Test
   public void testCreateKeyWithTTL() {
      String localKey = randomString();
      String localValue = randomString();
      Key createdKey = api().createKey(localKey, localValue, 1);
      assertNotNull(createdKey);
      assertNotNull(createdKey.node().expiration());
      assertTrue(createdKey.action().equals("set"));
      assertTrue(createdKey.node().ttl() == 1);

      try {
         Thread.sleep(3000);
      } catch (InterruptedException e) {
         Throwables.propagate(e);
      }

      createdKey = api().getKey(localKey);
      assertNotNull(createdKey);
      assertTrue(createdKey.message().equals("Key not found"));
   }

   @Test
   public void testCreateInOrderKeys() {
      Key createdKeyOne = api().createInOrderKey(inOrderKey, inOrderKeyValueOne);
      assertNotNull(createdKeyOne);
      assertTrue(createdKeyOne.action().equals("create"));
      assertTrue(createdKeyOne.node().value().equals(inOrderKeyValueOne));

      Key createdKeyTwo = api().createInOrderKey(inOrderKey, inOrderKeyValueTwo);
      assertNotNull(createdKeyTwo);
      assertTrue(createdKeyTwo.action().equals("create"));
      assertTrue(createdKeyTwo.node().value().equals(inOrderKeyValueTwo));
   }

   @Test(dependsOnMethods = "testCreateInOrderKeys")
   public void testListInOrderKeys() {
      Key listKeys = api().listInOrderKey(inOrderKey);
      assertNotNull(listKeys);
      assertTrue(listKeys.action().equals("get"));
      assertTrue(listKeys.node().nodes().size() == 2);
      assertTrue(listKeys.node().key().equals("/" + inOrderKey));
      assertTrue(listKeys.node().dir());
      assertTrue(listKeys.node().nodes().get(0).value().equals(inOrderKeyValueOne));
      assertTrue(listKeys.node().nodes().get(1).value().equals(inOrderKeyValueTwo));
   }

   @Test
   public void testCreateKey() {
      Key createdKey = api().createKey(key, value);
      assertNotNull(createdKey);
      assertTrue(createdKey.action().equals("set"));
      assertTrue(createdKey.node().value().equals(value));
   }

   @Test(dependsOnMethods = "testCreateKey")
   public void testGetKey() {
      Key getKey = api().getKey(key);
      assertNotNull(getKey);
      assertTrue(getKey.action().equals("get"));
      assertTrue(getKey.node().value().equals(value));
   }

   @Test(dependsOnMethods = "testGetKey", alwaysRun = true)
   public void testDeleteKey() {
      Key deletedKey = api().deleteKey(key);
      assertNotNull(deletedKey);
      assertTrue(deletedKey.action().equals("delete"));
      assertTrue(deletedKey.prevNode().value().equals(value));
   }

   @Test
   public void testGetNonExistentKey() {
      Key deletedKey = api().getKey(randomString());
      assertNotNull(deletedKey);
      assertTrue(deletedKey.message().equals("Key not found"));
   }

   @Test
   public void testDeleteNonExistentKey() {
      Key deletedKey = api().deleteKey(randomString());
      assertNotNull(deletedKey);
      assertTrue(deletedKey.message().equals("Key not found"));
   }

   @Test
   public void testCompareAndeDeleteKeyValue() {
      String compareValue = "world";
      Key createdKey = api().createKey(compareAndDeleteKeyValue, compareValue);
      assertNotNull(createdKey);

      Key deletedKey = api().compareAndDeleteKey(compareAndDeleteKeyValue, compareValue);
      assertNotNull(deletedKey);
      assertTrue(deletedKey.action().equals("compareAndDelete"));
      assertTrue(deletedKey.prevNode().value().equals(compareValue));
   }

   @Test
   public void testCompareAndeDeleteKeyValueWithWrongValue() {
      String compareValue = "world";
      Key createdKey = api().createKey(compareAndDeleteKeyValueFail, compareValue);
      assertNotNull(createdKey);

      Key failedComparison = api().compareAndDeleteKey(compareAndDeleteKeyValueFail, "random");
      assertNotNull(failedComparison);
      assertTrue(failedComparison.cause().equals("[random != world]"));
   }

   @Test
   public void testCompareAndeDeleteKeyIndex() {
      String compareValue = "world";
      Key createdKey = api().createKey(compareAndDeleteKeyIndex, compareValue);
      assertNotNull(createdKey);

      Key deletedKey = api().compareAndDeleteKey(compareAndDeleteKeyIndex, createdKey.node().createdIndex());
      assertNotNull(deletedKey);
      assertTrue(deletedKey.action().equals("compareAndDelete"));
      assertTrue(deletedKey.prevNode().value().equals(compareValue));
   }

   @Test
   public void testCompareAndeDeleteKeyValueWithWrongIndex() {
      String compareValue = "world";
      Key createdKey = api().createKey(compareAndDeleteKeyIndexFail, compareValue);
      assertNotNull(createdKey);

      int correctIndex = createdKey.node().createdIndex();
      int wrongIndex = createdKey.node().createdIndex() + 1;
      Key failedComparison = api().compareAndDeleteKey(compareAndDeleteKeyIndexFail, wrongIndex);
      assertNotNull(failedComparison);
      assertTrue(failedComparison.cause().equals("[" + wrongIndex + " != " + correctIndex + "]"));
   }

   @Test
   public void testCompareAndeSwapKeyValue() {
      String compareValue = "hello";
      Key createdKey = api().createKey(compareAndSwapKeyValue, compareValue);
      assertNotNull(createdKey);
      assertTrue(createdKey.errorCode() == 0);

      Key key = api().compareAndSwapKeyValue(compareAndSwapKeyValue, compareValue, "world");
      assertNotNull(key);
      assertTrue(key.action().equals("compareAndSwap"));
      assertTrue(key.prevNode().value().equals("hello"));
      assertTrue(key.node().value().equals("world"));
   }

   @Test
   public void testCompareAndeSwapKeyValueFail() {
      String compareValue = "hello";
      Key createdKey = api().createKey(compareAndSwapKeyValueFail, compareValue);
      assertNotNull(createdKey);
      assertTrue(createdKey.errorCode() == 0);

      Key key = api().compareAndSwapKeyValue(compareAndSwapKeyValueFail, "random", "world");
      assertNotNull(key);
      assertTrue(key.errorCode() != 0);
      assertTrue(key.cause().equals("[random != " + compareValue + "]"));
   }

   @Test
   public void testCompareAndeSwapKeyIndex() {
      String compareValue = "hello";
      Key createdKey = api().createKey(compareAndSwapKeyIndex, compareValue);
      assertNotNull(createdKey);
      assertTrue(createdKey.errorCode() == 0);

      Key key = api().compareAndSwapKeyIndex(compareAndSwapKeyIndex, createdKey.node().createdIndex(), "world");
      assertNotNull(key);
      assertTrue(key.action().equals("compareAndSwap"));
      assertTrue(key.prevNode().value().equals("hello"));
      assertTrue(key.node().value().equals("world"));
   }

   @Test
   public void testCompareAndeSwapKeyIndexFail() {
      String compareValue = "hello";
      Key createdKey = api().createKey(compareAndSwapKeyIndexFail, compareValue);
      assertNotNull(createdKey);
      assertTrue(createdKey.errorCode() == 0);

      int goodIndex = createdKey.node().createdIndex();
      int badIndex = createdKey.node().createdIndex() + 1;
      Key key = api().compareAndSwapKeyIndex(compareAndSwapKeyIndexFail, badIndex, "world");
      assertNotNull(key);
      assertTrue(key.errorCode() != 0);
      assertTrue(key.cause().equals("[" + badIndex + " != " + goodIndex + "]"));
   }

   @Test
   public void testWaitKey() {
      String localKey = randomString();
      String localValue = randomString();
      Key createdKey = api().createKey(localKey, localValue, 3);
      assertNotNull(createdKey);
      assertNotNull(createdKey.node().expiration());
      assertTrue(createdKey.node().ttl() == 3);

      createdKey = api().waitKey(localKey);
      assertNotNull(createdKey);
      assertTrue(createdKey.action().equals("expire"));
      assertTrue(createdKey.prevNode().value().equals(localValue));
   }

   @Test
   public void testWaitOnIndexKey() {
      String localKey = randomString();
      String localValue = randomString();
      Key createdKey = api().createKey(localKey, localValue, 3);
      assertNotNull(createdKey);
      assertNotNull(createdKey.node().expiration());
      assertTrue(createdKey.node().ttl() == 3);

      // the next action on this key, which will be an expire due
      // to the ttl, will increment the index by 1.
      int waitIndex = createdKey.node().createdIndex() + 1;

      createdKey = api().waitKey(localKey, waitIndex);
      assertNotNull(createdKey);
      assertTrue(createdKey.action().equals("expire"));
      assertTrue(createdKey.prevNode().value().equals(localValue));
   }

   @Test
   public void testCreateDir() {
      Key key = api().createDir(dir);
      assertNotNull(key);
      assertTrue(key.action().equals("set"));
      assertTrue(key.node().key().equals("/" + dir));
      assertTrue(key.node().dir());
   }

   @Test(dependsOnMethods = "testCreateDir")
   public void testCreateDirAlreadyExists() {
      Key key = api().createDir(dir);
      assertNotNull(key);
      assertTrue(key.message().equals("Not a file"));
   }

   @Test(dependsOnMethods = "testCreateDirAlreadyExists")
   public void testListDir() {
      Key key = api().listDir(dir, true);
      assertNotNull(key);
      assertTrue(key.action().equals("get"));
      assertTrue(key.node().key().equals("/" + dir));
      assertTrue(key.node().dir());
   }

   @Test(dependsOnMethods = "testListDir")
   public void testDeleteDir() {
      Key key = api().deleteDir(dir);
      assertNotNull(key);
      assertTrue(key.action().equals("delete"));
      assertTrue(key.node().key().equals("/" + dir));
      assertTrue(key.node().dir());
   }

   @Test
   public void testListDirNonExistent() {
      Key key = api().listDir(randomString(), true);
      assertNotNull(key);
      assertTrue(key.message().equals("Key not found"));
   }

   @Test
   public void testCreateDirWithTTL() {
      Key key = api().createDir(dirWithTTL, 1);
      assertNotNull(key);
      assertTrue(key.action().equals("set"));
      assertTrue(key.node().key().equals("/" + dirWithTTL));
      assertTrue(key.node().dir());
      try {
         Thread.sleep(5000);
      } catch (Exception e) {
         Throwables.propagate(e);
      }

      Key nonExistentDir = api().listDir(dirWithTTL, true);
      assertNotNull(nonExistentDir);
      assertTrue(nonExistentDir.message().equals("Key not found"));
   }

   @Test
   public void testDeleteDirNonExistent() {
      Key key = api().deleteDir(randomString());
      assertNotNull(key);
      assertTrue(key.message().equals("Key not found"));
   }

   @AfterClass
   public void finalize() {
      api().deleteKey(compareAndSwapKeyIndex);
      api().deleteKey(compareAndSwapKeyIndexFail);
      api().deleteKey(compareAndSwapKeyValue);
      api().deleteKey(compareAndSwapKeyValueFail);
      api().deleteKey(compareAndDeleteKeyValue);
      api().deleteKey(compareAndDeleteKeyValueFail);
      api().deleteKey(key);
      api().deleteDir(inOrderKey);
      api().deleteDir(dir);
      api().deleteDir(dirWithTTL);
   }

   private KeysApi api() {
      return api.keysApi();
   }
}
