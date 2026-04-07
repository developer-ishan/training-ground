package gateway;

public final class GatewayRejectedException extends RuntimeException {
    public GatewayRejectedException(String message) {
        super(message);
    }
}
