import java.io.PrintWriter;
import java.net.Socket;

class Finale implements Runnable {
    private String name;
    private Socket socket;

    public Finale(String name) {
        this.name = name;
        this.socket = AroundEarth.playerSocket.get(name);
    }

    public void run() {

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("===== 게임이 종료되었습니다 ======");

            if(AroundEarth.finishFlag == AroundEarth.FinishFlag.SKRULLWIN){
                out.println("********** Skrull 승리 ***********");
            }
            else if(AroundEarth.finishFlag == AroundEarth.FinishFlag.TERRANWIN){
                out.println("********** Terran 승리 ***********");
            }

            out.println("======= 연결을 종료합니다 ========");
            out.println("quit");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
