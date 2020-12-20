import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

class DayTime implements Runnable {
    Socket socket = null;
    String name;
    Phaser phaser;
    int number;

    public DayTime(String name, Phaser phaser) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
        this.number = AroundEarth.playerNum.get(name);
    }

    public void run() {
        phaser.register();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            display(out);

            phaser.arriveAndAwaitAdvance();
            out.println("===== 회의 시간을 시작합니다 =====");
            out.println("========= 제한 시간 30초 =========");

            if(number != -1) {
                Thread chatThread = new Thread(new Chat(name, br));
                chatThread.start();

                Thread.sleep(AroundEarth.discussTime);
                chatThread.interrupt();
            } else{
                Thread.sleep(AroundEarth.discussTime);
            }

            phaser.arriveAndAwaitAdvance();
            out.println("=== 회의 시간이 종료되었습니다 ===\n");

            phaser.arriveAndAwaitAdvance();
            out.println("======= 투표를 시작합니다 ========");
            out.println("========= 제한 시간 10초 =========\n");


            if(number != -1) {
                Thread voteThread = new Thread(new Vote(name, br));
                voteThread.start();

                Thread.sleep(AroundEarth.voteTime);
                voteThread.interrupt();
            } else{
                Thread.sleep(AroundEarth.voteTime);
            }

            phaser.arriveAndAwaitAdvance();
            out.println("=== 투표 시간이 종료되었습니다 ===");

            phaser.arriveAndDeregister();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display(PrintWriter out){

        out.println("\n\n========= " + AroundEarth.dayCount  + " 번째 날 아침 =========");

        String status = "";
        if(AroundEarth.isKilled){
            status = "*** " + AroundEarth.victim + "이(가) 사망하였습니다. ***";
        } else{
            status = "*** 아무도 사망하지 않았습니다. ***";
        }


        if(number != -1) out.println("당신의 번호는 " + number + "입니다.\n");
        else out.println("당신은 사망하셨습니다.\n말을 할 수 없습니다.\n");


        out.println(status);
        out.println("==========" + " 생존자 목록 " + "===========");
        AroundEarth.playerNum
                .keySet().stream()
                .filter(e -> AroundEarth.playerNum.get(e) > 0)
                .forEach(e -> out.print(e + "\t"));
        out.println("\n==================================\n");
   }
}
