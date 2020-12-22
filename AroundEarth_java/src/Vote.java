import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

/**
 * DayTime에서 회의 후 투표를 진행할 Runnable 클래스
 */
public class Vote implements Runnable{

    /**
     * 멤버 변수
     * name : 투표를 진행할 Player의 이름
     * br : 투표를 진행할 Player의 소켓 입력부분
     * socket : 투표를 진행할 Player의 소켓
     * phaser : 동기화 장치(투표 후 동시에 다음 작업을 진행하게 함)
     */
    private String name;
    private BufferedReader br;
    private Socket socket;
    private Phaser phaser;

    /**
     * 생성자
     */
    public Vote(String name, BufferedReader br, Phaser phaser) {
        this.phaser = phaser;
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
        this.br = br;
    }

    /**
     * 투표 진행
     */
    public void run() {
        try{
            //기존의 phaser에 이 스레드를 등록
            phaser.register();

            // 생존자 목록을 출력
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("==========" + " 생존자 목록 " + "===========");
            AroundEarth.playerNum
                    .keySet().stream()
                    .filter(e -> AroundEarth.playerNum.get(e) > 0)
                    .forEach(e -> out.print(e + "\t"));
            out.println("\n==================================");


            // 플레이어에게 입력을 받음(BufferReader)
            out.println("Skrull일 것 같은 player의 이름을 입력하세요.");
            String votedPlayer = br.readLine();


            // 투표 처리
            // 투표수 Map에 투표할 플레이어의 이름이 있고 && 그 플레이어가 살아 있을 때
            if(AroundEarth.voteCount.containsKey(votedPlayer) &&
                AroundEarth.playerNum.get(votedPlayer) != -1){

                System.out.println(name + " voted " + votedPlayer);

                // 투표수 Map의 숫자를 가져와서 1 증가시킴
                int votedNum = AroundEarth.voteCount.get(votedPlayer);
                AroundEarth.voteCount.replace(votedPlayer, votedNum + 1);
            }


            // 투표 종료 전 동기화
            phaser.arriveAndAwaitAdvance();
            // phaser 등록 해제
            phaser.arriveAndDeregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}