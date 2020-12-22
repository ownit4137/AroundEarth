import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Phaser;

/**
 * 밤 시간을 수행할 Runnable 클래스
 */
class NightTime implements Runnable {

    /**
     * 멤버 변수
     * socket : 플레이어의 소켓
     * name : 플레이어의 이름
     * phaser : 동기화 장치
     * isSkrull : 해당 플레이어가 skrull인지를 표시
     * number : 플레이어가 갖고 있는 숫자
     */
    private Socket socket = null;
    private String name;
    private Phaser phaser;
    private boolean isSkrull = false;
    private int number;

    /**
     * 생성자
     */
    public NightTime(String name, Phaser phaser, boolean isSkrull) {
        this.socket = AroundEarth.playerSocket.get(name);
        this.name = name;
        this.phaser = phaser;
        this.isSkrull = isSkrull;
        this.number = AroundEarth.playerNum.get(name);
    }


    /**
     * Skrull은 사람을 죽이고 Terran은 밤을 기다림
     *   - 15초간 진행
     */
    public void run() {
        // 쓰레드를 phaser에 등록
        phaser.register();

        try {
            // 연결된 socket의 입출력을 위해 선언
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            display(out);                                                  // 각종 출력문을 출력
            phaser.arriveAndAwaitAdvance();                                // 밤 시작 전 대기


            if(isSkrull){       // 플레이어가 Skrull일 경우
                // 입력을 받아 죽일 사람을 지목
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.println("당신은 Skrull입니다. \n죽일 사람의 이름을 15초 안에 입력하세요.");

                String vict = br.readLine();
                AroundEarth.killPlayer(vict);

            } else {            // 플레이어가 Terran일 경우
                // 15초 대기
                out.println("당신은 Terran입니다.");
                for (int i = 3; i > 0; i--) {
                    out.println(i * 5 + "초 후 낮으로 바뀝니다.");
                    Thread.sleep(5000);
                }
            }

            // 밤 종료 전 대기
            phaser.arriveAndAwaitAdvance();
            // phaser 등록 해제
            phaser.arriveAndDeregister();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 밤 시작할때 각종 출력문을 출력하는 함수
     *   - 몇 번째 밤인지
     *   - 플레이어의 번호는 몇번인지 / 플레이어가 사망했는지
     *   - 생존자 목록
     * @param out : Player의 출력을 담당할 PrintWriter 객체
     */
    public void display(PrintWriter out){
        out.println("\n\n========== " + AroundEarth.dayCount + " 번째 날 밤 ==========");
        if(number != -1) out.println("당신의 번호는 " + number + "입니다.\n");
        else out.println("당신은 사망하셨습니다.");


        // 생존자 목록을 Stream을 사용하여 출력(Player의 숫자가 -1이 아닌 것을 filtering)
        out.println("==========" + " 생존자 목록 " + "===========");
        AroundEarth.playerNum
                .keySet().stream()
                .filter(e -> AroundEarth.playerNum.get(e) != -1)
                .forEach(e -> out.print(e + "\t"));
        out.println("\n==================================\n");
    }
}
