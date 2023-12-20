import java.util.concurrent.CyclicBarrier;
import java.util.Arrays;
import static java.lang.System.*;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;

class Changer implements Runnable {
    private final Random random = new Random();
    private String str;
    private final CyclicBarrier barrier;
    private final Checker checker;
    private boolean running = true;
    private int count;
    private final int index;

    public Changer(String str, CyclicBarrier barrier, Checker checker, int index){
        this.str = str;
        this.barrier = barrier;
        this.checker = checker;
        this.count = calculator(str);
        this.index = index;
    }

    private int calculator(String str) {
        int count = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == 'A' || str.charAt(i) == 'B'){
                count++;
            }
        }
        return count;
    }

    @Override
    public void run(){
        while(running) {
            int randIndex = random.nextInt(str.length());
            switch (str.charAt(randIndex)) {
                case 'A': {
                    str = str.substring(0, randIndex) + 'C' + str.substring(randIndex + 1);
                    count--;
                    break;
                }
                case 'B': {
                    str = str.substring(0, randIndex) + 'D' + str.substring(randIndex + 1);
                    count--;
                    break;
                }
                case 'C': {
                    str = str.substring(0, randIndex) + 'A' + str.substring(randIndex + 1);
                    count++;
                    break;
                }
                case 'D': {
                    str = str.substring(0, randIndex) + 'B' + str.substring(randIndex + 1);
                    count++;
                    break;
                }
            }
            checker.getInfo(count);
            out.println("Thread #" + this.index + "  " + str + "    " + count);
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            out.println();
            running = checker.isRunning();
        }
    }
}
class Checker {
    private boolean running = true;
    private int counter = 0;
    private final int threads_number;
    private final int[] threadsArr;
    private boolean threads_finished = false;

    public Checker(int threadNum) {
        threads_number = threadNum;
        threadsArr = new int[threadNum];
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void getInfo(int data) {
        threadsArr[counter] = data;
        counter++;
        if (counter == threads_number) {
            notifyAll();
            threads_finished = true;
        }
        while (!threads_finished) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        counter--;
        if (counter == 0) {
            equality();
            threads_finished = false;
        }
    }

    public void equality() {
        boolean isEqual = true;
        Arrays.sort(threadsArr);
        for (int i = 1; i < threadsArr.length - 2; i++) {
            if (threadsArr[i] != threadsArr[i + 1]) {
                isEqual = false;
                break;
            }
        }
        if (isEqual) {
            if (threadsArr[0] == threadsArr[1] || threadsArr[threadsArr.length - 1] == threadsArr[1]) {
                running = false;
                out.println("Are equal");
            }
        }
    }
}

public class Main {
    private static final int threads_number = 4;

    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(threads_number);
        Checker checker = new Checker(threads_number);

        out.println("Threads # |    Line    | Count\n");

        Thread changer1 = new Thread(new Changer("ABCDCDAABCD", barrier, checker, 1));
        Thread changer2 = new Thread(new Changer("AAACAACBBAC", barrier, checker, 2));
        Thread changer3 = new Thread(new Changer("ACDCADCACDC", barrier, checker, 3));
        Thread changer4 = new Thread(new Changer("CDABBABCDAB", barrier, checker, 4));

        changer1.start();
        changer2.start();
        changer3.start();
        changer4.start();
    }
}
