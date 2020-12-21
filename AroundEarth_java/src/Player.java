import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Player {
    static boolean stop = false;

    public static void main(String[] args) {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            Socket socket = new Socket(localAddress, 10000);

            Runnable r = () -> {
                try {
                    // cSocket에서 보낸 메세지를 가져옴
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data = null;

                    while(!stop) {
                        data = br.readLine();
                        if(data.equals("quit")) {stop = true; }
                        System.out.println(data);
                    }

                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable w = () -> {
                try {
                    // cSocket에 메시지를 전송
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    Scanner scv = new Scanner(System.in);
                    String input = null;

                    while(!stop) {
                        input = scv.nextLine();

                        out.println(input);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Thread readingThread = new Thread(r);
            Thread writingThread = new Thread(w);
            readingThread.start();
            writingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}