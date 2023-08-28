import java.util.*;

public class Main {
    public static void main(String[] args){
        Queue<PCB> jobQueue = new LinkedList<>();

        SchedulingAlgorithms algorithms = new SchedulingAlgorithms(jobQueue);

        
        //first thread
        Thread readFileThread = new Thread(new Runnable(){
            @Override
            public void run(){
                algorithms.readJobsFromFile("job.txt", jobQueue);  //read jobs
            }
        } );

        try(Scanner scanner = new Scanner(System.in)) {
            System.out.println("Choose on of the following:");
            System.out.println("1- First Come First Serve (FCFS)");
            System.out.println("2- Round Robin (RR)");
            System.out.println("3- Multi-level feedback queue");
            System.out.println("4- Print Average Waiting and Turnaround for all three algorithms");
            try{
                int choice = scanner.nextInt();
                //second thread
                Thread thread = null;
                if(choice == 1){
                    thread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            algorithms.FCFS(true);
                        }
                    });
                }
                else if(choice == 2){
                    //second thread
                    thread = new Thread(new Runnable() {
						@Override
						public void run() {
							algorithms.RR(10, true);
						}
					});
                }
                else if(choice == 3){
                    //second thread
                    thread = new Thread(new Runnable() {
						@Override
						public void run() {
							algorithms.multi(true);
						}
					});
                }
                else if(choice == 4){
                    //secod thread
                    thread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            System.out.println("\nFirst Come First Serve");
                            algorithms.FCFS(true);
                            algorithms.readJobsFromFile("job.txt", jobQueue);
                            System.out.println("\nRound Robin");
                            algorithms.RR(10, true);
                            algorithms.readJobsFromFile("job.txt", jobQueue);
                            System.out.println("\nMulti-level feedback queue");
                            algorithms.multi(true);
                        }
                    });
                }
                else{
                    System.out.println("Invalid choice, try again");
                    return;
                }

                //first thread starts
                readFileThread.start();
                try{
                    //waits until readFileThread finishes reading the file
                    readFileThread.join();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                //second thread starts
                thread.start();
            } catch(Exception e){
                System.out.println("Please enter an integer");
            }
        }
    }
}