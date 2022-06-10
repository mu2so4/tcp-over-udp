package lab4.nets.pseudotcp.message;

public class SynAckMessage implements Message {
    private final int ackNumber;

    public SynAckMessage(int ackNumber) {
        this.ackNumber = ackNumber;
    }

    @Override
    public boolean ack() {
        return true;
    }

    @Override
    public boolean syn() {
        return true;
    }

    @Override
    public boolean fin() {
        return false;
    }

    @Override
    public int seq() {
        return 1;
    }

    @Override
    public int ackNumber() {
        return ackNumber;
    }

    @Override
    public byte[] getData() {
        return null;
    }
}
