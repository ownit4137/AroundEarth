import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

/** class AroundEarth **
 *
 * AroundEarth 게임을 진행합니다.
 * 플레이어는 Skrull(외계 종족)과 Terran(지구인)으로 나뉩니다.
 * 게임은 낮과 밤의 반복으로 이루어져 있고, 각 턴마다 최대 1명이 사망합니다.
 * 밤에는 Skrull이 한명을 지목하여 죽일 수 있습니다.
 * 낮에는 사람들이 회의를 통해 한 명을 투표로 처형할 수 있습니다.
 * Skrull이 Terran을 모두 죽이면 Skrull이 승리하고, Terran이 Skrull을 찾아내어 죽이면 Terran이 승리합니다.
 *
 * 각 Player들은 랜덤으로 서로 다른 숫자 하나를 받습니다.
 * 플레이어가 n명일 때, Skrull에게는 n이나 n-1이 부여됩니다.
 *      ex) 플레이어가 5명일 때 Skrull에게는 4나 5가 부여됨
 *
 * Skrull은 교묘하게 숨길 것이고, Terran은 그것을 찾아내야 합니다.
 *
 * @author ownit
 */


public class AroundEarth {
    static final int nightTimeWait = 15000;     // 밤 동안 main Thread를 Sleep시키는 시간
    static final int discussTime = 30000;       // 낮-토론 과정 동안 main Thread를 Sleep시키는 시간
    static final int voteTime = 10000;          // 낮-투표 과정 동안 main Thread를 Sleep시키는 시간
    static final int stabilizeTime = 3000;      // 각 과정이 끝날때 phaser을 안정시키기 위해 Thread를 Sleep시키는 시간

    static int currentPlayer = 5;               // 게임을 진행하는 Player수(죽으면 1씩 감소)
    static int skrullNum = 0;                   // skrull을 의미하는 숫자
    static int dayCount = 1;                    // 진행된 날을 count

    static boolean isKilled = false;            // 낮-밤이 바뀔 때 플레이어가 죽었는지를 판별
    static String victim = "";                  // 죽은 플레이어의 이름

    static Map<String, Socket> playerSocket = new Hashtable<String, Socket>();          // 플레이어의 이름과 각각의 연결된 소켓을 저장
    static Map<String, Integer> playerNum = new Hashtable<String, Integer>();           // 플레이어의 이름과 각각이 가진 숫자를 저장(사망하면 -1을 가짐)
    static Map<String, Integer> voteCount = new Hashtable<String, Integer>();           // 투표 과정에서 각 플레이어의 투표 수를 저장

    enum FinishFlag {NOTFINISHED, TERRANWIN, SKRULLWIN}                                 // 게임의 상태를 나타내는 enum class
    static FinishFlag finishFlag = FinishFlag.NOTFINISHED;                              // 게임이 끝났는지를 판별하는 변수





    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(currentPlayer + 1);           // 연결 과정에서 사용할 CyclicBarrier
        ServerSocket sSocket = null;

        try {
            sSocket = new ServerSocket(10000);                                    // 서버를 open
            System.out.println("======= AroundEarth 서버 열림 =======");

            for (int i = 0; i < currentPlayer; i++) {
                Socket cSocket = sSocket.accept();                                     // 지정한 플레이어의 수만큼 소켓 연결
                System.out.println(cSocket.getInetAddress() + "연결됨");
                Thread cThread = new Thread(new ConnectServer(cSocket, barrier));      // 쓰레드를 이용하여 ConnectServer 클래스를 실행
                cThread.start();
            }

            barrier.await();                                                           // 모든 플레이어가 연결될 때까지 대기
            giveRole();                                                                // 플레이어에게 숫자를 부여
            System.out.println("skrullNum is " + skrullNum);
            barrier.await();                                                           // 게임 시작 전 동기화

        } catch (Exception e) {
            e.printStackTrace();
        }

        Phaser phaser = new Phaser();                                                   // 게임을 진행할 phaser
        phaser.register();                                                              // main Thread를 등록


        // 낮과 밤의 반복 루프, finishFlag의 값이 바뀌면 빠져나옴
        while(true) {
            System.out.println("PhaseCount is " + phaser.getPhase());

            /**
             * NightTime - 밤 시간
             */
            try {
                for (String name : playerSocket.keySet()) {
                    // 플레이어가 Skrull인지를 판별하는 조건식
                    boolean isSkrull = playerNum.get(name) == skrullNum;
                    // 밤 시간을 수행할 NightTime 객체를 쓰레드로 실행(Skrull인지 구분)
                    Thread nightThread = new Thread(new NightTime(name, phaser, isSkrull));
                    nightThread.start();
                }

                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();                                         // 밤 시작 전 동기화

                Thread.sleep(nightTimeWait);                                            // 밤 시간동안 Sleep
                phaser.arriveAndAwaitAdvance();                                         // 밤 종료 후 동기화
            } catch (Exception e) {
                e.printStackTrace();
            }


            if((finishFlag = finishCheck()) != FinishFlag.NOTFINISHED) { break; }       // 밤이 끝난 후 finishCheck()함수를 불러 게임 종료 여부 판별
            dayCount++;                                                                 // 날짜 1 증가

            /**
             * DayTime - 낮 시간(회의, 투표)
             */
            try {
                for (String name : playerSocket.keySet()) {
                    // 낮 시간을 수행할 NightTime 객체를 쓰레드로 실행
                    Thread dayThread = new Thread(new DayTime(name, phaser));
                    dayThread.start();
                }

                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();                                         // 회의 시작 전 동기화

                Thread.sleep(AroundEarth.discussTime);
                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();                                         // 회의 종료 전 동기화

                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();                                         // 투표 시작 전 동기화

                Thread.sleep(AroundEarth.voteTime);
                Thread.sleep(AroundEarth.stabilizeTime);
                phaser.arriveAndAwaitAdvance();                                         // 투표 종료 전 동기화

                voteResult();                                                           // 투표 결과 처리
            } catch (Exception e) {
                e.printStackTrace();
            }

            if((finishFlag = finishCheck()) != FinishFlag.NOTFINISHED) { break; }       // 낮이 끝난 후 finishCheck()함수를 불러 게임 종료 여부 판별
        }


        // 게임 종료
        try {
            for (String name : playerSocket.keySet()) {
                // 게임 마무리 처리를 수행할 Finale 객체를 쓰레드로 실행
                Thread finaleThread = new Thread(new Finale(name));
                finaleThread.start();
            }

            Thread.sleep(stabilizeTime);

            // 소켓 연결을 종료
            for(Socket s : playerSocket.values()){
                s.close();
            }

            System.out.println("========== Game end ==========");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 플레이어에게 숫자를 부여하는 함수
     *  ex) 총 player가 5명일 때
     *       - Skrull은 4나 5를 가짐
     *       - Terran은 나머지 숫자를 가짐
     */
    public static void giveRole(){
        // 중복을 제거하여 숫자 배열 만들기
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        while (numbers.size() < currentPlayer) {
            int rand = (int) (Math.random() * currentPlayer) + 1;   // 랜덤 숫자 생성
            if (!numbers.contains(rand))  numbers.add(rand);        // 중복이 아닐 때 숫자 저장
        }

        // player 이름 List를 가져옴(keySet())
        List<String> names = new ArrayList<String>(playerSocket.keySet());


        // Stream을 이용하여 이름과 숫자를 하나씩 mapping해 playerNum(플레이어 이름-숫자)에 저장
        names.stream()
                .forEach(name -> playerNum.put(name, numbers.get(names.indexOf(name))));


        // skrull을 의미할 숫자를 랜덤으로 선정
        skrullNum = (int) (Math.random() * 2) + currentPlayer - 1;
    }



    /**
     * 투표에서 표를 가장 많이 얻었거나, Skrull에게 지목된 Player을 처형하는 함수
     * - 사망하면 플레이어가 가지고 있는 숫자 값을 -1로 변경
     * @param vict : 처형될 플레이어의 이름
     */
    public static void killPlayer(String vict){
        // 플레이어가 게임에 존재하고 && 플레이어가 죽지 않았을 때
        if(playerNum.keySet().contains(vict) && playerNum.get(vict) != -1){
            isKilled = true;                        // 플레이어가 죽음
            victim = vict;
            playerNum.replace(vict, -1);            // playerNum(플레이어 이름-숫자) 에서 숫자 값을 -1로 변경
            currentPlayer--;                        // 플레이하는 플레이어 1 감소
            System.out.println(vict + " is killed");
        }
        else{
            isKilled = false;                       // 아무도 죽지 않음
            victim = null;
        }
    }


    /**
     * 투표 결과를 처리하는 함수
     *  - 최다 득표자가 한명일 때 처형
     *  - for문을 그냥 사용하는 것이 더 효율적이지만 수업시간에 배운 stream을 활용해서 구성
     *  - voteCount : (플레이어 이름 & 득표 수) Map
     */
    public static void voteResult(){
        // 최다 득표 수
        int max = voteCount.values().stream().max(Integer::compareTo).orElse(0);

        // 최다 득표 수의 배열
        List<String> maxVotedList = voteCount.entrySet().stream()
                                    .filter(e -> e.getValue() == max).map(Map.Entry::getKey)
                                    .collect(Collectors.toList());

        // 최다 득표자가 한명일 때 처형
        if(maxVotedList.size() == 1) killPlayer(maxVotedList.get(0));

        // 득표수를 초기화
        voteCount.forEach((s, n) -> voteCount.replace(s, 0));

    }


    /**
     * 낮 & 밤이 끝날 때 마다 게임 종료 여부를 판별하는 함수
     * - skrull이 없을 때 : Terran 승리
     * - skrull만 남았을 때 : Skrull 승리
     * - playerNum : (플레이어 이름 - 숫자) Map
     * @return FinishFlag : 게임의 상태를 나타내는 enum class
     */
    public static FinishFlag finishCheck(){
        // skrull을 의미하는 숫자가 없을 때(= -1일 때, skrull이 처형되었음을 의미)
        if(!playerNum.containsValue(skrullNum)){
            return FinishFlag.TERRANWIN;            // Terran 승리
        }
        // 플레이어가 한명 남았을 때 && skrull을 의미하는 숫자가 있을 때(skrull이 살아있음을 의미)
        else if(currentPlayer == 1 && playerNum.containsValue(skrullNum)){
            return FinishFlag.SKRULLWIN;            // Skrull 승리
        }
        else {
             return FinishFlag.NOTFINISHED;         // 게임이 진행 중임
        }
    }
}
