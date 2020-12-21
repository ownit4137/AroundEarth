import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Phaser;

public class Chat extends Thread{
    private String name;
    private Socket socket;
    private BufferedReader br;
    private Phaser phaser;
    private boolean stop = false;

    public Chat(String name, BufferedReader br, Phaser phaser) {
        this.phaser = phaser;
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
        this.br = br;
    }

    public void setStop(boolean stop){
        this.stop = stop;
    }

    public void run() {
        try {
            phaser.register();

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

            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndDeregister();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}