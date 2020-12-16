import java.util.concurrent.Phaser;

class testP implements Runnable{
    Phaser phaser;
    int sleepTime;

    public testP(Phaser p, int t){
        this.phaser = p;
        sleepTime = t;
    }

    @Override
    public void run()
    {
        try {
            phaser.register();

            System.out.println(Thread.currentThread().getName() + " arrived");

            for(int i = 1; i <= (sleepTime/1000) ;i++){
                System.out.println(Thread.currentThread().getName() + " sleeps " + i + "seconds");
                Thread.sleep(1000);
            }


            phaser.arriveAndAwaitAdvance();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+" after passing barrier");
    }
}



public class phaserTest {
    public static void main(String[] args) throws InterruptedException
    {
        Phaser phaser = new Phaser();
        phaser.register();//register self... phaser waiting for 1 party (thread)
        System.out.println(Thread.currentThread().getName() + " arrived");


        int phasecount = phaser.getPhase();
        System.out.println("Phasecount is "+phasecount);


        new Thread(new testP(phaser, 2000)).start();
        new Thread(new testP(phaser, 4000)).start();
        new Thread(new testP(phaser, 6000)).start();

        Thread.sleep(10000);
        phaser.arriveAndAwaitAdvance();
        System.out.println(Thread.currentThread().getName()+" after passing barrier");


        phasecount = phaser.getPhase();
        System.out.println("Phasecount is "+phasecount);

    }
}

