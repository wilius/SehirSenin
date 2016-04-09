package org.alexiwilius.sehirsenin.res;

/**
 * Created by AlexiWilius-WS on 21.11.2015.
 */
public class ReachedMaxStarredCardException extends Exception {
    public ReachedMaxStarredCardException() {
    }

    public ReachedMaxStarredCardException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
