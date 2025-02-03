package com.auction.auction_site.exception;

public class AlreadyParticipatedException extends RuntimeException {
    public AlreadyParticipatedException(String message) {
        super(message);
    }

    public AlreadyParticipatedException() {
        super();
    }

    public AlreadyParticipatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyParticipatedException(Throwable cause) {
        super(cause);
    }
}
