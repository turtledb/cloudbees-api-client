package com.cloudbees.api;

/**
 * Base class for model objects in the CloudBees API.
 *
 * @author Kohsuke Kawaguchi
 */
/*package*/ abstract class CBObject {
    /*package*/ transient BeesClient2 root;
}
