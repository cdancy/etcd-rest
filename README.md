
# etcd-rest

java-based client to interact with Etcd's REST API. 

**As of 3/27/16 etcd-rest is considered feature complete with etcd v2 API.

## Setup

Client's can be built like so:

      EtcdClient client = new EtcdClient.Builder()
      .endPoint("http://127.0.0.1:2379") // Optional. Defaults to http://127.0.0.1:2379
      .credentials("admin:password") // Optional.
      .build();

      Version version = client.api().miscellaneousApi().version();
      
## Latest release

Can be found in jcenter:

	<dependency>
	  <groupId>com.cdancy</groupId>
	  <artifactId>etcd-rest</artifactId>
	  <version>0.0.1</version>
	</dependency>
	
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

## Examples

The [mock](https://github.com/cdancy/etcd-rest/tree/master/src/test/java/com/cdancy/etcd/rest/features) and [live](https://github.com/cdancy/etcd-rest/tree/master/src/test/java/com/cdancy/etcd/rest/features) tests provide many examples
that you can use in your own code.

## Components

- jclouds \- used as the backend for communicating with Etcd's REST API
    
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

