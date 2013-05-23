package com.cloudbees.api.cr;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class CloudResourceFacet {
    protected CloudResource owner;

    /**
     * {@link CloudResourceFacet} subtypes must have this constructor,
     * which gets invoked via reflection from {@link CloudResource#as(Class)}
     */
    protected CloudResourceFacet(CloudResource owner) {
        this.owner = owner;
    }
}
