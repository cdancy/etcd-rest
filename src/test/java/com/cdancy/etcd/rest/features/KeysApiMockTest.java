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

import org.testng.annotations.Test;

import com.cdancy.etcd.rest.EtcdApi;
import com.cdancy.etcd.rest.EtcdApiMetadata;
import com.cdancy.etcd.rest.domain.keys.Key;
import com.cdancy.etcd.rest.internal.BaseEtcdMockTest;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

/**
 * Mock tests for the {@link com.cdancy.etcd.rest.features.KeysApi} class.
 */
@Test(groups = "unit", testName = "KeysApiMockTest")
public class KeysApiMockTest extends BaseEtcdMockTest {

   public void testCreateKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-create.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.createKey("hello", "world");
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("set"));
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().value().equals("world"));
         assertSentWithFormData(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello", "value=world");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCreateKeyWithTTL() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-create-ttl.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.createKey("hello", "world", 5);
         assertNotNull(createdKey);
         assertNotNull(createdKey.node().expiration());
         assertTrue(createdKey.node().ttl() == 5);
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().value().equals("world"));
         assertSentWithFormData(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello", "value=world&ttl=5");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCreateInOrderKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-create-in-order.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.createInOrderKey("hello", "world");
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("create"));
         assertTrue(createdKey.node().key().matches("/hello/.+"));
         assertTrue(createdKey.node().value().equals("world"));
         assertSentWithFormData(server, "POST", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello", "value=world");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testListInOrderKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-create-in-order-list.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.listInOrderKey("hello");
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("get"));
         assertTrue(createdKey.node().nodes().size() == 2);
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().dir());
         assertTrue(createdKey.node().nodes().get(0).value().equals("World"));
         assertTrue(createdKey.node().nodes().get(1).value().equals("NewWorld"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?recursive=true&sorted=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testListInOrderKeyNonExistent() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-dir-delete-nonexistent.json")).setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.listInOrderKey("hello");
         assertNotNull(createdKey);
         assertTrue(createdKey.errorMessage().message().equals("Key not found"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?recursive=true&sorted=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testGetKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-get.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key foundKey = api.getKey("hello");
         assertNotNull(foundKey);
         assertTrue(foundKey.node().key().equals("/hello"));
         assertTrue(foundKey.node().value().equals("world"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testGetNonExistentKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-get-delete-nonexistent.json")).setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key nonExistentKey = api.getKey("NonExistentKeyToGet");
         assertNotNull(nonExistentKey);
         assertTrue(nonExistentKey.errorMessage().message().equals("Key not found"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/NonExistentKeyToGet");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-delete.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key deletedKey = api.deleteKey("hello");
         assertTrue(deletedKey.prevNode().key().equals("/hello"));
         assertTrue(deletedKey.prevNode().value().equals("world"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteNonExistentKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-get-delete-nonexistent.json")).setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key nonExistentKey = api.deleteKey("NonExistentKeyToDelete");
         assertNotNull(nonExistentKey);
         assertTrue(nonExistentKey.errorMessage().message().equals("Key not found"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/NonExistentKeyToDelete");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testWaitKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-wait.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key waitedOnKey = api.waitKey("hello");
         assertNotNull(waitedOnKey);
         assertTrue(waitedOnKey.action().equals("expire"));
         assertTrue(waitedOnKey.prevNode().value().equals("world"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?wait=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testWaitOnIndexKey() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-wait.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key waitedOnKey = api.waitKey("hello", 2);
         assertNotNull(waitedOnKey);
         assertTrue(waitedOnKey.action().equals("expire"));
         assertTrue(waitedOnKey.prevNode().value().equals("world"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?wait=true&waitIndex=2");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndDeleteKeyValue() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-delete-value.json"))
            .setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key deletedKey = api.compareAndDeleteKey("hello", "world");
         assertNotNull(deletedKey);
         assertTrue(deletedKey.action().equals("compareAndDelete"));
         assertTrue(deletedKey.prevNode().key().equals("/hello"));
         assertTrue(deletedKey.prevNode().value().equals("world"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?prevValue=world");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndDeleteKeyValueWithWrongValue() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-delete-value-fail.json"))
            .setResponseCode(412));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key failedKey = api.compareAndDeleteKey("hello", "world");
         assertNotNull(failedKey);
         assertTrue(failedKey.errorMessage().message().equals("Compare failed"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?prevValue=world");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndDeleteKeyIndex() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-delete-value.json"))
            .setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key deletedKey = api.compareAndDeleteKey("hello", 8);
         assertNotNull(deletedKey);
         assertTrue(deletedKey.action().equals("compareAndDelete"));
         assertTrue(deletedKey.prevNode().key().equals("/hello"));
         assertTrue(deletedKey.prevNode().value().equals("world"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?prevIndex=8");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndDeleteKeyValueWithWrongIndex() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-delete-index-fail.json"))
            .setResponseCode(412));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key failedKey = api.compareAndDeleteKey("hello", 1);
         assertNotNull(failedKey);
         assertTrue(failedKey.errorMessage().cause().equals("[1 != 8]"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello?prevIndex=1");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndSwapKeyValue() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-compare-and-swap-value.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key key = api.compareAndSwapKeyValue("foo", "hello", "world");
         assertNotNull(key);
         assertTrue(key.errorMessage() == null);
         assertTrue(key.action().equals("compareAndSwap"));
         assertTrue(key.prevNode().value().equals("hello"));
         assertTrue(key.node().value().equals("world"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/foo?prevValue=hello");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndSwapKeyValueWithWrongValue() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-swap-value-fail.json"))
            .setResponseCode(412));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key failedKey = api.compareAndSwapKeyValue("foo", "hello", "world");
         assertNotNull(failedKey);
         assertTrue(failedKey.errorMessage().cause().equals("[hello != world]"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/foo?prevValue=hello");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndSwapKeyIndex() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-compare-and-swap-value.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key key = api.compareAndSwapKeyIndex("foo", 8, "world");
         assertNotNull(key);
         assertTrue(key.errorMessage() == null);
         assertTrue(key.action().equals("compareAndSwap"));
         assertTrue(key.prevNode().value().equals("hello"));
         assertTrue(key.node().value().equals("world"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/foo?prevIndex=8");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndSwapKeyIndexWithWrongIndex() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-swap-index-fail.json"))
            .setResponseCode(412));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key failedKey = api.compareAndSwapKeyIndex("foo", 1, "world");
         assertNotNull(failedKey);
         assertTrue(failedKey.errorMessage().cause().equals("[1 != 8]"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/foo?prevIndex=1");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndSwapKeyExist() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-compare-and-swap-exist.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key key = api.compareAndSwapKeyExist("foo", true, "world");
         assertNotNull(key);
         assertTrue(key.errorMessage() == null);
         assertTrue(key.action().equals("update"));
         assertTrue(key.prevNode().value().equals("hello"));
         assertTrue(key.node().value().equals("world"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/foo?prevExist=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCompareAndSwapKeyExistFail() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-compare-and-swap-exist-fail.json"))
            .setResponseCode(412));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key failedKey = api.compareAndSwapKeyExist("foo", false, "world");
         assertNotNull(failedKey);
         assertTrue(failedKey.errorMessage().message().equals("Key already exists"));
         assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/foo?prevExist=false");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCreateDir() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-dir-create.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.createDir("hello");
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("set"));
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().dir());
         assertSentWithFormData(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello", "dir=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   @Test
   public void testCreateDirAlreadyExists() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-dir-create-already-exists.json"))
            .setResponseCode(403));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key key = api.createDir("hello");
         assertNotNull(key);
         assertTrue(key.errorMessage().message().equals("Not a file"));
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testCreateDirWithTTL() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-dir-create-ttl.json")).setResponseCode(201));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.createDir("hello", 100);
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("set"));
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().ttl() == 100);
         assertTrue(createdKey.node().dir());
         assertSentWithFormData(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello", "dir=true&ttl=100");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testListDir() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-dir-list.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.listDir("hello", true);
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("get"));
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().dir());
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello/?recursive=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testListDirNonExistent() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-get-delete-nonexistent.json")).setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.listDir("hello", true);
         assertNotNull(createdKey);
         assertTrue(createdKey.errorMessage().message().equals("Key not found"));
         assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello/?recursive=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteDir() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(new MockResponse().setBody(payloadFromResource("/keys-dir-delete.json")).setResponseCode(200));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.deleteDir("hello");
         assertNotNull(createdKey);
         assertTrue(createdKey.action().equals("delete"));
         assertTrue(createdKey.node().key().equals("/hello"));
         assertTrue(createdKey.node().dir());
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello/?recursive=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }

   public void testDeleteDirNonExistent() throws Exception {
      MockWebServer server = mockEtcdJavaWebServer();

      server.enqueue(
            new MockResponse().setBody(payloadFromResource("/keys-get-delete-nonexistent.json")).setResponseCode(404));
      EtcdApi etcdApi = api(server.getUrl("/"));
      KeysApi api = etcdApi.keysApi();
      try {
         Key createdKey = api.deleteDir("hello");
         assertNotNull(createdKey);
         assertTrue(createdKey.errorMessage().message().equals("Key not found"));
         assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/keys/hello/?recursive=true");
      } finally {
         etcdApi.close();
         server.shutdown();
      }
   }
}
