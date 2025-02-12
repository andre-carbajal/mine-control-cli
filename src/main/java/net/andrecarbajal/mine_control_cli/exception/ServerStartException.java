package net.andrecarbajal.mine_control_cli.exception;

public class ServerStartException extends RuntimeException {
    public ServerStartException(String message) {
        super(message);
    }

    public ServerStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
