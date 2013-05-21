This is a Java client library to talk to various CloudBees API, such as [RUN@cloud API](http://wiki.cloudbees.com/bin/view/RUN/API) and [Grand Central API](https://sites.google.com/a/cloudbees.com/account-provisioning-api/home/user-api).

Using the code
==============
To use this library, add the following dependency to your Maven POM:

    <dependency>
      <groupId>com.cloudbees</groupId>
      <artifactId>cloudbees-api-client</artifactId>
      <version>1.5.3</version>
    </dependency>

The entry point of this library is the `BeesClient` class, which you can instantiate like this:

    import com.cloudbees.api.BeesClient;

    String apiUrl = "https://api.cloudbees.com/api";
    String apiKey = ""; //from CloudBees account
    String secret = ""; //from CloudBees account
    BeesClient client = new BeesClient(apiUrl, apiKey, secret, "xml", "1.0")

RUN@cloud API
=============
Historically, this library started as just the client libary for RUN@cloud API. Therefore, you'll see a large number of RUN@cloud API defined as RPC-ish methods directly on `BeesClient`:

    //list your applications
    client.applicationList();

These APIs let you interact with applications and databases.
For all the available RUN@cloud API calls, see [this document](http://wiki.cloudbees.com/bin/view/RUN/API)

Service API
===========
FIXME: please document what this API is about and where the details are defined.

User  API
=========
User API lets you interact with users on your account. This document lives in [here](https://sites.google.com/a/cloudbees.com/account-provisioning-api/home/user-api) and is in the process of moving into public.

These map to the following methods:

    client.createUser(...)
    client.getUser(...)
    client.getSelfUser()
    client.updateUser(...)
    client.getUserByFingerprint(...)

OAuth API
=========
[CloudBees OAuth service](http://wiki.cloudbees.com/bin/view/RUN/OAuth) lets your app interact with CloudBees OAuth service.

This API's primary interface is `OauthClient`, and you can create one like this:

    OauthClient oac = client.getOauthClient();

See [the javadoc](/cloudbees/cloudbees-api-client/blob/master/cloudbees-api-client/src/main/java/com/cloudbees/api/oauth/OauthClient.java) of `OauthClient` for more about all the operations that are exposed.
