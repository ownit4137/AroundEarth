import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {
    static ArrayList<Socket> userSocketList = new ArrayList<Socket>();
    static Socket socket = null;

    public Server(Socket socket) {
        this.socket = socket;
        userSocketList.add(socket);
    }

    public void run() {
        try(
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ){

            System.out.println(socket.getInetAddress() + "연결됨");

            out.println("닉네임을 입력해 주세요");

            String inputLine;
            String name = null;
            PrintWriter sender = null;

            while((inputLine = br.readLine()) != null ) {
                if(name == null) name = inputLine;

                for(int i = 0; i < userSocketList.size(); i++) {
                    sender = new PrintWriter(userSocketList.get(i).getOutputStream(), true);

                    sender.println(name + " : " + inputLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //ExecutorService eService = Executors.newFixedThreadPool(5);

        try(ServerSocket sSocket = new ServerSocket(10000)) {
            System.out.println("서버 열림");

            while(true) {
                Socket cSocket = sSocket.accept();
                System.out.println("연결됨");

                Thread cThread = new Server(cSocket);
                cThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}