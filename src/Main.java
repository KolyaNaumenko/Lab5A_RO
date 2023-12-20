import java.util.Arrays;
import java.util.Random;
import static java.lang.System.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class Recruits implements Runnable {
    private static final AtomicBoolean end = new AtomicBoolean(false);
    private static final List<Boolean> endPart = new ArrayList<>();
    private final int[] recruits;
    private final Barrier barrier;
    private final int partN;
    private final int leftN;
    private final int rightN;

    public Recruits(int[] recruits, Barrier barrier, int partN, int leftN, int rightN) {
        this.recruits = recruits;
        this.barrier = barrier;
        this.partN = partN;
        this.leftN = leftN;
        this.rightN = rightN;
    }

    public static void fillP(int numberOfParts) {
        for (int i = 0; i < numberOfParts; i++) {
            endPart.add(false);
        }
    }

    public void run() {
        while (!end.get()) {
            boolean currentPartFinished = endPart.get(partN);
            if (!currentPartFinished) {
                out.println("Part "+ partN + ": " + Arrays.toString(recruits));
                boolean isFormatted = true;
                for (int i = leftN; i < rightN - 1; i++) {
                    if (recruits[i] != recruits[i+1]){
                        recruits[i] *= -1;
                        isFormatted = false;
                    }
                }
                if(isFormatted) {
                    finish();
                }
            }
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void finish() {
        out.println("Part " + partN + " has finished sorting.");
        endPart.set(partN, true);
        for (boolean part: endPart) {
            if (!part) {
                return;
            }
        }
        end.set(true);
    }
}

class Barrier {
    private int partiesAtStart;
    private int partiesAwait;

    public Barrier(int parties) {
        this.partiesAtStart = parties;
        this.partiesAwait = parties;
    }

    public synchronized void await() throws InterruptedException {
        partiesAwait--;
        if(partiesAwait > 0) {
            this.wait();
        }

        partiesAwait = partiesAtStart;
        notifyAll();
    }
}

public class Main {
    private static final int size = 100;
    private static final int parts = 2;
    private static final Barrier barrier = new Barrier(parts);
    private static final Thread[] threads = new Thread[parts];
    private static final int [] recruits = new int[size];
    private static final Random random = new Random(currentTimeMillis());

    public static void main(String[] args) {
        Recruits.fillP(parts);
        fillR();
        Threads();
        out.println("Result: " + Arrays.toString(recruits));
    }

    private static void fillR() {
        for(int i = 0; i < size; i++) {
            if(random.nextBoolean()) {
                recruits[i] = 1;
            } else {
                recruits[i] = -1;
            }
        }
    }

    private static void Threads() {
        for(int i = 0; i < threads.length; i++){
            threads[i] = new Thread(new Recruits(recruits, barrier, i, i * 50, (i + 1) * 50));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}