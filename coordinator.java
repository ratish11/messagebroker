import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

public class coordinator {
    private ServerSocket serverSocket;
    private Socket socket;
    int port, threshold;
    ArrayList<String> partcipantIDs;
    HashMap<String, Queue> mQueue;
    HashMap<String, Socket> pSocketConn;
    ArrayList<String> liveParticipants;
    ArrayList<Integer> ports;

    public coordinator(String conf_file) {
        try {
            File cFile = new File(config);
            if(!conf.exists()) {
                System.out.println("Error: no config to read...")
                System.exit(1);
            }
            Scanner conf = new Scanner(conf);
            port = Integer.parseInt(conf.nextLine());
            threshold = Integer.parseInt(conf.nextLine());
            //instantiate data members
            partcipantIDs = new ArrayList<>();
            mQueue = new HashMap<>();
            pSocketConn = new HashMap<>();
            liveParticipants = new ArrayList<>();
            ports = new ArrayList<>();
            ports.add(port);
            serverSocket = new ServerSocket(port);
            System.out.println("Coordinator started....\n");
            while(true) {
                tyr {
                    socket = ss.accept();
                } catch (IOException io) {
                    System.out.println("Error: Unable to connect participant");
                }
                new Thread(new ParticipantHandler(socket, partcipantIDs, mQueue, pSocketConn, liveParticipants, threshold, ports)).start();
            }
        }
    }
    public static void main(String args[]) {
        if(args.length != 1){
            System.out.println("Error: Invalid arguments");
            System.exit(1);
        }
        coordinator C = new coordinator(args[0]);
    }
}

class ParticipantHandler implements Runnable {
    private Socket socket, participantSocket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String pid;
    private int threshold;
    private String participantPort;
    private HashMap<String, Socket> pSocketConn;
    private HashMap<String Queue> mQueue;
    private ArrayList<String> liveParticipants;
    private ArrayList<String> partcipantIDs;
    private ArrayList<Integer> ports;

    public ParticipantHandler(Socket socket, ArrayList<String> partcipantIDs, HashMap<String, Queue> mQueue, HashMap<String, Socket> pSocketConn, ArrayList<String> liveParticipants, int threshold, ArrayList<String> ports) {
        this.socket = s;
        this.partcipantIDs = partcipantIDs;
        this.mQueue = mQueue;
        this.pSocketConn = pSocketConn;
        this.liveParticipants = liveParticipants;
        this.threshold = threshold;
        this. ports = ports;
    }

    public synchronized void run() {
        System.out.println("Participant " + client.getInetAddress().getHostAddress() + " connected");
        try {
            while(true) {
                dis = new DataInputStream(socket.getInputStream());
                String command = din.readUTF();
                System.out.println(command);
                if(command.trim().startsWith("register"))
                    register(command);
                else if (command.trim().startsWith("derigister"))
                    derigister(command);
                else if (command.trim().startsWith("reconect"))
                    reconnect(command);
                else if (command.trim().startsWith("disconect"))
                    disconect(command);
                else if (command.trim().startsWith("msend"))
                    msend(command);
            }
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }

        private void register(String command) {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            while(true) {
                UUID pID = UUID.randomUUID();
                System.out.println(pID);
                if(!partcipantIDs.cotains(pID)) {
                    dos.writeUTF(pID);
                    break;
                }
            }
            String IP = dis.readUTF();
            int port = Integer.parseInt(dis.readUTF());
            if(!partcipantIDs.contains(pID)) {
                partcipantIDs.add(pID);
                Queue<MessageData> q = new LinkedList<>();

            }

        }
    }
}

class MessageData {
    private String msg;
    private Long timestamp;

    public MessageData(String msg, Long ts) {
        this.msg = msg;
        this.timestamp = ts;
    }

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message}
    public Long getTimestamp() {return timestamp;}
    public void setTimestamp() {this.timestamp = timestamp;}
}