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
    int number;

    public NightTime(String name, Phaser phaser, Boolean isSkrull) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
        this.isSkrull = isSkrull;
        this.number = AroundEarth.playerNum.get(name);
    }

    public void run() {
        phaser.register();
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            display(out);
            Thread.sleep(AroundEarth.stabilizeTime);
            phaser.arriveAndAwaitAdvance();

            if(isSkrull){
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("당신은 Skrull입니다. \n죽일 사람의 이름을 15초 안에 입력하세요.");

                String vict = br.readLine();
                AroundEarth.killTerran(vict);

            } else {
                out.println("당신은 Terran입니다.");
                for (int i = 3; i > 0; i--) {
                    out.println(i * 5 + "초 후 낮으로 바뀝니다.");
                    Thread.sleep(5000);
                }
            }

            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndDeregister();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display(PrintWriter out){
        out.println("\n\n========== " + AroundEarth.dayCount + " 번째 날 밤 ==========");
        if(number != -1) out.println("당신의 번호는 " + number + "입니다.\n");
        else out.println("당신은 사망하셨습니다.");

        out.println("==========" + " 생존자 목록 " + "===========");
        AroundEarth.playerNum
                .keySet().stream()
                .filter(e -> AroundEarth.playerNum.get(e) > 0)
                .forEach(e -> out.print(e + "\t"));
        out.println("\n==================================\n");


    }
}
