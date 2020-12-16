import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

class NightTime implements Runnable {
    Socket socket = null;
    String name;
    Phaser phaser;
    Boolean isSkrull = false;

    public NightTime(String name, Phaser phaser, Boolean isSkrull) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
        this.isSkrull = isSkrull;
    }

    public void run() {
        phaser.register();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("========== " + phaser.getPhase() + " 번째 날 밤 ==========\n");
            out.println("당신의 번호는 " + AroundEarth.playerNum.get(name) + "입니다.");

            out.println("생존자 목록\n==================================");
            AroundEarth.playerNum.keySet().forEach(out::println);
            out.println("==================================\n\n");

            phaser.arriveAndAwaitAdvance();


            if(isSkrull){
                out.println("당신은 Skrull입니다. 죽일 사람의 이름을 15초 안에 입력하세요.");
                String vict = br.readLine();

                if(AroundEarth.playerNum.keySet().contains(vict) && AroundEarth.playerNum.get(vict) != -1){
                    AroundEarth.isKilled = true;
                    AroundEarth.victim = vict;
                }
            } else {
                out.println("당신은 Terran입니다.");
                for (int i = 3; i > 0; i--) {
                    out.println(i * 5 + "초 후 낮으로 바뀝니다.");
                    Thread.sleep(5000);
                }
            }

            phaser.arriveAndAwaitAdvance();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
