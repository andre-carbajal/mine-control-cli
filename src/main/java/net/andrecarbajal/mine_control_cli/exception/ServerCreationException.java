package net.andrecarbajal.mine_control_cli.exception;

public class ServerCreationException extends RuntimeException {
    public ServerCreationException(String message) {
        super(message);
    }

    public ServerCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
