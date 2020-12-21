import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

public class Vote implements Runnable{
    private String name;
    private BufferedReader br;
    private Socket socket;
    private Phaser phaser;

    public Vote(String name, BufferedReader br, Phaser phaser) {
        this.phaser = phaser;
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
        this.br = br;
    }

    public void run() {
        try{
            phaser.register();

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("==========" + " 생존자 목록 " + "===========");
            AroundEarth.playerNum
                    .keySet().stream()
                    .filter(e -> AroundEarth.playerNum.get(e) > 0)
                    .forEach(e -> out.print(e + "\t"));
            out.println("\n==================================");
            out.println("Skrull일 것 같은 player의 이름을 입력하세요.");

            String votedPlayer = br.readLine();

            if(AroundEarth.voteCount.containsKey(votedPlayer) &&
                AroundEarth.playerNum.get(votedPlayer) != -1){

                System.out.println(name + " voted " + votedPlayer);
                int votedNum = AroundEarth.voteCount.get(votedPlayer);
                AroundEarth.voteCount.replace(votedPlayer, votedNum + 1);
            }

            phaser.arriveAndAwaitAdvance();
            phaser.arriveAndDeregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}