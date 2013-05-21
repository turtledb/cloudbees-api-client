What's this?
============

This small Java library lets you interact with [CloudBees OAuth service](http://wiki.cloudbees.com/bin/view/RUN/OAuth).

To use this library, add the following <dependency> tag in your Maven POM:

    <dependency>
      <groupId>com.cloudbees.oauth</groupId>
      <artifactId>oauth-client</artifactId>
      <version>1.1</version>
    </dependency>

The library's primary interface is `OAuthClient`, and you can create one like this:

    OAuthClient oac = new OAuthClientImpl();

See [the javadoc](blob/master/src/main/java/com/cloudbees/oauth/OauthClient.java) of `OAuthClient` for more about all the operations that are exposed.