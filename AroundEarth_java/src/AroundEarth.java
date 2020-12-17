import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;


public class AroundEarth {
    static final int maxPlayer = 4;
    static int skrullNum;
    static int dayCount = 1;

    static Boolean isKilled = false;
    static String victim = "";
    static Boolean isFinished = false;


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
                Thread cThread = new Thread(new ConnectServer(cSocket, barrier));
                cThread.start();
            }

            barrier.await();
            giveRole();
            System.out.println("skrullNum is " + skrullNum);
            barrier.await();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Phaser phaser = new Phaser();
        phaser.register();

        Scanner scan = new Scanner(System.in);
        int processFlag = 0;


        while(!isFinished) {
            System.out.println("PhaseCount is " + phaser.getPhase());

            // NightTime
            try {
                for (String name : playerSocket.keySet()) {
                    Boolean isSkrull = playerNum.get(name) == skrullNum;
                    Thread nightThread = new Thread(new NightTime(name, phaser, isSkrull));
                    nightThread.start();
                }
                phaser.arriveAndAwaitAdvance();
                Thread.sleep(15000);
                phaser.arriveAndAwaitAdvance();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if((processFlag = scan.nextInt()) == 0){
                break;
            }

            /*
            *
            *   종료 조건
            *
            */

            // DayTime
            dayCount++;
            try {
                for (String name : playerSocket.keySet()) {
                    Thread dayThread = new Thread(new DayTime(name, phaser));
                    dayThread.start();
                }
                phaser.arriveAndAwaitAdvance();
                Thread.sleep(10000);
                phaser.arriveAndAwaitAdvance();

            } catch (Exception e) {
                e.printStackTrace();
            }


            /*
             *
             *   종료 조건
             *
             */
        }



        /*
         *
         *   게임 종료 처리
         *
         */
        System.out.println("game end");

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


        int rand = (int) (Math.random() * 2) + maxPlayer - 1;
        skrullNum = rand;
    }
    public static void killTerran(String vict){
        if(playerNum.keySet().contains(vict) && playerNum.get(vict) != -1){
            isKilled = true;
            victim = vict;
            playerNum.replace(vict, -1);
        }
        else{
            isKilled = false;
        }
    }

}



//playerSocket.forEach((u, v) -> System.out.println(v.isClosed()+"#"));       //check
