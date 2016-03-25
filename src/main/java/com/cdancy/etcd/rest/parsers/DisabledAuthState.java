package com.cdancy.etcd.rest.parsers;

import javax.inject.Singleton;

import org.jclouds.http.HttpResponse;

import com.cdancy.etcd.rest.domain.auth.AuthState;
import com.google.common.base.Function;

/**
 * Created by dancc on 3/11/16.
 */
@Singleton
public class DisabledAuthState implements Function<HttpResponse, AuthState> {

   public AuthState apply(HttpResponse response) {
      return AuthState.create(false, null);
   }
}
