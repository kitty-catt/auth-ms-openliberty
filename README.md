# auth-ms-openliberty: Openliberty Microservice with OpenID Connect Provider

*This project is part of the 'IBM Cloud Native Reference Architecture' suite, available at
https://cloudnativereference.dev/*

## Table of Contents

* [Introduction](#introduction)
    + [APIs](#apis)
* [Pre-requisites](#pre-requisites)
* [Implementation Details](#implementation-details)
* [Running the application on Docker](#running-the-application-on-docker)
    + [Get the Auth application](#get-the-auth-application)
    + [Set up custom keystore](#set-up-custom-keystore)
    + [Run the Auth application](#run-the-auth-application)
    + [Validating the application](#validating-the-application)
    + [Exiting the application](#exiting-the-application)
* [Conclusion](#conclusion)

## Introduction

This project demonstrates how to authenticate the API user using [OpenID Connect](https://openliberty.io/docs/20.0.0.8/reference/config/openidConnectProvider.html) in the Storefront reference application. The MicroProfile based Authorization Server is used as an OpenID Connect Provider. This reference application delegates authentication and authorization to this component, which verifies the user credentials.

 - MicroProfile based Authorization Server application that handles user authentication and authorization.
 - Uses OpenID Connect 1.0 and acts as a provider to validate login credentials.
 - Return a [mpJwt](https://www.ibm.com/support/knowledgecenter/en/SSAW57_liberty/com.ibm.websphere.liberty.autogen.nd.doc/ae/rwlp_config_mpJwt.html) Bearer token back to caller for identity propagation and authorization.

#### Interaction with OpenID Connect Provider

<p align="center">
    <img src="./images/auth_customer_micro.png">
</p>

- When username/password is passed in, the Authorization microservice validates the credentials based on the details configured in the basic registry.  

#### Interaction with Resource Server API

<p align="center">
    <img src="./images/openliberty_auth.png">
</p>

- When a client wishes to acquire an access token to call a protected API, it calls the OpenID Connect Provider (Authorization microservice) token endpoint with the username/password of the user and requests a token with scope `blue`.
- Authorization microservice will perform the validation.
- If the username/password are valid, a mpJWT token is returned with an `access token` included in it.
- The client uses the `access token` in the `Authorization` header as a bearer token to call other Resource Servers that have the protected API (such as the [Orders microservice](https://github.com/ibm-garage-ref-storefront/orders-ms-openliberty).
- The service implementing the REST API verifies that the `access token` from mpJWT is valid, and then extracts the required claims from the mpJWT to identify the caller.
- The mpJWT is encoded with scope `blue` and the the expiry time in `expires_in`; once the token is generated there is no additional interaction between the Resource Server and the Auth server.

### APIs

Following the [OpenID Connect endpoint URLs](https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_oidc_endpoint_urls.html), the Authorization server exposes both an authorization URI and a token URI.

```
GET /OP/authorize
POST /OP/token
```

The Storefront reference application supports the following clients and grant types:

- The [Storefront Web Application](https://github.com/ibm-garage-ref-storefront/storefront-ui) is using client ID `bluecomputeweb` and client secret `bluecomputewebs3cret` supports Password grant type.

The Storefront application has three scopes `openid`, `admin`, and  `blue`.

## Pre-requisites:
* [Appsody](https://appsody.dev/)
    + [Installing on MacOS](https://appsody.dev/docs/installing/macos)
    + [Installing on Windows](https://appsody.dev/docs/installing/windows)
    + [Installing on RHEL](https://appsody.dev/docs/installing/rhel)
    + [Installing on Ubuntu](https://appsody.dev/docs/installing/ubuntu)
For more details on installation, check [this](https://appsody.dev/docs/installing/installing-appsody/) out.
* Docker Desktop
    + [Docker for Mac](https://docs.docker.com/docker-for-mac/)
    + [Docker for Windows](https://docs.docker.com/docker-for-windows/)

## Implementation Details

We created a new openliberty project using appsody as follows.

```
appsody repo add kabanero https://github.com/kabanero-io/kabanero-stack-hub/releases/download/0.6.5/kabanero-stack-hub-index.yaml

appsody init kabanero/java-openliberty
```

**OpenID Connect Provider** - Configure a Liberty server to act as an OpenID Connect Provider by enabling the openidConnectServer-1.0 feature in Liberty. The ssl-1.0 feature is also required for the openidConnectServer-1.0 feature. This OpenID Connect provider is built on the top of OAuth Provider. So, the oauth provider should be configured as well along with the OpenID Connect provider.

**Basic User Registry** - Liberty Server can be configured with a basic user registry by defining the users and groups information for authentication.

## Running the application on Docker

### Get the Auth application

- Clone auth repository:

```bash
git clone https://github.com/ibm-garage-ref-storefront/auth-ms-openliberty.git
cd auth-ms-openliberty
```

### Set up custom keystore

- Create a local instance of a Keystore by running the commands below.

```
keytool -genkeypair -dname "cn=bc.ibm.com, o=User, ou=IBM, c=US" -alias bckey -keyalg RSA -keysize 2048 -keypass password -storetype PKCS12 -keystore ./BCKeyStoreFile.p12 -storepass password -validity 3650
keytool -list -keystore ./BCKeyStoreFile.p12 -storepass password
keytool -export -alias bckey -file client.cer -keystore ./BCKeyStoreFile.p12 -storepass password
keytool -import -v -trustcacerts -alias bckey -file client.cer -keystore ./truststore.p12 -storepass password -noprompt
```

- Go to `src/main/liberty/config/resources/security` and copy `BCKeyStoreFile.p12` to this location.

- `client.cer` and `truststore.p12` will not be used in this particualr microservice. These will be needed by other storefront microservices like [orders](https://github.com/ibm-garage-ref-storefront/orders-ms-openliberty) and [customer](https://github.com/ibm-garage-ref-storefront/customer-ms-openliberty)

### Run the Auth application

- To run the auth application, use the below command.

```
appsody run
```

- If it is successfully running, you will see something like below.

```
[Container] [INFO] Unit tests finished.
[Container] [INFO] Waiting up to 30 seconds to find the application start up or update message...
[Container] [INFO] CWWKM2010I: Searching for (CWWKZ0001I.*|CWWKZ0003I.*auth-ms-openliberty) in /opt/ol/wlp/usr/servers/defaultServer/logs/messages.log. This search will timeout after 30 seconds.
[Container] [INFO] CWWKM2015I: Match number: 1 is [9/11/20 16:23:48:951 UTC] 00000033 com.ibm.ws.app.manager.AppMessageHelper                      A CWWKZ0001I: Application auth-ms-openliberty started in 3.782 seconds..
[Container] [INFO] Running integration tests...
[Container] [INFO]
[Container] [INFO] -------------------------------------------------------
[Container] [INFO]  T E S T S
[Container] [INFO] -------------------------------------------------------
[Container] [INFO] Running it.dev.appsody.auth.EndpointTest
[Container] [INFO] [ERROR   ] CWMOT0008E: OpenTracing cannot track JAX-RS requests because an OpentracingTracerFactory class was not provided.
[Container] [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.486 s - in it.dev.appsody.auth.EndpointTest
[Container] [INFO] Running it.dev.appsody.auth.HealthEndpointTest
[Container] [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.315 s - in it.dev.appsody.auth.HealthEndpointTest
[Container] [INFO]
[Container] [INFO] Results:
[Container] [INFO]
[Container] [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[Container] [INFO]
[Container] [INFO] Integration tests finished.
```

- You can also verify it as follows.

```
$ docker ps
CONTAINER ID        IMAGE                           COMMAND                  CREATED              STATUS              PORTS                                                                    NAMES
5e7d299711b5        kabanero/java-openliberty:0.2   "/.appsody/appsody-c…"   About a minute ago   Up About a minute   0.0.0.0:7777->7777/tcp, 0.0.0.0:9080->9080/tcp, 0.0.0.0:9443->9443/tcp   auth-ms-openliberty
```

### Validating the application

Now, you can validate the application as follows.

```
curl -k -d "grant_type=password&client_id=bluecomputeweb&client_secret=bluecomputewebs3cret&username=foo&password=bar&scope=openid" https://localhost:9443/oidc/endpoint/OP/token
```

If it is successful, you will see something like below.

```
$ curl -k -d "grant_type=password&client_id=bluecomputeweb&client_secret=bluecomputewebs3cret&username=foo&password=bar&scope=openid" https://localhost:9443/oidc/endpoint/OP/token
{"access_token":"eyJraWQiOiJmQTJYUzMzM09yajlMZmZFSXZJdCIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJmb28iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwic2NvcGUiOlsib3BlbmlkIl0sImp0aSI6IkhLTEgzUlRjM0RodXd6WXAiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzL29pZGMvZW5kcG9pbnQvT1AiLCJleHAiOjE1OTk4NDg3MzgsImlhdCI6MTU5OTg0MTUzOCwiZ3JvdXBzIjpbIlVzZXJzIl0sInVwbiI6ImZvbyJ9.Jke__r_QAsCkz80XM8bZradtutQ4DylC6S8o5a2CJ9juXUDnVfvA4FNXrlLm23_ckoRduSJEamjwq5EgeVB7cs3xQ_B0BnnnfYD4Pmqpi-kDXYunMVCtmx6BNzQUo9D6LBnt9YUoz77X2ZoSeWOwj94_YoayBPF6hJYPR-UOKs23a-26sPlH4107tWba1uvIIxvteJ3Xu1oDThryKwQ7VHoNQZ4LRSXUFYeYZSY4IGQ5rYKXqWaCJlF4oaXempKlWnvhTUopVn4qzC9QSKdturyKUpprMUDshdjafeElPmASUbkLfIN-6SnhZdZ5CybLcGEgtXNapKtcez6vdJvyww","token_type":"Bearer","expires_in":7200,"scope":"openid","refresh_token":"88ioTtUzZpk0XfZx6U1vmYvksXNpHAwvlqJfOxDWCHqqWO7lFA"}
```

Originally, in the [storefront](https://cloudnativereference.dev/) application, this microservice will talk to [customer](https://github.com/ibm-garage-ref-storefront/customer-ms-openliberty) microservice and validate the credentials. To make it easy for local validation, we enabled a test user which can be used to verify the functionality of this application.

### Exiting the application

To exit the application, just press `Ctrl+C`.

It shows you something like below.

```
[Container] [INFO] [AUDIT   ] CWWKE0036I: The server defaultServer stopped after 4 minutes, 58.597 seconds.
[Container] [WARNING] CWWKM2133W: The command to stop server defaultServer failed. The server is probably already stopped.
[Container] [INFO] ------------------------------------------------------------------------
[Container] [INFO] BUILD SUCCESS
[Container] [INFO] ------------------------------------------------------------------------
[Container] [INFO] Total time:  05:12 min
[Container] [INFO] Finished at: 2020-09-11T16:28:36Z
[Container] [INFO] ------------------------------------------------------------------------
[Container] + set +x
[Container] The file watcher is not running because no APPSODY_RUN/TEST/DEBUG_ON_CHANGE action was specified or it has been disabled using the --no-watcher flag.
Closing down development environment.
```

## Conclusion

You have successfully deployed and tested the Auth Microservice in local Docker Containers using Appsody.

To see the Auth application working in a more complex microservices use case, checkout our Microservice Reference Architecture Application [here](https://github.com/ibm-garage-ref-storefront/docs).
