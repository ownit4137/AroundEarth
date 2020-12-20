import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class Chat extends Thread{
    String name;
    Socket socket;
    BufferedReader br;
    boolean stop = false;

    public Chat(String name, BufferedReader br) {
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
        this.br = br;
    }

    public void setStop(boolean stop){
        this.stop = stop;
    }

    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            PrintWriter sender = null;

            while (!stop) {
                inputLine = br.readLine();
                for (Map.Entry<String, Socket> ent : AroundEarth.playerSocket.entrySet()) {
                    new PrintWriter(ent.getValue().getOutputStream(), true)
                            .println(name + " : " + inputLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}