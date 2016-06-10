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

import org.testng.annotations.Test;

import com.cdancy.etcd.rest.EtcdApi;
import com.cdancy.etcd.rest.EtcdApiMetadata;
import com.cdancy.etcd.rest.domain.auth.AuthState;
import com.cdancy.etcd.rest.internal.BaseEtcdMockTest;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

/**
 * Mock tests for the {@link com.cdancy.etcd.rest.features.AuthApi} class.
 */
@Test(groups = "unit", testName = "AuthApiMockTest")
public class AuthApiMockTest extends BaseEtcdMockTest {

    public void testIsEnabled() throws Exception {
        MockWebServer server = mockEtcdJavaWebServer();

        server.enqueue(new MockResponse().setBody(payloadFromResource("/auth-enabled.json")).setResponseCode(200));
        EtcdApi etcdApi = api(server.getUrl("/"));
        AuthApi api = etcdApi.authApi();
        try {

            boolean state = api.isEnabled();
            assertTrue(state);
            assertSent(server, "GET", "/" + EtcdApiMetadata.API_VERSION + "/auth/enable");
        } finally {
            etcdApi.close();
            server.shutdown();
        }
    }

    public void testEnable() throws Exception {
        MockWebServer server = mockEtcdJavaWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        EtcdApi etcdApi = api(server.getUrl("/"));
        AuthApi api = etcdApi.authApi();
        try {
            AuthState state = api.enable();
            assertNotNull(state);
            assertTrue(state.enabled());
            assertNull(state.errorMessage());
            assertSent(server, "PUT", "/" + EtcdApiMetadata.API_VERSION + "/auth/enable");
        } finally {
            etcdApi.close();
            server.shutdown();
        }
    }

    public void testDisabled() throws Exception {
        MockWebServer server = mockEtcdJavaWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        EtcdApi etcdApi = api(server.getUrl("/"));
        AuthApi api = etcdApi.authApi();
        try {
            AuthState state = api.disable();
            assertNotNull(state);
            assertFalse(state.enabled());
            assertNull(state.errorMessage());
            assertSent(server, "DELETE", "/" + EtcdApiMetadata.API_VERSION + "/auth/enable");
        } finally {
            etcdApi.close();
            server.shutdown();
        }
    }
}
