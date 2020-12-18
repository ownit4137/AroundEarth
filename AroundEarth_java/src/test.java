import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;

class Chatty implements Runnable{
    BufferedReader br;
    int num;
    public Chatty(BufferedReader br, int num ){
        this.br = br;
        this.num = num;
    }

    @Override
    public void run() {
        String inputLine;
        PrintWriter sender = null;

        try {
            while ((inputLine = br.readLine()) != null) {
                if(Thread.interrupted()) break;
                for (Map.Entry<Integer, Socket> ent : test.userSocketList.entrySet()) {
                    new PrintWriter(ent.getValue().getOutputStream(), true)
                            .println("player" + num + " : " + inputLine);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}



public class test implements Runnable{
    static Map<Integer, Socket> userSocketList = new Hashtable<Integer, Socket>();
    Socket socket = null;
    int num;

    public test(int num) {
        this.num = num;
        this.socket = userSocketList.get(num);
    }

    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("연결됨, 10초 채팅");


            Thread cth = new Thread(new Chatty(br, num));
            cth.start();

            Thread.sleep(10000);
            cth.interrupt();

            out.println("채팅이 끝났습니다. ");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try(ServerSocket sSocket = new ServerSocket(10000)) {
            System.out.println("서버 열림");

            while(true) {
                Socket cSocket = sSocket.accept();
                int num = (int) (Math.random() * 45);
                userSocketList.put(num, cSocket);
                System.out.println("연결됨");

                Thread cThread = new Thread(new test(num));
                cThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}