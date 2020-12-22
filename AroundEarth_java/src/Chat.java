import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * DayTime에서 회의를 진행할 클래스
 */
public class Chat extends Thread{

    /**
     * 멤버 변수
     * name : 회의를 진행할 Player의 이름
     * br : 회의를 진행할 Player의 소켓 입력부분
     * socket : 회의를 진행할 Player의 소켓
     * phaser : 동기화 장치(회의 후 동시에 다음 작업을 진행하게 함)
     * stop : 쓰레드의 회의를 중단시키는데 사용
     */
    private String name;
    private Socket socket;
    private BufferedReader br;
    private Phaser phaser;
    private boolean stop = false;

    /**
     * 생성자
     */
    public Chat(String name, BufferedReader br, Phaser phaser) {
        this.phaser = phaser;
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
        this.br = br;
    }

    /**
     * stop 변수를 설정
     * @param stop : 쓰레드의 회의를 중단시키는데 사용
     */
    public void setStop(boolean stop){
        this.stop = stop;
    }


    /**
     * 플레이어들의 회의를 진행
     */
    public void run() {
        try {
            // 쓰레드를 phaser에 등록
            phaser.register();

            // 연결된 socket의 입출력을 위해 선언
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            while (!stop) {                     // stop이 false일 때 실행
                inputLine = br.readLine();                                                     // Player가 보낸 데이터를 읽어
                for (Map.Entry<String, Socket> ent : AroundEarth.playerSocket.entrySet()) {    // (플레이어 이름 - Socket) Map에 있는 모든 소켓에게
                    new PrintWriter(ent.getValue().getOutputStream(), true)            // 전송
                            .println(name + " : " + inputLine);
                }
            }


            // 회의 종료 전 동기화
            phaser.arriveAndAwaitAdvance();
            // phaser 등록 해제
            phaser.arriveAndDeregister();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}