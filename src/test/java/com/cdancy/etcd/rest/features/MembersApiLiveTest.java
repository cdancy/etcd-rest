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
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jclouds.rest.ResourceAlreadyExistsException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cdancy.etcd.rest.BaseEtcdApiLiveTest;
import com.cdancy.etcd.rest.domain.members.CreateMember;
import com.cdancy.etcd.rest.domain.members.Member;
import com.google.common.collect.ImmutableList;

@Test(groups = "live", testName = "MembersApiLiveTest", singleThreaded = true)
public class MembersApiLiveTest extends BaseEtcdApiLiveTest {

    private String selfID;
    private Member nonSelfMember;
    private Member addedMember;

    @BeforeClass
    protected void init() {
        selfID = api.statisticsApi().self().id();
        assertNotNull(selfID);
    }

    public void testListMembers() {
        List<Member> members = api().list();
        assertNotNull(members);
        assertTrue(members.size() > 0);
        if (members.size() != 1) {
            for (Member member : members) {
                if (!member.id().equals(selfID)) {
                    this.nonSelfMember = member;
                    return;
                }
            }
            throw new RuntimeException("Could not find another member in cluster with different id");
        }
    }

    @Test(dependsOnMethods = "testListMembers", enabled = false)
    public void testDeleteMember() {
        boolean successful = api().delete(nonSelfMember.id());
        assertTrue(successful);
    }

    @Test(dependsOnMethods = "testDeleteMember", enabled = false)
    public void testAddMember() {
        assertNotNull(nonSelfMember);

        addedMember = api().add(CreateMember.create(null, nonSelfMember.peerURLs(), null));
        assertNotNull(addedMember);
        assertTrue(addedMember.peerURLs().containsAll(nonSelfMember.peerURLs()));
    }

    @Test(dependsOnMethods = "testAddMember", expectedExceptions = ResourceAlreadyExistsException.class, enabled = false)
    public void testAddExistingMember() {
        assertNotNull(addedMember);

        Member existingMember = api().add(CreateMember.create(null, addedMember.peerURLs(), addedMember.clientURLs()));
        assertNotNull(existingMember);
    }

    @Test
    public void testAddMemberWithMalformedURL() {
        Member member = api().add(CreateMember.create(null, ImmutableList.of("htp:/hello/world:11bye"), null));
        assertNotNull(member);
        assertTrue(member.errorMessage().message().startsWith("URL scheme must be http or https"));
    }

    @Test
    public void testAddMemberWithIllegalFormat() {
        Member member = api().add(CreateMember.create(null, ImmutableList.of("http://www.google.com"), null));
        assertNotNull(member);
        assertTrue(member.errorMessage().message().startsWith("URL address does not have the form"));
    }

    @Test(dependsOnMethods = "testAddMemberWithIllegalFormat")
    public void testDeleteMemberNonExistentMember() {
        boolean successful = api().delete(randomString());
        assertFalse(successful);
    }

    private MembersApi api() {
        return api.membersApi();
    }
}
