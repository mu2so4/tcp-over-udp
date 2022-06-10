package lab4.nets;

import lab4.nets.pseudotcp.PseudoTcpSocket;

import java.io.*;
import java.net.InetAddress;
import java.util.logging.LogManager;

public class PseudoTcpReceiver {
    public static void main(String[] args) {
        try(InputStream stream = PseudoTcpReceiver.class.getClassLoader().
                getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int listeningPort = 12321;
        try(PseudoTcpSocket socket = new PseudoTcpSocket(InetAddress.getLoopbackAddress(), listeningPort)) {
            OutputStream fileOutputStream = new FileOutputStream("out.txt");
            socket.receive(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
