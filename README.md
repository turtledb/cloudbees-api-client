This is a Java client library to talk to various CloudBees API, such as [RUN@cloud API](http://wiki.cloudbees.com/bin/view/RUN/API) and [Grand Central API](https://sites.google.com/a/cloudbees.com/account-provisioning-api/home/user-api).

Using the code
==============
To use this library, add the following dependency to your Maven POM:

    <dependency>
      <groupId>com.cloudbees</groupId>
      <artifactId>cloudbees-api-client</artifactId>
      <version>1.5.6</version>
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

Create BeesClient instance using clientId and clientSecret

BeesClient constructor given above creates instance using CloudBees account's apikey and secret. If you want to create BeesClient instance using OAuth app's clientId and secret use the code below:

    //Create BeesClient using your OAuth app's clientId and clientSecret
    BeesClient bees = new BeesClient(clientId, clientSecret);


This API's primary interface is `OauthClient`, and you can create one like this:
    
    OauthClient oac = client.getOauthClient();

Generate access_token using API key and secret

    //withNote is needed only when you are generating OAuth token using your API key and secret
    TokenRequest tokenRequest = new TokenRequest().withNote("My monitoring app", "https://mymonitoring.example.org")
                                                  .withAccount("acme")
                                                  .withScopes("https://api.cloudbees.com/v2/users/user");
    
    OauthToken token = oac.createToken(tokenRequest);


Generate access_token using OAuth app's clientId and clientSecret

    BeesClient bc = new BeesClient(clientId, clientSecret);
    
    TokenRequest tokenRequest = new TokenRequest().withScopes("https://api.cloudbees.com/v2/users/user");
    OauthToken token = oac.createOAuthClientToken(tokenRequest);
    
    //This will generate refresh_token along with access_token
    tokenRequest.withGenerateRequestToken(true)
    
Validate Token

When your app receeves OAuth token, first you need to parse it and then validate it. Validation could mean checking for OAuth scope your application is expecting. You can also check to see which user or account this token was granted to by inspecting (see OauthToken class)

            BeesClient bees = new BeesClient(clientId, clientSecret);
            OauthClient oauthClient = bees.getOauthClient();
            
            //parse Bearer token
            String token = oauthClient.parseAuthorizationHeader(request.getHeaderValue("Authorization"));

            //Although not recommended but OAuth token is passed as request parameter as well
            //Parse access_token query param using JAX-RS Jersey library
            request.getQueryParameters().getFirst("access_token");

            OauthToken oauthToken = oauthClient.validateToken(token, scope);

See [the javadoc](cloudbees-api-client/blob/master/cloudbees-api-client/src/main/java/com/cloudbees/api/oauth/OauthClient.java) of `OauthClient` for more about all the operations that are exposed.
