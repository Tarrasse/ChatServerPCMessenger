package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

    private static final int PORT = 9001;

    private static ArrayList<String> names = new ArrayList<>();

    private static ArrayList<PrintWriter> writers = new ArrayList<>();


    public static void main(String[] args)  {
        HandelersFactory handelersFactory = new HandelersFactory();
        System.out.println("The chat server is running.");
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            while (true) {
                //listening to new users
                Socket s = listener.accept();
                //starting threat for every user to handle him
                handelersFactory.newHandeler(s).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }


    private static class HandelersFactory {
        public Handler newHandeler (Socket socket){
            return  new Handler(socket);
        }
    }

    private static class Handler extends Thread {
        private String name;                                    //user name
        private Socket socket;                                  //user socket
        private BufferedReader in;
        private PrintWriter out;
        private PrintWriter selectedPrintWriter = null;         //other user to chat with
        private ArrayList<String> others = new ArrayList<>();           //all users but the the current

        @Override
        public void run() {
            try {
                System.out.println("running");

                //initializing the Scanner and the Print Writer
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    //first get the name
                    name = in.readLine();
                    System.out.println(name);

                    if (name == null) {
                        return;
                    }

                    //if name already exists make the user enter another one (no repeat)
                    if (!names.contains(name)) {
                        out.println("1");

                        for (int i = 0; i < names.size() ; i++) {
                            others.add(names.get(i));
                        }

                        System.out.println(name + " added ");
                        names.add(name);
                        writers.add(out);
                        break;
                    }else{
                        out.println("0");
                    }

                }

                while (true) {
                    //listening to user input
                    System.out.println("listening");
                    String input = in.readLine();
                    System.out.println(input);
                    //fails to read
                    if (input == null) {
                        System.out.println("null input");
                        return;
                    }

                    //this pattern indicates the selected user
                    if(input.contains("123!@#")){
                        //cut out the pattern
                        input = input.substring(6);
                        if(input == "###"){
                            //making the user chat at the chat room
                            selectedPrintWriter=null;
                        }else {
                            System.out.println(name + " Selected " + input);
                            //select the PrintWriter
                            for (int i = 0; i < names.size(); i++) {
                                System.out.println(names.get(i));
                                if (input.equals(names.get(i))) {
                                    selectedPrintWriter = writers.get(i);
                                }
                            }
                        }
                    }
                    //the Pattern to request the others names
                    else if(input.contains("#?!")){
                        others= new ArrayList<>();
                        //refreshing the others
                        for (int i = 0; i < names.size() ; i++) {
                            if(! names.get(i).equals(name)){
                                others.add(names.get(i));
                            }
                        }
                        System.out.println(others);
                        out.println("#?!" + others);
                    }else{
                        //messages
                        if(selectedPrintWriter != null){
                            //if the user has choose someone to chat with before
                            selectedPrintWriter.println(input);
                        }else {
                            //sending the message to public
                            PrintWriter writer ;
                            for (int i = 0; i < names.size() ; i++) {
                                writer = writers.get(i);
                                if(!names.get(i).equals(name)) {
                                    writer.println("PUBLIC " + " " + input);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                //when the user remove  the chat
                if (name != null) {
                    //removing his name
                    names.remove(name);
                }
                if (out != null) {
                    //removing his PrintWriter
                    writers.remove(out);
                }
                try {
                    //closing the socket
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }


        public Handler(Socket socket) {
            //initialize the socket
            this.socket = socket;
        }
    }
}