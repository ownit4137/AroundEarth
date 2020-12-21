import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;


public class AroundEarth {
    static final int nightTimeWait = 15000;
    static final int discussTime = 30000;
    static final int voteTime = 10000;
    static final int stabilizeTime = 3000;

    static int currentPlayer = 5;
    static int skrullNum = 0;
    static int dayCount = 1;

    static boolean isKilled = false;
    static String victim = "";

    static Map<String, Socket> playerSocket = new Hashtable<String, Socket>();
    static Map<String, Integer> playerNum = new Hashtable<String, Integer>();
    static Map<String, Integer> voteCount = new Hashtable<String, Integer>();

    enum FinishFlag {NOTFINISHED, TERRANWIN, SKRULLWIN}
    static FinishFlag finishFlag = FinishFlag.NOTFINISHED;


    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(currentPlayer + 1);
        ServerSocket sSocket = null;


        try {
            sSocket = new ServerSocket(10000);
            System.out.println("======= AroundEarth 서버 열림 =======");

            for (int i = 0; i < currentPlayer; i++) {
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

        while(true) {
            System.out.println("PhaseCount is " + phaser.getPhase());

            // NightTime
            try {
                for (String name : playerSocket.keySet()) {
                    boolean isSkrull = playerNum.get(name) == skrullNum;
                    Thread nightThread = new Thread(new NightTime(name, phaser, isSkrull));
                    nightThread.start();
                }

                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();

                Thread.sleep(nightTimeWait);
                phaser.arriveAndAwaitAdvance();

            } catch (Exception e) {
                e.printStackTrace();
            }


            if((finishFlag = finishCheck()) != FinishFlag.NOTFINISHED) { break; }

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


                voteResult();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if((finishFlag = finishCheck()) != FinishFlag.NOTFINISHED) { break; }
        }



        try {
            for (String name : playerSocket.keySet()) {
                Thread finaleThread = new Thread(new Finale(name));
                finaleThread.start();
            }

            Thread.sleep(stabilizeTime);

            for(Socket s : playerSocket.values()){
                s.close();
            }

            System.out.println("========== Game end ==========");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void giveRole(){
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        while (numbers.size() < currentPlayer) {
            int rand = (int) (Math.random() * currentPlayer) + 1;
            if (!numbers.contains(rand)) {
                numbers.add(rand);
            }
        }

        List<String> names = new ArrayList<String>(playerSocket.keySet());

        names.stream()
                .forEach(name -> playerNum.put(name, numbers.get(names.indexOf(name))));


        int rand = (int) (Math.random() * 2) + currentPlayer - 1;
        skrullNum = rand;
    }
    public static void killPlayer(String vict){
        if(playerNum.keySet().contains(vict) && playerNum.get(vict) != -1){
            isKilled = true;
            victim = vict;
            playerNum.replace(vict, -1);
            currentPlayer--;
            System.out.println(vict + " is killed");
        }
        else{
            isKilled = false;
            victim = null;
        }
    }



    // for문을 그냥 사용해도 되지만 수업시간에 배운 stream을 활용해서 구성
    public static void voteResult(){
        int max = voteCount.values().stream().max(Integer::compareTo).orElse(0);
        List<String> maxVotedList = voteCount.entrySet().stream()
                                    .filter(e -> e.getValue() == max).map(Map.Entry::getKey)
                                    .collect(Collectors.toList());

        if(maxVotedList.size() == 1){
            killPlayer(maxVotedList.get(0));
        }

        voteCount.forEach((s, n) -> voteCount.replace(s, 0));

    }


    public static FinishFlag finishCheck(){
        if(!playerNum.containsValue(skrullNum)){
            return FinishFlag.TERRANWIN;
        }
        else if(currentPlayer == 1 && playerNum.containsValue(skrullNum)){
            return FinishFlag.SKRULLWIN;
        }
        else {
             return FinishFlag.NOTFINISHED;
        }
    }
}
