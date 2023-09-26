import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class App extends Application {
    static int numPoints = 10000;
    static int numThreads = 10;

    static int n = 1;
    static int l = 0;
    static int ml = 0;

    static int Z = 1;

    static final double a0 = 0.6529;

    FileWriter fileWriter;

    static String[] fileNames = new String[numThreads];
    static ArrayList<Thread> threads = new ArrayList<Thread>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            fileWriter = new FileWriter("points.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numThreads; i++) {
            PointMaker pm = new PointMaker();
            Thread t = new Thread(pm);
            threads.add(t);
            pm.setThread(t);
            fileNames[i] = t.getName();
            Thread.ofVirtual().start(t);
            //System.out.println(t.getName());
        }

        while (threads.size() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        
        }

        combineFiles();

    }

    public void combineFiles() {
        System.out.println("Combining Files");
        try {
            int k = 0;
            for (int i = 0; i < numThreads; i++) {
                Scanner s = new Scanner(new File(fileNames[i] + ".csv"));
                while (s.hasNextLine()) {
                    String t = s.nextLine() + " \n";
                    fileWriter.write(t);
                    k++;
                }
                
            }
            System.out.println(k);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done");

    }

}

class PointMaker implements Runnable {
    Thread t;
    FileWriter runnableWriter;

    public void setThread(Thread t) {
        this.t = t;
    }

    @Override
    public void run() {
        try {
            runnableWriter = new FileWriter(t.getName() + ".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < App.numPoints / App.numThreads; i++) {
            //System.out.println(i);
            boolean again = true;
            while (again == true) {
                double r = Math.random();
                double theta = Math.random() * Math.PI * 2;
                double phi = Math.random() * Math.PI * 2;

                double psi = 0;

                if (App.n == 1 && App.l == 0 && App.ml == 0) {
                    psi = (1 / Math.sqrt(Math.PI)) * Math.pow((App.Z / App.a0), 3.0 / 2)
                            * Math.pow(Math.E, (-(App.Z / App.a0) * r));
                }

                if (Math.pow(psi, 2) <= 10) {
                    again = false;
                    double x = r * Math.sin(theta) * Math.cos(phi);
                    double y = r * Math.sin(theta) * Math.sin(phi);
                    double z = r * Math.cos(theta);

                    String s = String.format("%f, %f, %f\n", x, y, z);

                    try {
                        runnableWriter.write(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("kickback");
                    again = true;
                }
            }

        }
        try {
            runnableWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        App.threads.remove(t);
        t.interrupt();

    }
}
