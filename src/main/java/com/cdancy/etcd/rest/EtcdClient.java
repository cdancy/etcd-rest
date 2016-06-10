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

package com.cdancy.etcd.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jclouds.ContextBuilder;
import org.jclouds.javax.annotation.Nullable;

public class EtcdClient {

    private static String[] endPointProperties = { "etcd.rest.endpoint", "etcdRestEndpoint", "ETCD_REST_ENDPOINT",
            "ETCD_LISTEN_CLIENT_URLS", "ETCD_ADVERTISE_CLIENT_URLS" };
    private static String[] credentialsProperties = { "etcd.rest.credentials", "etcdRestCredentials",
            "ETCD_REST_CREDENTIALS" };
    private String endPoint;
    private String credentials;
    private final EtcdApi etcdApi;

    /**
     * Create an EtcdClient. We will query system properties and environment
     * variables for the endPoint and credentials.
     */
    public EtcdClient() {
        configureParameters();

        this.etcdApi = ContextBuilder.newBuilder(new EtcdApiMetadata.Builder().build()).endpoint(endPoint())
                .credentials("N/A", credentials()).buildApi(EtcdApi.class);
    }

    /**
     * Create an EtcdClient.
     * 
     * @param endPoint
     *            url of etcd instance
     * @param credentials
     *            the optional credentials for the etcd instance
     */
    public EtcdClient(final String endPoint, @Nullable final String credentials) {
        this.endPoint = endPoint;
        this.credentials = credentials;

        this.etcdApi = ContextBuilder.newBuilder(new EtcdApiMetadata.Builder().build()).endpoint(endPoint())
                .credentials("N/A", credentials()).buildApi(EtcdApi.class);
    }

    /**
     * Configure the endPoint and credentials by querying the system, and then
     * the environment, for the relevant property names.
     */
    private void configureParameters() {

        // query system for endPoint value
        if (endPoint == null) {
            endPoint = retrivePropertyValue(true, endPointProperties);
        }

        // query system for credentials value
        if (credentials == null) {
            credentials = retrivePropertyValue(false, credentialsProperties);
        }
    }

    /**
     * Retrieve property value while optionally pinging, if it is found, to see
     * if it is reachable.
     * 
     * @param ping
     *            whether to ping URL
     * @param keys
     *            list of keys to search
     * @return the first value found from list of keys
     */
    public String retrivePropertyValue(boolean ping, String... keys) {
        String value = null;
        for (String possibleKey : keys) {
            value = retrivePropertyValue(possibleKey);
            if (value != null) {
                if (ping) {
                    if (EtcdClient.pingEtcdURL(value, 60000)) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return value;
    }

    /**
     * Check system properties, and then environment variables, for value of
     * key.
     * 
     * @param key
     *            the key to search for
     * @return the value of key or null if not found
     */
    public String retrivePropertyValue(String key) {
        String value = System.getProperty(key);
        return value != null ? value : System.getenv(key);
    }

    public String endPoint() {
        return endPoint;
    }

    public String credentials() {
        return credentials;
    }

    public EtcdApi api() {
        return etcdApi;
    }

    public static class Builder {
        private String endPoint;
        private String credentials;

        public Builder() {
        }

        public Builder(final String endPoint, final String credentials) {
            this.endPoint = endPoint;
            this.credentials = credentials;
        }

        public Builder endPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public Builder credentials(String credentials) {
            this.credentials = credentials;
            return this;
        }

        public EtcdClient build() {
            return new EtcdClient(endPoint, credentials);
        }
    }

    /**
     * Ping etcd URL to see if it is reachable.
     * 
     * @param url
     *            the url to ping
     * @param timeout
     *            the timeout value to wait for ping
     * @return true if pingable, false otherwise
     */
    public static boolean pingEtcdURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url + "/version").openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
