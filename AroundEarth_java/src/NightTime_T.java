import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

class NightTime_T implements Runnable {
    static Socket socket = null;
    String name;
    Phaser phaser;

    public NightTime_T(String name, Phaser phaser) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
    }

    public void run() {
        phaser.register();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            out.println("========== " + phaser.getPhase() + " 번째 날 밤 ==========\n");
            out.println("당신의 번호는 " + AroundEarth.playerNum.get(name) + "입니다.");

            out.println("생존자 목록\n==============================");
            AroundEarth.playerNum.keySet().forEach(out::println);
            out.println("==============================\n\n");

            phaser.arriveAndAwaitAdvance();
            for (int i = 3; i > 0; i--) {
                out.println(i * 5 + "초 후 낮으로 바뀝니다.");
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
