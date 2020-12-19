import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
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
            out.println("====== 회의 시간을 시작합니다. ======");
            out.println("====== 제한 시간 30초 ======");

            Thread chatThread = new Thread(new Chat(name, br));
            chatThread.start();

            Thread.sleep(AroundEarth.discussTime);
            chatThread.interrupt();

            phaser.arriveAndAwaitAdvance();
            out.println("====== 회의 시간이 종료되었습니다. ======");

            phaser.arriveAndAwaitAdvance();
            out.println("====== 투표를 시작합니다. ======");
            out.println("====== 제한 시간 10초 ======");

            Thread voteThread = new Thread(new Vote(name, br));
            voteThread.start();

            Thread.sleep(AroundEarth.voteTime);
            voteThread.interrupt();

            phaser.arriveAndAwaitAdvance();
            out.println("====== 투표 시간이 종료되었습니다. ======");

            phaser.arriveAndDeregister();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void display(PrintWriter out){

        out.println("========= " + AroundEarth.dayCount  + " 번째 날 아침 =========\n");

        if(number != -1) out.println("당신의 번호는 " + number + "입니다.");

        String status = "";
        if(AroundEarth.isKilled){
            status = "*** " + AroundEarth.victim + "이(가) 사망하였습니다. ***";
        } else{
            status = "*** 아무도 사망하지 않았습니다. ***";
        }


        out.println(status);
        out.println("생존자 목록\n==================================");
        AroundEarth.playerNum
                .keySet().stream()
                .filter(e -> AroundEarth.playerNum.get(e) > 0)
                .forEach(out::println);
        out.println("==================================\n\n");

        /*

        회의 진행 메세지

         */
   }
}
