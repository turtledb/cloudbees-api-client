package com.cloudbees.api;

/**
 * SSH public key.
 *
 * @author Kohsuke Kawaguchi
 * @see CBUser#ssh_keys
 */
public class CBSshKey {
    /**
     * A single line format of the public key.
     * The same format as in ~/.ssh/authorized_keys (except the third token), such as
     * "ssh-rsa AAAAB3123456....ffbb"
     */
    public String content;

    /**
     * Human-readable name that identifies this key among other keys.
     * <p/>
     * The third token in ~/.ssh/authorized_keys
     */
    public String name;

    /**
     * Public key fingerprint like "11:22:33:...:dd:ee:ff"
     */
    public String fingerprint;
}
