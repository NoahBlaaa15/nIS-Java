package de.n04h.nis;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static DatagramSocket socket;
    public static String clipboardContent = "";
    public static String lastClipboard = "";

    public static void main(String[] args) {
        try {
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            clipboardContent = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

            if(List.of(args).contains("server")){
                //No Server found
                System.out.println("Server mode");

                ServiceInfo serviceInfo = ServiceInfo.create("_nisserver", "nis-main", 1337, "");
                jmdns.registerService(serviceInfo);

                socket = new DatagramSocket(serviceInfo.getPort());

                while (true){
                    serverCycle();
                    if(System.in.available() != 0){
                        break;
                    }
                }

                System.out.println("Stopping server");

                jmdns.unregisterAllServices();

            }else{
                //Server found
                System.out.println("Client mode");

                ServiceInfo svi = jmdns.getServiceInfo("_nisserver", "nis-main");

                if(svi != null) {
                    System.out.println(svi.getInetAddresses()[0]);

                    socket = new DatagramSocket();

                    while (true){
                        clientCycle(svi);
                        if(System.in.available() != 0){
                            break;
                        }
                    }

                    //System.out.println("Stopping client");
                }else{
                    System.out.println("Server not found");
                }
            }

        } catch (IOException | InterruptedException | UnsupportedFlavorException e) {
            e.printStackTrace();
        }
    }

    public static void clientCycle(ServiceInfo serviceInfo) throws InterruptedException, IOException, UnsupportedFlavorException {

        clipboardContent = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

        if(!clipboardContent.equalsIgnoreCase(lastClipboard)){
            byte[] buffer;

            buffer = clipboardContent.getBytes();

            DatagramPacket request = new DatagramPacket(buffer, buffer.length, serviceInfo.getInetAddresses()[0], serviceInfo.getPort());
            socket.send(request);

            lastClipboard = clipboardContent;
        }

        byte[] buffer = "request".getBytes();

        DatagramPacket request = new DatagramPacket(buffer, buffer.length, serviceInfo.getInetAddresses()[0], serviceInfo.getPort());
        socket.send(request);


        buffer = new byte[512];

        DatagramPacket response = new DatagramPacket(buffer, buffer.length);
        socket.receive(response);

        String recievedData = new String(buffer, 0, response.getLength());

        System.out.println(recievedData);
        if(!clipboardContent.equalsIgnoreCase((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor))){
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(recievedData), null);
        }
        clipboardContent = recievedData;
        lastClipboard = recievedData;

        Thread.sleep(5000);
    }

    public static void serverCycle() throws IOException {
        byte[] buffer = new byte[512];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        socket.receive(request);

        String action = new String(buffer, 0, request.getLength());

        System.out.println(action);

        if(action.equals("request")){

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();

            buffer = clipboardContent.getBytes();

            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);

        }else{

            clipboardContent = action;

        }

    }

}
