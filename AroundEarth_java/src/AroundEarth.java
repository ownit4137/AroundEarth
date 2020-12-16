import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;


public class AroundEarth {
    static final int maxPlayer = 4;
    static int skrullNum;

    static Map<String, Socket> playerSocket = new Hashtable<String, Socket>();
    static Map<String, Integer> playerNum = new Hashtable<String, Integer>();


    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(maxPlayer + 1);
        ServerSocket sSocket = null;


        try {
            sSocket = new ServerSocket(10000);
            System.out.println("서버 열림");

            for (int i = 0; i < maxPlayer; i++) {
                Socket cSocket = sSocket.accept();
                System.out.println("연결됨");

                Thread cThread = new Thread(new ConnectServer(cSocket, barrier));
                cThread.start();
            }

            barrier.await();
            giveRole();
            barrier.await();

        } catch (Exception e) {
            e.printStackTrace();
        }

        playerSocket.forEach((u, v) -> System.out.println(v.isClosed()+"!"));       // check


        Phaser phaser = new Phaser();
        phaser.register();

        int phaseCount = phaser.getPhase();
        System.out.println("PhaseCount is " + phaseCount);

        playerSocket.forEach((u, v) -> System.out.println(v.isClosed()+"#"));       //check

        try{
            for (String name : playerSocket.keySet()){
                System.out.println(name);
                Thread cThread = new Thread(new NightTime_T(name, phaser));
                cThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




        phaser.arriveAndAwaitAdvance();
        System.out.println("after passing barrier");

        phaseCount = phaser.getPhase();
        System.out.println("PhaseCount is "+phaseCount);


        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void giveRole(){
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        while (numbers.size() < maxPlayer) {
            int rand = (int) (Math.random() * maxPlayer) + 1;
            if (!numbers.contains(rand)) {
                numbers.add(rand);
            }
        }

        List<String> names = new ArrayList<String>(playerSocket.keySet());

        names.stream()
                .forEach(name -> playerNum.put(name, numbers.get(names.indexOf(name))));


        int rand = (int) (Math.random() * 2) + 4;
        skrullNum = rand;
    }

}
