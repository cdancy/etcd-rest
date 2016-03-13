
# etcd-rest

java-based client to interact with Etcd's REST API.

## Setup

Client's can be built like so:

      EtcdClient client = new EtcdClient.Builder()
      .endPoint("http://127.0.0.1:2379")
      .build();

      Version version = client.api().miscellaneousApi().version();

## Property based setup

Client's do NOT need supply the endPoint as part of instantiating the EtcdClient object. 
Instead one can supply them through system properties,
environment variables, or a combination of the 2. System properties will be searched
first and if not found we will attempt to query the environment.

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
	
Running integration tests can be done like so (also runs mock tests):

	./gradlew clean build integTest -PtestEtcdEndpoint=http://127.0.0.1:2379 
	
