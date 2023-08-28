public class PCB {
    public int id;
    public int burstTime;
    public int burst;
    public int memory;
    public int arrivalTime;
    public int startTime;
    public int waitingTime;
    public int turnaroundTime;
    public int remainingTime;

    public PCB(int id, int burstTime, int memory) {
        this.id = id;
        this.burstTime = burstTime;
        this.memory = memory;
        this.arrivalTime = 0;
        this.startTime = 0;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.remainingTime = burstTime;
        this.burst = burstTime;
    }
}
