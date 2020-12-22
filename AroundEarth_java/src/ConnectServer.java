import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

/**
 * 게임 시작 전 플레이어 접속을 담당하는 클래스
 * 쓰레드를 사용
 */
public class ConnectServer implements Runnable {

    /**
     * 멤버 변수
     * socket : 연결된 Player의 소켓
     * barrier : 동기화 장치(연결 후 동시에 다음 작업을 진행하게 함)
     */
    Socket socket = null;
    CyclicBarrier barrier;

    /**
     * 생성자
     */
    public ConnectServer(Socket socket, CyclicBarrier barrier) {
        this.socket = socket;
        this.barrier = barrier;
    }


    /**
     * 플레이어 접속 후 인원이 다 찰 때까지 대기
     */
    public void run() {
        try  {
            // 연결된 socket의 입출력을 위해 선언
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("========== AroundEarth ==========");
            out.println("===== 닉네임을 입력해 주세요 ====");

            String name = br.readLine();                                    // 이름 입력
            out.println("\n다른 플레이어들을 기다리는 중...\n");

            AroundEarth.playerSocket.put(name, socket);                     // playerSocket(플레이어 이름 & 소켓) Map에 Player의 이름과 socket을 저장
            AroundEarth.voteCount.put(name, 0);                             // voteCount(플레이어 이름 & 투표수) Map에 Player의 이름과 기본값(0)을 저장
            barrier.await();                                                // 모든 플레이어가 접속할 때까지 대기

            out.println("준비 완료, 5초 후 게임이 시작됩니다");
            for (int i = 5; i > 0; i--) {
                out.println(i);
                Thread.sleep(1000);
            }
            out.println("게임 시작 !");                                       // 5초 대기 후 게임 시작

            Thread.sleep(AroundEarth.stabilizeTime);
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
