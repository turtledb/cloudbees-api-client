package com.cloudbees.api.cr;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotate {@link CloudResourceFacet} subtypes for the cloud resource type name that they represent.
 *
 * @author Vivek Pandey
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface CloudResourceType {
    /**
     * Gives string representation of URL
     */
    String value();
}

