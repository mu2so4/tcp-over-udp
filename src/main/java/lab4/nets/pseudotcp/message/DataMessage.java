package lab4.nets.pseudotcp.message;

public class DataMessage implements Message {
    private final byte[] data;
    private final int seqNumber;
    private final int ackNumber;

    public DataMessage(byte[] data, int seqNumber, int ackNumber) {
        this.data = data;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
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
        return data;
    }
}
