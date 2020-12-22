import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;


/**
 * 서버에 접속하여 서버와 소켓 통신을 진행할 Player 클래스
 */
public class Player {

    /**
     * stop : 서버에 읽고 쓰는 쓰레드를 중지할 변수
     */
    static boolean stop = false;

    public static void main(String[] args) {
        try {
            // localhost의 주소를 가져옴
            InetAddress localAddress = InetAddress.getLocalHost();
            // 10000번 포트를 사용하여 연결
            Socket socket = new Socket(localAddress, 10000);


            /**
             * 서버에서 보낸 메시지를 읽는 Runnable 람다식
             */
            Runnable r = () -> {
                try {
                    // socket의 내용을 읽는 객체
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data = null;

                    while(!stop) {      // stop이 false일 동안 한 줄씩 가져와 출력
                        data = br.readLine();
                        // 내용이 "quit"이면 게임이 종료되었음을 의미하므로 읽는 기능을 중지
                        if(data.equals("quit")) { stop = true; }
                        System.out.println(data);
                    }
                    // 소켓 연결 종료
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            /**
             * 서버에 입력한 메세지를 보내는 Runnable 람다식
             */
            Runnable w = () -> {
                try {
                    // socket에 내용을 보내는 객체
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    // 입력을 받아
                    Scanner scv = new Scanner(System.in);
                    String input = null;

                    while(!stop) {      // stop이 false일 동안 한 줄씩 입력
                        input = scv.nextLine();
                        out.println(input);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            // 읽고 쓰는 쓰레드를 실행
            Thread readingThread = new Thread(r);
            Thread writingThread = new Thread(w);
            readingThread.start();
            writingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}