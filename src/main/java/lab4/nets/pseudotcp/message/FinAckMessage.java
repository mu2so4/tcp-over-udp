package lab4.nets.pseudotcp.message;

public class FinAckMessage implements Message {
    private final int ackNumber;

    public FinAckMessage(int ackNumber) {
        this.ackNumber = ackNumber;
    }

    @Override
    public boolean ack() {
        return true;
    }

    @Override
    public boolean syn() {
        return false;
    }

    @Override
    public boolean fin() {
        return true;
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
