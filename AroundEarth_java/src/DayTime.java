import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

/**
 * 낮 시간(회의, 투표)을 수행할 Runnable 클래스
 */
class DayTime implements Runnable {

    /**
     * 멤버 변수
     * socket : 플레이어의 소켓
     * name : 플레이어의 이름
     * phaser : 동기화 장치
     * number : 플레이어가 갖고 있는 숫자
     */
    private Socket socket = null;
    private String name;
    private Phaser phaser;
    private int number;

    /**
     * 생성자
     */
    public DayTime(String name, Phaser phaser) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
        this.number = AroundEarth.playerNum.get(name);
    }

    /**
     * 사망하지 않은 모든 Player가 회의를 진행함
     *   - 30초간 진행
     * 사망하지 않은 모든 Player가 투표를 진행함
     *   - 10초간 진행
     */
    public void run() {
        // 쓰레드를 phaser에 등록
        phaser.register();
        try {
            // 연결된 socket의 입출력을 위해 선언
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            display(out);                                           // 각종 출력문을 출력
            phaser.arriveAndAwaitAdvance();                         // 회의 시작 전 동기화

            out.println("===== 회의 시간을 시작합니다 =====");
            out.println("========= 제한 시간 30초 =========");

            if(number != -1) {                                      // 사망하지 않은 플레이어는
                Chat chatThread = new Chat(name, br, phaser);       // 쓰레드 Chat 객체 실행
                chatThread.start();
                Thread.sleep(AroundEarth.discussTime);              // 30초간 진행
                chatThread.setStop(true);                           // 중지(setStop 사용)

            } else{                                                 // 사망한 플레이어는
                Thread.sleep(AroundEarth.discussTime);              // 대기
            }

            phaser.arriveAndAwaitAdvance();                         // 회의 종료 전 동기화
            out.println("== 회의 시간이 종료되었습니다 ===\n");

            phaser.arriveAndAwaitAdvance();                         // 투표 시작 전 동기화
            out.println("======= 투표를 시작합니다 ========");
            out.println("========= 제한 시간 10초 =========\n");


            if(number != -1) {                                                  // 사망하지 않은 플레이어는
                Thread voteThread = new Thread(new Vote(name, br, phaser));     // 쓰레드 Vote 객체 실행
                voteThread.start();

                Thread.sleep(AroundEarth.voteTime);                             // 10초간 실행
                voteThread.interrupt();                                         // 중지(interrupt 사용)

            } else{                                                             // 사망한 플레이어는
                Thread.sleep(AroundEarth.voteTime);                             // 대기
            }

            phaser.arriveAndAwaitAdvance();                                     // 투표 종료 전 동기화
            out.println("=== 투표 시간이 종료되었습니다 ===");

            phaser.arriveAndDeregister();                                       // phaser 등록 해제

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 낮 시작할때 각종 출력문을 출력하는 함수
     *   - 몇 번째 낮인지
     *   - 밤에 어떤 플레이어가 사망했는지
     *   - 플레이어의 번호는 몇번인지 / 플레이어가 사망했는지
     *   - 생존자 목록
     * @param out : Player의 출력을 담당할 PrintWriter 객체
     */
    public void display(PrintWriter out){

        out.println("\n\n========= " + AroundEarth.dayCount  + " 번째 날 아침 =========");

        if(number != -1) out.println("당신의 번호는 " + number + "입니다.\n");
        else out.println("당신은 사망하셨습니다.\n말을 할 수 없습니다.\n");

        String status = "";     // 밤에 어떤 플레이어가 사망했는지를 나타내는 문자열
        if(AroundEarth.isKilled){
            status = "*** " + AroundEarth.victim + "이(가) 사망하였습니다. ***";
        } else{
            status = "*** 아무도 사망하지 않았습니다. ***";
        }
        out.println(status);

        // 생존자 목록을 Stream을 사용하여 출력(Player의 숫자가 -1이 아닌 것을 filtering)
        out.println("==========" + " 생존자 목록 " + "===========");
        AroundEarth.playerNum
                .keySet().stream()
                .filter(e -> AroundEarth.playerNum.get(e) != -1)
                .forEach(e -> out.print(e + "\t"));
        out.println("\n==================================\n");
   }
}
