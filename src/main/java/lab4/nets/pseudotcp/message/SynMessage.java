package lab4.nets.pseudotcp.message;

public class SynMessage implements Message {
    private final int seqNumber;

    public SynMessage(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    @Override
    public boolean ack() {
        return false;
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
        return seqNumber;
    }

    @Override
    public int ackNumber() {
        return 0;
    }

    @Override
    public byte[] getData() {
        return null;
    }
}
