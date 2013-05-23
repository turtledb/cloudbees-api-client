package com.cloudbees.api.cr;

/**
 * {@link CloudResourceFacet} is like moons of {@link CloudResource}.
 * They provide type-safe access to various states and CRT-specific operations.
 *
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
        if (owner==null)
            throw new AssertionError();
    }

    /**
     * Returns the {@link CloudResource} that this facet is representing.
     *
     * @return never null.
     */
    public CloudResource getOwner() {
        return owner;
    }
}
