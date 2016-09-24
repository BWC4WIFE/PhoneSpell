package com.phonekeypad.console;

/**
 * Intractable error
 *
 * Created by darcio on 9/14/16.
 */
public class ChallengeRuntimeException extends RuntimeException {
    public ChallengeRuntimeException(String s, Exception e) {
        super(s, e);
    }

    public ChallengeRuntimeException(String s) {
        super(s);
    }
}
