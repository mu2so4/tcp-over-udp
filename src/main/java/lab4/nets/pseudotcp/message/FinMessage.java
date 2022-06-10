package lab4.nets.pseudotcp.message;

public class FinMessage implements Message {
    private final int seqNumber;

    public FinMessage(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    @Override
    public boolean ack() {
        return false;
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
