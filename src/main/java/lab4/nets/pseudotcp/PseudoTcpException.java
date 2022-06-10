package lab4.nets.pseudotcp;

public class PseudoTcpException extends RuntimeException {
    public PseudoTcpException() {
        super();
    }

    public PseudoTcpException(String message) {
        super(message);
    }

    public PseudoTcpException(Throwable throwable) {
        super(throwable);
    }
}
