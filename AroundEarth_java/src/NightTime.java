import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

class NightTime_S implements Runnable {
    static Socket socket = null;
    String name;
    Phaser phaser;

    public NightTime_S(String name, Phaser phaser) {
        socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
    }

    public void run() {

    }
}



