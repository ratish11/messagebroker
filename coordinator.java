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
            File cFile = new File(conf_file);
            if(!cFile.exists()) {
                System.out.println("Error: no config to read...");
                System.exit(1);
            }
            Scanner conf = new Scanner(cFile);
            port = Integer.parseInt(conf.nextLine());
            threshold = Integer.parseInt(conf.nextLine());
            threshold *= 1000;
            System.out.println("System time threshold in miliseconds: " + String.valueOf(threshold));
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
                try {
                    socket = serverSocket.accept();
                } catch (IOException io) {
                    System.out.println("Error: Unable to connect participant");
                }
                new Thread(new ParticipantHandler(socket, partcipantIDs, mQueue, pSocketConn, liveParticipants, threshold, ports)).start();
            }
        } catch (IOException io) {
            io.printStackTrace();
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
    private Socket socket, pConn;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String pID;
    private int threshold;
    private String participantPort;
    private HashMap<String, Socket> pSocketConn;
    private HashMap<String, Queue> mQueue;
    private ArrayList<String> liveParticipants;
    private ArrayList<String> partcipantIDs;
    private ArrayList<Integer> ports;

    public ParticipantHandler(Socket socket, ArrayList<String> partcipantIDs, HashMap<String, Queue> mQueue, HashMap<String, Socket> pSocketConn, ArrayList<String> liveParticipants, int threshold, ArrayList<Integer> ports) {
        this.socket = socket;
        this.partcipantIDs = partcipantIDs;
        this.mQueue = mQueue;
        this.pSocketConn = pSocketConn;
        this.liveParticipants = liveParticipants;
        this.threshold = threshold;
        this. ports = ports;
    }

    public void run() {
        System.out.println("Participant " + socket.getInetAddress().getHostAddress() + " connected");
        try {
            while (true) {
                dis = new DataInputStream(socket.getInputStream());
                String command = dis.readUTF();
                System.out.println(command);
                if (command.trim().startsWith("register"))
                    register(command);
                else if (command.trim().startsWith("deregister"))
                    deregister(command);
                else if (command.trim().startsWith("reconect"))
                    reconnect(command);
                else if (command.trim().startsWith("disconnect"))
                    disconect(command);
                else if (command.trim().startsWith("msend"))
                    msend(command);
                else
                    System.out.println("Invalid command received");
                System.out.println("participant ids " + partcipantIDs + " at " + System.currentTimeMillis());
            }
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
    }

    private void register(String command) {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            // while(true) {
            //     UUID pid = UUID.randomUUID();
            //     pID = String.valueOf(pid);
            //     System.out.println(pID);
            //     if(!partcipantIDs.contains(pID)) {
            //         dos.writeUTF(pID);
            //         break;
            //     }
            // }
            pID = dis.readUTF();
            String IP = dis.readUTF();
            int port = Integer.parseInt(dis.readUTF());
            if(!partcipantIDs.contains(pID)) {
                partcipantIDs.add(pID);
                Queue<MessageData> msgQ = new LinkedList<>();
                mQueue.put(pID, msgQ);
                dos.writeUTF("Participant Registered Successfully !!");
                System.out.println("Info: Participant " + pID + " Registered Successfully !!");
                Thread.sleep(500);
                pConn = new Socket(IP, port);
                pSocketConn.put(pID, pConn);
                liveParticipants.add(pID);
                System.out.println("Info: participant " + pID + " B thread connected");
            } else {
                dos.writeUTF("Participant already registered or ID already in use, correct your ID!!");
                System.out.println("Participant already registered or ID " + pID + " already in use");
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void deregister(String command) {
        System.out.println("Deregistering participant " + pID);
        try {
            dos = new DataOutputStream(pSocketConn.get(pID).getOutputStream());
            dos.writeUTF("deregistering");
            partcipantIDs.remove(pID);
            mQueue.remove(pID);
            liveParticipants.remove(pID);
            pSocketConn.remove(pID);
            pConn.close();
            Thread.currentThread().stop();
        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    public void reconnect(String command) {
        try {
            dis = new DataInputStream((socket.getInputStream()));
            dos = new DataOutputStream(socket.getOutputStream());
            String IP = dis.readUTF();
            int port = Integer.parseInt(dis.readUTF());
            if(partcipantIDs.contains(pID)) {
                if(!liveParticipants.contains(pID)) {
                    Thread.sleep(100);
                    pConn = new Socket(IP,port);
                    pSocketConn.put(pID, pConn);
                    liveParticipants.add(pID);
                    dos.writeUTF("Participant reconnected successfully !!");
                    System.out.println("Participant " + pID + " reconnected successfully !!");
                    Queue<MessageData> msgQ = mQueue.get(pID);
                    Thread.sleep(100);
                    while(!msgQ.isEmpty()) {
                        MessageData msg = msgQ.poll();
                        if(System.currentTimeMillis() - msg.getTimestamp() <= threshold) {
                            dos = new DataOutputStream(pConn.getOutputStream());
                            dos.writeUTF(msg.getMessage());
                        }
                    }
                } else dos.writeUTF("Participant already connected and alive");
            } else {
                dos.writeUTF("Participant not registered");
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void disconect(String command) {
        try {
            dos = new DataOutputStream(pSocketConn.get(pID).getOutputStream());
            dos.writeUTF("disconnecting");
            pSocketConn.remove(pConn);
            liveParticipants.remove(pID);
            pConn.close();
            dos.writeUTF("disconnecting");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void msend(String command) {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            String msg = dis.readUTF();
            if(partcipantIDs.contains(pID)) {
                if(liveParticipants.contains(pID)) {
                    dos.writeUTF("Message received, now transferring...");
                    Thread.sleep(100);
                    for(String id : partcipantIDs) {
                        if(id != pID) {
                            System.out.println("checking if participant " + id + " is live");
                            if(liveParticipants.contains(id)) {
                                System.out.println("transferring to participant : " + id);
                                // Socket s = pSocketConn.get(id);
                                DataOutputStream dos = new DataOutputStream(pSocketConn.get(id).getOutputStream());
                                dos.writeUTF(msg);
                            } else {
                                System.out.println("Participant " + id + " is not live, caching...");
                                Queue<MessageData> q = mQueue.get(id);
                                q.add(new MessageData(msg, System.currentTimeMillis()));
                                mQueue.put(id, q);
                            }
                        }
                        
                    }
                } else {
                    dos.writeUTF("Participant " + pID + " is not live");
                }
            } else {
                dos.writeUTF("Participant is not registered");
            }
        } catch (IOException io) {
            try {
                dos.writeUTF("Error: Unable to send message");
                System.out.println("Error: Unable to send message");
            } catch (IOException iox){
                iox.printStackTrace();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        };
    }
}

class MessageData {
    private String msg;
    private Long timestamp;

    public MessageData(String msg, Long ts) {
        this.msg = msg;
        this.timestamp = ts;
    }

    public String getMessage() {return msg;}
    public void setMessage(String message) {this.msg = msg;}
    public Long getTimestamp() {return timestamp;}
    public void setTimestamp() {this.timestamp = timestamp;}
}
