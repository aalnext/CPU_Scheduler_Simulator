import java.io.*;
import java.util.*;

public class SchedulingAlgorithms {

    private static final int MAX_MEMORY = 1024;

    Queue<PCB> jobQueue = new LinkedList<>();     //queue of all jobs
    Queue<PCB> readyQueue = new LinkedList<>();   
    Queue<PCB> tableQueue = new LinkedList<>();    //for printing the table in order of finished time
    

    // synchronizes access to shared resources
    Object lock = new Object();

    // num of processes
    static int processCounter = 0;

    //to make the job queue in this class and main class the same job queue
    public SchedulingAlgorithms(Queue<PCB> jobQueue) {
        this.jobQueue = jobQueue;
    }

    public static void GannttChart(PCB pcb, int firstNum, int NextNum) {
        System.out.print("| p|" + pcb.id + " |");
        System.out.println(firstNum + " -- " + NextNum);

    }

    // read jobs from .txt file
    public Queue<PCB> readJobsFromFile(String fileName, Queue<PCB> jobs) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {         //reads each line one by one, and repalces ; with :
                line = line.replace(";", ":");
                String[] tokens = line.split(":");
                int id = Integer.parseInt(tokens[0]);                    //changes into Integer
                int burstTime = Integer.parseInt(tokens[1]);
                int memory = Integer.parseInt(tokens[2]);
                //to make sure no process more than 1024 mb enters job queue and we only take 30 or less
                if (memory <= MAX_MEMORY && processCounter<=30) {
                    jobs.add(new PCB(id, burstTime, memory));
                    processCounter++;                
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return jobs;
    }

    public static void Table(Queue<PCB> tableQueue, boolean printTable) {
        PCB p[] = tableQueue.toArray(new PCB[tableQueue.size()]);        //changes to array for ease of use
        if(printTable)
            System.out.println(
                    "Process ID --- Burst Time --- Start Time --- Finish Time --- Turnaround Time --- Waiting Time");
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int actualCounter = 0;
        
        for (int i = 0; i < processCounter; i++) {
            actualCounter += 1;
            totalWaitingTime += p[i].waitingTime;
            totalTurnaroundTime += p[i].turnaroundTime;
            if(printTable)
                System.out.println("    " + p[i].id + "             "
                        + p[i].burstTime + "             " +
                        p[i].startTime + "              " +
                        (p[i].burstTime + p[i].waitingTime + p[i].startTime) + "                " + p[i].turnaroundTime
                        + "                "
                        + p[i].waitingTime);
            
        }

        //for(int i = 0; i<processCounter; i++){
        //    p[i].waitingTime = 0;
        //    p[i].turnaroundTime = 0;
        //}

        
        System.out.println("===============================================================================");
        System.out.println("Calculate Average Waiting Time: " + (totalWaitingTime / (double) actualCounter));
        System.out.println("Calculate Average Turnaround Time: " + (totalTurnaroundTime / (double) actualCounter));
    }


    public void FCFS(boolean printTableGanttChart) {
        // We assume a process starts when it enters the READY queue, NOT the job queue
        synchronized (lock) {
            
            int maxSize = 0;
            int totalTime = 0;
            
            // algorithm ends when the job queue or ready queue ends
            while (!jobQueue.isEmpty() || !readyQueue.isEmpty()) {
                // ends if the job queue ends or the size of all processes is more than 1024
                while (!jobQueue.isEmpty() && maxSize + jobQueue.peek().memory <= MAX_MEMORY) {
                    PCB pro = jobQueue.poll();
                    readyQueue.add(pro);
                    pro.startTime = totalTime;
                    maxSize += pro.memory;
                }

                // take a process from the ready list
                for (PCB pro : readyQueue) {
                    int waitingTime = totalTime - pro.startTime;
                    pro.waitingTime = Math.max(0, waitingTime);
                    if(printTableGanttChart)
                        GannttChart(pro, totalTime, totalTime + pro.remainingTime);

                    totalTime += pro.burstTime;
                    int turnAroundTime = totalTime - pro.startTime;
                    pro.turnaroundTime = turnAroundTime;
                    tableQueue.add(pro);

                    maxSize -= pro.memory;
                }

                
                readyQueue.clear();
                
            }

           
        Table(tableQueue, printTableGanttChart);
        tableQueue.clear();
        jobQueue.clear();
        processCounter = 0;
        
        }
    }

    public void RR(int quantum, boolean printTableGanttChart) {
        // We assume a process starts when it enters the READY queue, NOT the job queue
        synchronized (lock) {

            int q = quantum;
            int totalTime = 0;
            int maxSize = 0;
            PCB pro;

            while (!readyQueue.isEmpty() || !jobQueue.isEmpty()) {
                while (!jobQueue.isEmpty() && maxSize + jobQueue.peek().memory <= MAX_MEMORY) {
                     pro = jobQueue.poll();
                    readyQueue.add(pro);
                    maxSize += pro.memory;
                    pro.startTime = totalTime;
                }

                boolean done = false;
                // ends if the process will finish (remaining time < quant) or when ready queue ends
               
                while (!done && !readyQueue.isEmpty()) {
                     pro = readyQueue.poll();

                    if (pro.remainingTime > 0) {
                        // if remaining time is higher than quant
                        if (pro.remainingTime > q) {
                            if(printTableGanttChart)
                                GannttChart(pro, totalTime, totalTime + q); 
                            totalTime += q;
                            pro.remainingTime -= q;
                            readyQueue.add(pro);
                            // if remaining time lower than quant
                        } else {
                            if(printTableGanttChart)
                                GannttChart(pro, totalTime, totalTime + pro.remainingTime);
                            totalTime += pro.remainingTime;
                            pro.waitingTime = totalTime - pro.burstTime - pro.startTime;
                            pro.remainingTime = 0;
                            pro.turnaroundTime = totalTime - pro.startTime;
                            done = true;
                            maxSize -= pro.memory;
                            readyQueue.add(pro);
                            tableQueue.add(pro);
                        }
                    }
                }
                
            }
            
                Table(tableQueue, printTableGanttChart);
                tableQueue.clear();
                readyQueue.clear();
                jobQueue.clear();
                processCounter = 0;
        }
    }

    public void multi(boolean printTableGanttChart) { //boolean variable is to determine if we print the table/gantt chart

        synchronized (lock) {
           
            Queue<PCB> q0 = new LinkedList<>();
            Queue<PCB> q1 = new LinkedList<>();
            Queue<PCB> q2 = new LinkedList<>();
            int totalTime = 0;
            int maxSize = 0;
            PCB pro;

            while (!readyQueue.isEmpty() || !jobQueue.isEmpty()) {

                while (!jobQueue.isEmpty() && maxSize + jobQueue.peek().memory <= MAX_MEMORY) {
                    pro = jobQueue.poll();
                    readyQueue.add(pro);
                    q0.add(pro);
                    maxSize += pro.memory;
                    pro.startTime = totalTime;
                } // only adds jobs so that it fits memory

                while (!q0.isEmpty() || !q1.isEmpty() || !q2.isEmpty()) {

                    while (!q0.isEmpty()) {
                       
                        pro = q0.poll();
                        readyQueue.poll();    //to empty the process from the ready queue
                        
                        if (pro.remainingTime <= 8) { // pro is done in RR8
                            if(printTableGanttChart)
                                GannttChart(pro, totalTime, totalTime + pro.burstTime);

                            totalTime += pro.burstTime;
                            pro.waitingTime = totalTime - pro.burstTime - pro.startTime;
                            pro.remainingTime = 0;
                            pro.turnaroundTime = totalTime - pro.startTime;
                            maxSize -= pro.memory;
                            tableQueue.add(pro);

                        } else { // pro does RR8 then goes to RR16
                            if(printTableGanttChart)
                                GannttChart(pro, totalTime, totalTime + 8);
                            totalTime += 8;
                            pro.remainingTime -= 8;

                            q1.add(pro);
                        }
                    }
                    while (!q1.isEmpty()) {

                         pro = q1.poll();
                        if (pro.remainingTime <= 16) { // pro finished in RR16
                            if(printTableGanttChart)
                                GannttChart(pro, totalTime, totalTime + pro.remainingTime);
                            totalTime += pro.remainingTime;
                            pro.waitingTime = totalTime - pro.burstTime - pro.startTime;
                            pro.remainingTime = 0;
                            pro.turnaroundTime = totalTime - pro.startTime;
                            maxSize -= pro.memory;
                            tableQueue.add(pro);

                        } else { // pro does RR16 and goes to q2 (FCFS)
                            if(printTableGanttChart)
                                GannttChart(pro, totalTime, totalTime + 16);
                            totalTime += 16;
                            pro.remainingTime -= 16;
                            q2.add(pro);
                        }
                    }

                    while (!q2.isEmpty()) {

                        pro = q2.poll();
                        if(printTableGanttChart)
                            GannttChart(pro, totalTime, totalTime + pro.remainingTime);
                        totalTime += pro.remainingTime;
                        pro.waitingTime = totalTime - pro.burstTime - pro.startTime;
                        pro.remainingTime = 0;
                        pro.turnaroundTime = totalTime - pro.startTime;
                        maxSize -= pro.memory;
                        tableQueue.add(pro);

                    }
                }
                
            }
            
            Table(tableQueue, printTableGanttChart);
            //clear all queues since there might be another algorithm using them next (choice == 4)
            tableQueue.clear();
            readyQueue.clear();
            jobQueue.clear();
            //make the number of processes 0 in case another algorithm will be done (choice == 4)
            processCounter = 0;
        }

    }
}