import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

class DayTime implements Runnable {
    Socket socket = null;
    String name;
    Phaser phaser;

    public DayTime(String name, Phaser phaser) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
    }

    public void run() {
        phaser.register();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("========= " + phaser.getPhase() + " 번째 날 아침 =========\n");
            out.println("당신의 번호는 " + AroundEarth.playerNum.get(name) + "입니다.");

            if(AroundEarth.isKilled){
                out.println("*** " + AroundEarth.victim + "이(가) 죽었습니다. ***");
                if(name == AroundEarth.victim){
                    phaser.arriveAndDeregister();
                    out.println("당신은 사망했습니다");
                }
            }

            out.println("생존자 목록\n==================================");
            AroundEarth.playerNum.keySet().forEach(out::println);
            out.println("==================================\n\n");

            phaser.arriveAndAwaitAdvance();


            out.println("채팅 30초");
            Thread.sleep(10000);

            phaser.arriveAndAwaitAdvance();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
