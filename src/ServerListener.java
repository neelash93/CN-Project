/**
 * Created by Prateek
 */
import java.net.ServerSocket;
import java.util.List;


class ServerListener implements Runnable {
    private int port;
    private List<Message> messagesFromPeers;

    public ServerListener(int port, List<Message> messagesFromPeers) {
        this.port = port;
        this.messagesFromPeers = messagesFromPeers;
    }

    @Override
    public void run() {
        System.out.println("The server is running waiting for connections." + '\n');
        int clientIndex = 0;
        ServerSocket listenerSocket = null;
        try {
            while (true) {
                try {
                    listenerSocket = new ServerSocket(port);
                    new Thread(new ServerRequestHandler(listenerSocket.accept(), clientIndex , messagesFromPeers)).start();
//                    System.out.println("Client "  + clientNum + " is connected!");
                    clientIndex++;
//                } catch (SocketException e) {
//                    Thread.sleep(50);
                } finally {
                    listenerSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}