package lab4.nets.pseudotcp.message;

public class AckMessage implements Message {
    private final int seqNumber;
    private int ackNumber;

    public AckMessage(int seqNumber, int ackNumber) {
        this.seqNumber = seqNumber;
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
        return false;
    }

    @Override
    public int seq() {
        return seqNumber;
    }

    @Override
    public int ackNumber() {
        return ackNumber;
    }

    @Override
    public byte[] getData() {
        return null;
    }

    public void setAckNumber(int ackNumber) {
        this.ackNumber = ackNumber;
    }
}
