package lab4.nets.pseudotcp.message;

import lab4.nets.pseudotcp.PseudoTcpException;

public class IllegalPacketException extends PseudoTcpException {
    public IllegalPacketException() {
        super();
    }

    public IllegalPacketException(String message) {
        super(message);
    }

    public IllegalPacketException(Throwable throwable) {
        super(throwable);
    }
}
