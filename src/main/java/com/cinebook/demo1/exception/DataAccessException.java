package com.cinebook.demo1.exception;


public class DataAccessException extends RuntimeException {
  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataAccessException(String message) {
    super(message);
  }
}
