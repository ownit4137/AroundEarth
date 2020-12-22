import java.io.PrintWriter;
import java.net.Socket;

/**
 * 종료 후 처리를 진행할 Runnable 클래스
 */
class Finale implements Runnable {

    /**
     * name : 플레이어의 이름
     * socket : 플레이어의 socket
     */
    private String name;
    private Socket socket;

    /**
     * 생성자
     */
    public Finale(String name) {
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
    }

    /**
     * 게임 결과를 출력함
     */
    public void run() {

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("===== 게임이 종료되었습니다 ======");


            // finishFlag에 따라 결과 문구 출력
            if(AroundEarth.finishFlag == AroundEarth.FinishFlag.SKRULLWIN){
                out.println("********** Skrull 승리 ***********");
            }
            else if(AroundEarth.finishFlag == AroundEarth.FinishFlag.TERRANWIN){
                out.println("********** Terran 승리 ***********");
            }

            // 연결 종료
            out.println("======= 연결을 종료합니다 ========");

            // 게임이 종료되었음을 알리는 "quit"을 플레이어 소켓에 보냄
            out.println("quit");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
