
# etcd-rest

java-based client to interact with Etcd's REST API.

## Setup

Client's can be built like so:

      EtcdClient client = new EtcdClient.Builder()
      .endPoint("http://127.0.0.1:2379")
      .build();

      Version version = client.api().miscellaneousApi().version();
      
## Latest release

Can be found in either jcenter or maven central.

	<dependency>
	  <groupId>com.cdancy</groupId>
	  <artifactId>etcd-rest</artifactId>
	  <version>0.0.1</version>
	</dependency>
	
## Documentation

javadocs can be found via [github pages here](http://cdancy.github.io/etcd-rest/docs/javadoc/)

## Property based setup

Client's do NOT need supply the endPoint as part of instantiating the EtcdClient object. 
Instead one can supply it through system properties, environment variables, or a combination 
of the 2. System properties will be searched first and if not found we will attempt to 
query the environment.

Setting the `endpoint` can be done with any of the following (searched in order):

- `etcd.rest.endpoint`
- `etcdRestEndpoint`
- `ETCD_REST_ENDPOINT`

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

