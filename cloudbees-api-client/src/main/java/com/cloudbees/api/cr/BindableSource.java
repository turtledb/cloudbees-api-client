package com.cloudbees.api.cr;

/**
 * @author Kohsuke Kawaguchi
 */
public class BindableSource extends CloudResourceFacet {
    public BindableSource(CloudResource owner) {
        super(owner);
    }

    /**
     * Binds this cloud resource to the specified target.
     */
    public void bindTo(CloudResource sink) {

    }
}