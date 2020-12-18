import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class Chat implements Runnable{
    String name;
    Socket socket;
    BufferedReader br;

    public Chat(String name, BufferedReader br) {
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
        this.br = br;
    }

    public void run() {
        try{
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            PrintWriter sender = null;

            while((inputLine = br.readLine()) != null ) {
                if(Thread.interrupted()) break;
                for (Map.Entry<String, Socket> ent : AroundEarth.playerSocket.entrySet()){
                    new PrintWriter(ent.getValue().getOutputStream(), true)
                            .println(name + " : " + inputLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}