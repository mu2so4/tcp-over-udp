package lab4.nets.pseudotcp.message;

import java.io.Serializable;

public interface Message extends Serializable {
    boolean ack();
    boolean syn();
    boolean fin();
    int seq();

    int ackNumber();
    byte[] getData();
}
