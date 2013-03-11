package com.cloudbees.api.config;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * {@link Environment} and {@link ConfigParameters} can both own parameters, hence this class.
 *
 * @author Kohsuke Kawaguchi
 */
public class ParameterHolder {
    @XStreamImplicit(itemFieldName="param")
    private ParameterList<ParameterSettings> parameters;

    @XStreamImplicit(itemFieldName="runtime-param")
    private ParameterList<ParameterSettings> runtimeParameters;

    /**
     * Parameters are string-to-string map, made available to
     * applications as system properties.
     *
     * @return
     *      This method always returns the same live map. Update to this map
     *      gets reflected to the {@link ConfigParameters}.
     */
    public ParameterMap getParameters() {
        if (parameters == null)
            parameters = new ParameterList<ParameterSettings>();
        return parameters.asMap();
    }

    /**
     * Runtime parameters are used to communicate with the stack
     * that runs your application (as opposed to the application itself.)
     *
     * @return
     *      This method always returns the same live map. Update to this map
     *      gets reflected to the {@link ConfigParameters}.
     */
    public ParameterMap getRuntimeParameters() {
        if (runtimeParameters == null)
            runtimeParameters = new ParameterList<ParameterSettings>();
        return runtimeParameters.asMap();
    }
}
