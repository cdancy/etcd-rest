[![Build Status](https://travis-ci.org/cdancy/etcd-rest.svg?branch=master)](https://travis-ci.org/cdancy/etcd-rest)
# etcd-rest

java-based client to interact with Etcd's REST API. 

**As of 3/27/16 etcd-rest is considered feature complete with etcd v2 API.

## Setup

Client's can be built like so:

      EtcdClient client = new EtcdClient.Builder()
      .endPoint("http://127.0.0.1:2379") // Optional. Defaults to http://127.0.0.1:2379
      .credentials("admin:password") // Optional.
      .build();

      Key createdKey = client.api().keysApi().createKey("keyName", "keyValue");
      
## Latest release

Can be sourced from jcenter like so:

	<dependency>
	  <groupId>com.cdancy</groupId>
	  <artifactId>etcd-rest</artifactId>
	  <version>0.9.2</version>
	</dependency>
	
We also publish '__-sources__', '__-javadoc__', and '__-all__' artifacts.
	
## Documentation

javadocs can be found via [github pages here](http://cdancy.github.io/etcd-rest/docs/javadoc/)

## Property based setup

Client's do NOT need supply the endPoint or credentials as part of instantiating the EtcdClient object. 
Instead one can supply them through system properties, environment variables, or a combination 
of the 2. System properties will be searched first and if not found we will attempt to 
query the environment.

Setting the `endpoint` can be done with any of the following (searched in order):

- `etcd.rest.endpoint`
- `etcdRestEndpoint`
- `ETCD_REST_ENDPOINT`

Setting the `credentials` can be done with any of the following (searched in order):

- `etcd.rest.credentials`
- `etcdRestCredentials`
- `ETCD_REST_CREDENTIALS`

## Credentials

etcd-rest credentials can take 1 of 2 forms:

- Colon delimited username and password: __admin:password__ 
- Base64 encoded username and password: __YWRtaW46cGFzc3dvcmQ=__ 

## Understanding ErrorMessage

Instead of throwing an exception most objects will have an attached [ErrorMessage](https://github.com/cdancy/etcd-rest/blob/master/src/main/java/com/cdancy/etcd/rest/error/ErrorMessage.java). It is up to the user to check the handed back object to see if the `ErrorMessage` is non-null before proceeding. 

The `message` attribute of `ErrorMessage` is what etcd hands back to us when something fails. In some cases the message may be expected (e.g. Key already exists) and in others not (e.g. User HelloWorld already exists). Using the example above one might proceed like this:

      Key createdKey = client.api().keysApi().createKey("keyName", "keyValue");
      if (createdKey.errorMessage() != null) {
      
          // at this point we know something popped on the server-side.
          // now decide whether we care or not.
          if (createdKey.errorMessage().message().contains("Key already exists")) {
              // ignore 
          } else {
              throw new Exception("Unexpected error: " + createdKey.errorMessage().message());
          }
      }

## Examples

The [mock](https://github.com/cdancy/etcd-rest/tree/master/src/test/java/com/cdancy/etcd/rest/features) and [live](https://github.com/cdancy/etcd-rest/tree/master/src/test/java/com/cdancy/etcd/rest/features) tests provide many examples
that you can use in your own code.

## Components

- jclouds \- used as the backend for communicating with Etcd's REST API
- AutoValue \- used to create immutable value types both to and from the etcd program
    
## Testing

Running mock tests can be done like so:

	./gradlew clean build mockTest
	
Running integration tests can be done like so (requires docker):

	./gradlew clean build integTest
	
Running integration tests without invoking docker can be done like so:

	./gradlew clean build integTest -PbootstrapDocker=false -PtestEtcdEndpoint=http://127.0.0.1:2379 
	
# Additional Resources

* [Etcd REST API](https://github.com/coreos/etcd/blob/master/Documentation/api.md)
* [Etcd Auth API](https://github.com/coreos/etcd/blob/master/Documentation/auth_api.md)
* [Apache jclouds](https://jclouds.apache.org/start/)

