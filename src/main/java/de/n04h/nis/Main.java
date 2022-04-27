package de.n04h.nis;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            ServiceInfo svi = jmdns.getServiceInfo("_nisserver", "nis-main");

            if(svi != null) {
                System.out.println(svi.getInetAddresses()[0]);
            }

            if(List.of(args).contains("server")){
                //No Server found
                System.out.println("Server mode");

                ServiceInfo serviceInfo = ServiceInfo.create("_nisserver", "nis-main", 1337, "");
                jmdns.registerService(serviceInfo);

                DatagramSocket socket = new DatagramSocket(serviceInfo.getPort());

                byte[] buffer = new byte[512];

                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                buffer = data.getBytes();

                DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                socket.send(response);

                Thread.sleep(10000);

                jmdns.unregisterAllServices();

            }else{
                //Server found
                System.out.println("Client mode");
                if(svi != null) {

                    String hostname = "localhost";
                    int port = svi.getPort();

                    InetAddress address = svi.getInetAddresses()[0];
                    DatagramSocket socket = new DatagramSocket();

                    byte[] buffer = new byte[512];

                    DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
                    socket.send(request);

                    //TODO: Differentiate between clipboard request and clipboard update

                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.receive(response);

                    String quote = new String(buffer, 0, response.getLength());

                    System.out.println(quote);
                }else{
                    System.out.println("Server not found");
                }
            }

        } catch (IOException | UnsupportedFlavorException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
