import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class participant{
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    String partcipantID;
    String host;
    int port;
//  String config_name;
    String log;
    private ThreadB threadB;

    public participant(String config)
    {
        try {
            File con_file = new File(config);
            if(!con_file.exists()) {
                System.out.println("Error: no config to read...");
                System.exit(1);
            }
            Scanner cfile = new Scanner(con_file);
            partcipantID = cfile.nextLine();
            log = cfile.nextLine();
            host = cfile.next();
            port = Integer.parseInt(cfile.next());

            socket = new Socket(host, port);
            // Start Command line
            commandLine();
        } catch (IOException ex) {
            System.out.println("Error: Configuration file");
            System.exit(0);
        }
    }

    public void commandLine()
    {
        Scanner input = new Scanner(System.in);
        while(true)
        {
            System.out.print("participant> ");
            try {
                // Send command to server and call the function accordingly
                String inp = input.nextLine();
                // dos = new DataOutputStream(socket.getOutputStream());
                // dos.writeUTF(inp);
                if(inp.startsWith("register")){
                    register(inp);
                }
                else if(inp.trim().startsWith("deregister")){
                    deregister(inp);
                }
                else if(inp.startsWith("reconnect")){
                    reconnect(inp);
                }
                else if(inp.trim().startsWith("disconnect")){
                    disconnect(inp.trim());
                }
                else if(inp.startsWith("msend")){
                    msend(inp);
                } else if(inp.trim().startsWith("quit")){
                    deregister(inp);
                    System.exit(0);
                } else if(inp != "") {
                    System.out.print("Invalid command, try again..\n");
                }
                // return;
            } catch (Exception e) {
                e.printStackTrace();
                // return;
            }
        }
    }


    private void register(String input) {
        if(input.split(" ").length == 2) {
            try {
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(input);
                InetAddress address = InetAddress.getLocalHost();
                int port  = Integer.parseInt(input.split(" ")[1]);
                // System.out.print(String.valueOf(port));
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(partcipantID);
                dos.writeUTF(address.getHostAddress());
                dos.writeUTF(String.valueOf(port));
                dis = new DataInputStream(socket.getInputStream());
                String ack = dis.readUTF();
                System.out.println(ack);
                if(!ack.equals("Participant Registered Successfully !!")) return;
                threadB = new ThreadB(port, log);
                new Thread(threadB).start();
            } catch (IOException ex) {
                System.out.println("Error: Unable to connect");
                ex.printStackTrace();
            }
        } else System.out.println("Error: provide port");
    }

    private void deregister(String input) {
        System.out.println("deregistering...");
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(input);
            System.out.println("Deregistering participant: " + partcipantID);
            Thread.sleep(200);
            threadB.relinquish();
            Thread.currentThread().stop();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void reconnect(String input) {
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(input);
            InetAddress address = InetAddress.getLocalHost();
            int port  = Integer.parseInt(input.split(" ")[1]);
            threadB = new ThreadB(port, log);
            new Thread(threadB).start();
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(address.getHostAddress());
            dos.writeUTF(input.split(" ")[1]);
            dis = new DataInputStream(socket.getInputStream());
            String ack = dis.readUTF();
            System.out.println(ack);
            if(!ack.equals("User reconnected!"))
                threadB.relinquish();

        } catch (IOException ex) {
            System.out.println("Unable to connect");
        }
    }

    private void disconnect(String input) {
        System.out.println("disconnecting...");
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(input);
            Thread.sleep(200);
            threadB.relinquish();
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
    private void msend(String input) {
        if(input.split(" ").length > 1) {
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(input);
                String message = partcipantID + ": " + input.substring(6);
                dos.writeUTF(message);

                System.out.println(dis.readUTF());
            }
            catch (IOException io) {
                io.printStackTrace();   
            }
        } else System.out.println("Error: Invalid send command..");
    }

    public static void main(String args[]){
        if(args.length != 1){
            System.out.println("Error: Invalid arguements");
            System.exit(0);
        }
        participant participant = new participant(args[0]);

    }
}

class ThreadB implements Runnable{
    
    private int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private String log_name;

    public ThreadB(int port, String log)
    {
        this.port = port;
        this.log_name = log;

    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            if(serverSocket != null)
                socket = serverSocket.accept();
            while(socket != null && serverSocket != null) {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                Thread.sleep(200);
                // System.out.print("Stopping for input");
                String msg = dis.readUTF();
                    // System.out.print("My wait is over");
                if(!msg.equals("disconnecting") && !msg.equals("deregistering")) {
                    System.out.println("\n"+msg);
                    BufferedWriter out = new BufferedWriter(new FileWriter(log_name, true));
                    out.write(msg + "\n");
                    out.close();
                    System.out.print("participant> ");
                } else {
                    Thread.currentThread().stop();
                }
            }
        } catch ( InterruptedException ie) {
            ie.printStackTrace();
            return;
        } catch (IOException io) {
            // io.printStackTrace();
            return;
        } 
    }

    public void relinquish() {
        try {
            if(socket != null)
            {
                socket.close();
                socket = null;
            }
            if(serverSocket != null)
            {
                serverSocket.close();
                serverSocket = null;
            }
            // Thread.currentThread().stop();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
