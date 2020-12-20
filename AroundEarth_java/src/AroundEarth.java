import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.stream.Stream;


public class AroundEarth {
    static final int maxPlayer = 4;
    static final int dayTimeWait = 50000;
    static final int nightTimeWait = 15000;

    static final int discussTime = 30000;
    static final int voteTime = 10000;
    static final int stabilizeTime = 3000;
    static int skrullNum;
    static int dayCount = 1;

    static boolean isKilled = false;
    static String victim = "";
    static boolean isFinished = false;

    static Map<String, Socket> playerSocket = new Hashtable<String, Socket>();
    static Map<String, Integer> playerNum = new Hashtable<String, Integer>();
    static Map<String, Integer> voteCount = new Hashtable<String, Integer>();


    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(maxPlayer + 1);
        ServerSocket sSocket = null;


        try {
            sSocket = new ServerSocket(10000);
            System.out.println("======= AroundEarth 서버 열림 =======");

            for (int i = 0; i < maxPlayer; i++) {
                Socket cSocket = sSocket.accept();
                System.out.println(cSocket.getInetAddress() + "연결됨");
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
                    boolean isSkrull = playerNum.get(name) == skrullNum;
                    Thread nightThread = new Thread(new NightTime(name, phaser, isSkrull));
                    nightThread.start();
                }
                phaser.arriveAndAwaitAdvance();
                Thread.sleep(nightTimeWait);
                phaser.arriveAndAwaitAdvance();

            } catch (Exception e) {
                e.printStackTrace();
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

                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();

                Thread.sleep(AroundEarth.discussTime);
                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();

                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();

                Thread.sleep(AroundEarth.voteTime);
                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();



            } catch (Exception e) {
                e.printStackTrace();
            }
            AroundEarth.voteCount.entrySet().stream().forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
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
    public static void killPlayer(String vict){
        if(playerNum.keySet().contains(vict) && playerNum.get(vict) != -1){
            isKilled = true;
            victim = vict;
            playerNum.replace(vict, -1);
        }
        else{
            isKilled = false;
        }
    }

    public static void voteResult(){
        Stream countStream = AroundEarth.voteCount.values().stream();
        /*

            투표수 세기

         */

    }
}




//playerSocket.forEach((u, v) -> System.out.println(v.isClosed()+"#"));       //check
