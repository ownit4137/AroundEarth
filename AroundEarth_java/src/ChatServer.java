import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

public class ChatServer implements Runnable{
    static Map<String, Socket> userSocketList = new Hashtable<String, Socket>();
    static Socket socket = null;

    public ChatServer(Socket socket) {
        this.socket = socket;
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
                if(name == null) {
                    name = inputLine;
                    userSocketList.put(name, socket);
                }

                for (Map.Entry<String, Socket> ent : userSocketList.entrySet()){
                    new PrintWriter(ent.getValue().getOutputStream(), true)
                            .println(name + " : " + inputLine);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try(ServerSocket sSocket = new ServerSocket(10000)) {
            System.out.println("서버 열림");

            while(true) {
                Socket cSocket = sSocket.accept();
                System.out.println("연결됨");

                Thread cThread = new Thread(new ChatServer(cSocket));
                cThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}