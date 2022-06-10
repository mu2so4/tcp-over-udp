package lab4.nets;

import lab4.nets.pseudotcp.PseudoTcpSocket;

import java.io.*;
import java.util.logging.LogManager;

public class PseudoTcpSender {
    public static void main(String[] args) {
        try(InputStream stream = PseudoTcpSender.class.getClassLoader().
                getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int listeningPort = 12321;
        try(PseudoTcpSocket socket = new PseudoTcpSocket(listeningPort);
            InputStream fileInputStream = new FileInputStream("in.txt")) {
            socket.send(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
