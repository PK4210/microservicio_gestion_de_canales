package org.fiuni.mytube_channels.exception;

public class OperationNotAllowedException extends RuntimeException {
  public OperationNotAllowedException(String message) {
    super(message);
  }
}
