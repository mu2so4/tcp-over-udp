package lab4.nets.pseudotcp;

import lab4.nets.pseudotcp.message.*;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

public class PseudoTcpSocket implements AutoCloseable {
    private static final int ACK_TIMEOUT = 200;
    private static final int RECEIVE_TIMEOUT = 3000;
    private static final double LOSS_PROBABILITY = 0.8;

    private static final int MAX_PAYLOAD = 200;
    private final Random random = new Random();
    private static final Logger logger = Logger.getLogger(PseudoTcpSocket.class.getCanonicalName());

    private final DatagramSocket socket;
    private InetAddress address;
    private int remotePort;

    private final byte[] receiveBuffer = new byte[1024];

    public PseudoTcpSocket(InetAddress sourceAddress, int remotePort) throws IOException {
        socket = new DatagramSocket();
        socket.setSoTimeout(RECEIVE_TIMEOUT);
        address = sourceAddress;
        this.remotePort = remotePort;
    }

    public PseudoTcpSocket(int listeningPort) throws IOException {
        socket = new DatagramSocket(listeningPort);
    }



    private void sendMessage(Message message) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteOutputStream);
        outputStream.writeObject(message);
        byte[] sendBuffer = byteOutputStream.toByteArray();
        outputStream.close();
        DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length, address, remotePort);
        packet.setData(sendBuffer);
        socket.send(packet);
    }

    private Message receiveMessage() throws IOException {
        DatagramPacket inputPacket = new DatagramPacket(receiveBuffer,
                receiveBuffer.length, address, remotePort);
        inputPacket.setData(receiveBuffer);
        socket.receive(inputPacket);
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(receiveBuffer);
        ObjectInputStream inputStream = new ObjectInputStream(byteInputStream);
        try {
            return (Message) inputStream.readObject();
        }
        catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private SynMessage receiveSYNMessage() throws IOException {
        socket.setSoTimeout(0);
        DatagramPacket inputPacket = new DatagramPacket(receiveBuffer,
                receiveBuffer.length);
        socket.receive(inputPacket);
        address = inputPacket.getAddress();
        remotePort = inputPacket.getPort();
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(receiveBuffer);
        ObjectInputStream inputStream = new ObjectInputStream(byteInputStream);
        socket.setSoTimeout(ACK_TIMEOUT);
        try {
            return (SynMessage) inputStream.readObject();
        }
        catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Message sendMessageAndReceiveAck(Message sendMessage, String sentMessageName,
                                   boolean syn, boolean fin,
                                   String ackMessageName, int attemptCount) throws IOException {
        logger.info("sent " + sentMessageName + " seq=" + sendMessage.seq());
        for(int attempt = 0; attempt < attemptCount || attemptCount == 0; attempt++) {
            sendMessage(sendMessage);
            try {
                Message ackMessage = receiveMessage();
                if(packetIsNotLost()) {
                    if(!ackMessage.ack()) {
                        logger.info("received duplicate syn=" + ackMessage.syn() +
                                " fin=" + ackMessage.fin());
                        attempt = -1;
                        continue;
                    }
                    if(syn != ackMessage.syn())
                        throw new IllegalPacketException("syn flag != " + syn);
                    if(fin != ackMessage.fin())
                        throw new IllegalPacketException("fin flag != " + fin);
                    logger.info("received " + ackMessageName + " message, ack=" + ackMessage.ackNumber());
                    return ackMessage;
                }
                //else {
                    //logger.info("timeout. Retransmitted " + sentMessageName + " seq=" + sendMessage.seq());
                //}
            }
            catch(SocketTimeoutException t) {
                logger.info("timeout. Resent " + sentMessageName + " seq=" + sendMessage.seq());
            }
        }
        return null;
    }

    private boolean packetIsNotLost() {
        return random.nextDouble() > LOSS_PROBABILITY;
    }

    public void receive(OutputStream streamTo) throws IOException {
        int controlSeq = Math.abs(random.nextInt());
        Message synMessage = new SynMessage(controlSeq);
        //clearInput();
        Message synAckMessage = sendMessageAndReceiveAck(synMessage,
                "SYN", true, false, "SYN ACK", 30);
        if(synAckMessage == null) {
            logger.info("failed to connect");
            return;
        }

        AckMessage ackMessage = new AckMessage(controlSeq, 1);
        logger.info("sent ACK=" + ackMessage.ackNumber());
        sendMessage(ackMessage);
        int currentAckNumber = 1;

        while(true) {
            Message message;
            try {
                message = receiveMessage();
            }
            catch(SocketTimeoutException e) {
                logger.info("receiving data timeout");
                break;
            }
            if(packetIsNotLost()) {
                if(message.ack() && message.syn() ||
                        currentAckNumber > message.seq()) {
                    logger.info("sent duplicate ACK=" + ackMessage.ackNumber());
                    sendMessage(ackMessage);
                }
                else {
                    currentAckNumber++;
                    streamTo.write(message.getData());
                    logger.info("received data with seq=" + message.seq());
                    logger.info("sent ACK=" + currentAckNumber);
                    ackMessage.setAckNumber(currentAckNumber);
                    sendMessage(ackMessage);
                }
            }
        }

        Message finMessage = new FinMessage(controlSeq);
        Message finAckMessage = sendMessageAndReceiveAck(finMessage,
                "FIN", false, true,
                "FIN ACK", 10);
        if(finAckMessage == null) {
            logger.info("closing connection without FIN ACK");
            return;
        }
        ackMessage.setAckNumber(1);
        for(int index = 0; index < 10; index++) {
            sendMessage(ackMessage);
        }
        logger.info("connection closed successfully");
    }

    public void send(InputStream streamFrom) throws IOException {
        //clearInput();
        SynMessage synMessage;
        while(true) {
            synMessage = receiveSYNMessage();
            if(packetIsNotLost()) {
                logger.info("received SYN packet");
                break;
            }
        }
        int controlSeq = synMessage.seq();
        Message synAckMessage = new SynAckMessage(controlSeq);
        logger.info("sent SYN ACK message");
        int attempt;
        final int maxCount = 28;
        for(attempt = 0; attempt < maxCount; attempt++) {
            sendMessage(synAckMessage);
            try {
                Message ackMessage = receiveMessage();
                if(packetIsNotLost()) {
                    logger.info("received ACK=" + ackMessage.ackNumber());
                    break;
                }
            }
            catch(SocketTimeoutException e) {
                logger.info("timeout. Resending SYN ACK packet");
            }
        }
        if(attempt == maxCount) {
            logger.info("client disconnected");
            return;
        }

        byte[] data = new byte[MAX_PAYLOAD];

        for(int seq = 1;; seq++) {
            int realSize = Math.min(MAX_PAYLOAD, streamFrom.available());
            if(streamFrom.read(data) == -1)
                break;
            if(realSize < MAX_PAYLOAD)
                data = Arrays.copyOf(data, realSize);
            Message message = new DataMessage(data, seq, controlSeq);
            Message ack = sendMessageAndReceiveAck(message, "data", false, false,
                    "ACK", 60);
            if(ack == null) {
                logger.info("client disconnected");
                return;
            }
        }

        socket.setSoTimeout(RECEIVE_TIMEOUT * 2);
        Message finMessage;
        while(true) {
            try {
                finMessage = receiveMessage();
            }
            catch (SocketTimeoutException e) {
                logger.info("connection closed without FIN");
                return;
            }
            if(packetIsNotLost()) {
                logger.info("received FIN packet");
                if(!finMessage.fin())
                    throw new IllegalPacketException("received not FIN packet");
                break;
            }
        }

        socket.setSoTimeout(ACK_TIMEOUT);
        Message finAckMessage = new FinAckMessage(controlSeq);
        Message fin = sendMessageAndReceiveAck(finAckMessage, "FIN ACK", false, false,
                "ACK", 6);
        if(fin == null) {
            logger.info("connection closed without ACK");
        }
        else {
            logger.info("connection closed successfully");
        }
    }

    private void clearInput() throws IOException {
        int oldTimeout = socket.getSoTimeout();
        socket.setSoTimeout(10);
        while(true) {
            try {
                receiveMessage();
            }
            catch(SocketTimeoutException e) {
                break;
            }
        }
        socket.setSoTimeout(oldTimeout);
    }

    @Override
    public void close() throws IOException {
        clearInput();
        socket.close();
    }
}
