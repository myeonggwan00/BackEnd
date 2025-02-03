package com.auction.auction_site.exception;

public class AuctionFinishedException extends RuntimeException {
    public AuctionFinishedException(String message) {
        super(message);
    }

  public AuctionFinishedException() {
    super();
  }

  public AuctionFinishedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuctionFinishedException(Throwable cause) {
    super(cause);
  }
}
