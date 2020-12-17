import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;


public class ConnectServer implements Runnable {
    static Socket socket = null;
    CyclicBarrier barrier;

    public ConnectServer(Socket socket, CyclicBarrier barrier) {
        this.socket = socket;
        this.barrier = barrier;
    }

    public void run() {
        try  {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println(socket.getInetAddress() + "연결됨");

            out.println("닉네임을 입력해 주세요");

            /*
             *
             *   이름 중복처리
             *
             */

            String name = br.readLine();
            AroundEarth.playerSocket.put(name, socket);
            barrier.await();

            out.println("5초 후 게임이 시작됩니다");
            for (int i = 5; i > 0; i--) {
                out.println(i);
                Thread.sleep(1000);
            }
            out.println("게임 시작 !");
            /*
             *
             *   게임 시작 문구 출력
             *
             */
            Thread.sleep(2000);
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
