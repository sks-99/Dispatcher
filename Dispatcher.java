import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.ArrayList;

import java.util.LinkedList;
import java.util.Queue;


//Coded by Sumeet Sandhu


public class Dispatcher {
	public static int CurrentCPUTime;
	public static int PreviousCPUTime;
	
	public static int RunningProcess;
	
	public class PCB{
		int process_ID;
		String state;
		int TotalTimeReady;
		int TotalTimeRunning;
		int TotalTimeBlocked;
		
		public PCB() {
			process_ID=0;
			TotalTimeReady=0;
			TotalTimeRunning=0;
			TotalTimeBlocked=0;
			state="";
			
		}
		
		//Process ID
		public int getPID() {
			return process_ID;
		}
		public void setPID(int value_PID) {	
			this.process_ID= value_PID;
		}
		
		//State
		public String getState() {
			return state;
		}
		public void setState(String value_state) {
			this.state= value_state;
		}

		//Total Time Ready
		public int getTotalTimeReady() {
			return TotalTimeReady;
		}
		public void setTotalTimeReady(int timeReady) {
			this.TotalTimeReady= timeReady;
		}
		
		//Total Time Running
		public int getTotalTimeRunning() {
			return TotalTimeRunning;
		}
		public void setTotalTimeRunning(int timeRunning) {
			this.TotalTimeRunning= timeRunning;
		}
		
		//Total Time Blocked
		public int getTotalTimeBlocked() {
			return TotalTimeBlocked;
		}
		public void setTotalTmeBlocked(int timeBlocked) {
			this.TotalTimeBlocked= timeBlocked;
		}	
	}
	

		public static void main(String[] args) {
			Scanner scanner = new Scanner(new InputStreamReader(System.in));
			ArrayList<String> commands = new ArrayList<String>();
	
			//Reads Input from user and puts it in Array list
			while(true) {
			    String nextLine = scanner.nextLine();
			    if ( nextLine.equals("") ) {
			       break;
			    }
			    commands.add(nextLine); 
			}
			
			
			//Split Line to find (time, events, PID)
			String temp= commands.get(0);
			String[] splited = temp.split("\\s+");
	
			String inputEvent;
			int inputResource;
			int inputPID;
			
			ArrayList<Integer> Resource1 = new ArrayList<Integer>();
			ArrayList<Integer> Resource2 = new ArrayList<Integer>();
			ArrayList<Integer> Resource3 = new ArrayList<Integer>();
			Queue<Integer> ReadyQueue= new LinkedList<Integer>();
			ArrayList<PCB> ProcessTable = new ArrayList<PCB>();
			
			
			//Update system for Idle Process
			CurrentCPUTime=0;
			Dispatcher D1= new Dispatcher();
			Dispatcher.PCB process_PCB_0= D1.new PCB();
			process_PCB_0.setPID(0);
			process_PCB_0.setState("Running");
			process_PCB_0.setTotalTimeRunning(0);
			//Add PCB Process 0 into Process Table
			ProcessTable.add(0, process_PCB_0);
			RunningProcess=0;
			
			//Go through Input Lines & Update System
			for(int i=0; i<commands.size(); i++) {
				temp= commands.get(i);
				splited = temp.split("\\s+");
				PreviousCPUTime= CurrentCPUTime;
				//Update Current time in System
				CurrentCPUTime= Integer.parseInt(splited[0]);
				
				//Check token of split string for event type
				inputEvent= splited[1];
				
				if(inputEvent.equals("C" )) { //Input Event =  Create
					//Update all Processes Time
					UpdateProcesses(ProcessTable, CurrentCPUTime, PreviousCPUTime);
					
					//Input PID
					inputPID= Integer.parseInt(splited[2]);	
					//Create PCB
					Dispatcher.PCB process_PCB= D1.new PCB();
					//Set PID
					process_PCB.setPID(inputPID);
					
					//Set State
					if(RunningProcess==0) { //If Idle process is running, new process->state becomes running
						ProcessTable.get(0).setState("Ready");
						
						process_PCB.setState("Running");
						ProcessTable.add(process_PCB);
						RunningProcess= inputPID;
					}
					else {//If other process is already running, new process state set to ready
						
						process_PCB.setState("Ready");
						ProcessTable.add(process_PCB);
						//Update Queue
						ReadyQueue.add(inputPID);
					}	
				}
				
				if(inputEvent.equals("T")) {//Input Event = Timer Interrupt
					//Update all processes for new time
					UpdateProcesses(ProcessTable, CurrentCPUTime, PreviousCPUTime);
					
					//Running PID-> Ready State
					int Old_Running_PID= RunningProcess;
					
					
					
					if(!ReadyQueue.isEmpty()) {
						
						int Old_Ready_PID = ReadyQueue.remove(); //Remove previous ready pid from queue
						ReadyQueue.add(Old_Running_PID); //Add previous running pid to queue
						RunningProcess= Old_Ready_PID; //Add previous ready pid to RunningProcess
						
						//Update State of Previous Running PID to Ready
						int RunningIndex= SearchProcessTable(Old_Running_PID, ProcessTable);
						ProcessTable.get(RunningIndex).setState("Ready");
						
						//Update State of Previous Ready PID to Running
						int ReadyIndex= SearchProcessTable(Old_Ready_PID, ProcessTable);
						ProcessTable.get(ReadyIndex).setState("Running");
					}
					else {
						//Empty Ready Queue
						//Running Process goes back into CPU since Ready Queue is empty
						RunningProcess= Old_Running_PID;
					}
					
					

				}
				

				if(inputEvent.equals("R" )) { //Input Event = Resource
					//Update all processes for new time
					UpdateProcesses(ProcessTable, CurrentCPUTime, PreviousCPUTime);
					//Input Resource number
					inputResource= Integer.parseInt(splited[2]);
					//Input PID
					inputPID= Integer.parseInt(splited[3]);
					//Search Process Table for input PID
					int inputIndex= SearchProcessTable(inputPID, ProcessTable);
					//Update Running Process State to Blocked
					ProcessTable.get(inputIndex).setState("Blocked");
					//Add Blocked Process into list according to resource input
					if(inputResource==1) 
						Resource1.add(inputPID);
					
					if(inputResource==2) 
						Resource2.add(inputPID);
					
					if(inputResource==3) 
						Resource3.add(inputPID);
		
					//Update RunningProcess
					//If ReadyQueue contains process, change its state to Running
					if(!ReadyQueue.isEmpty()) {
						int NewRunningProcessPID= ReadyQueue.remove();
						RunningProcess= NewRunningProcessPID;
						int update_index= SearchProcessTable(NewRunningProcessPID, ProcessTable);
						ProcessTable.get(update_index).setState("Running");
					}
					else {
						//if Ready Queue is empty and Now running Process is empty since I changed state to blocked
						//Process 0 starts running again
						RunningProcess=0;
						ProcessTable.get(0).setState("Running");
						
						
						
					}
					//Process 0 starts running if there is no RunningProcess
					
					
					
					
				}
		
				//Input Event = Interrupt        (Block->Ready)
				if(inputEvent.equals("I")) {
					//Update all processes for new time
					UpdateProcesses(ProcessTable, CurrentCPUTime, PreviousCPUTime);
					//Input Resource number
					inputResource= Integer.parseInt(splited[2]);
					//Input PID
					inputPID= Integer.parseInt(splited[3]);
					
					if(inputResource==1) {
						int inputIndex= SearchList(inputPID, Resource1);
						Resource1.remove(inputIndex); //unblock Process
	
					}
					
					if(inputResource==2) {
						int inputIndex= SearchList(inputPID, Resource2);
						Resource2.remove(inputIndex); //unblock Process
	
					}
					if(inputResource==3) {
						int inputIndex= SearchList(inputPID, Resource3);
						Resource3.remove(inputIndex); //unblock Process
	
					}
					
					int processTable_index= SearchProcessTable(inputPID, ProcessTable);
					
					//Ready Queue is Empty, new State of process is Running
					if(ReadyQueue.peek()==null) {
						ProcessTable.get(processTable_index).setState("Running");
						
						ProcessTable.get(0).setState("Ready");
					}
					else {
						//Ready Queue is not Empty, new state of process is ready
						ReadyQueue.add(inputPID);
						//Update State
						ProcessTable.get(processTable_index).setState("Ready");
					}
					
				}
				
				//Input Event =  Exit
				if(inputEvent.equals("E")) {
					//Update all processes for new time
					UpdateProcesses(ProcessTable, CurrentCPUTime, PreviousCPUTime);
					//Input PID
					inputPID= Integer.parseInt(splited[2]);	
					//Change State from Running to Exit
					int Table_index= SearchProcessTable(inputPID, ProcessTable);
					ProcessTable.get(Table_index).setState("Exit");
					//Update Queue
					if( !ReadyQueue.isEmpty() ) {
						int RunPID= ReadyQueue.peek();
						ReadyQueue.remove();
						RunningProcess= RunPID;
						int newRunningIndex= SearchProcessTable(RunningProcess, ProcessTable);
						ProcessTable.get(newRunningIndex).setState("Running");
					}
					else {
						//ReadyQueue is empty
						RunningProcess=0;
						ProcessTable.get(0).setState("Running");
					}
					

				}
				
			}
			
			
			//Sort ProcessTable to Ascending Order
			for (int i = 0; i < ProcessTable.size(); i++) {     
	            for (int j = i+1; j < ProcessTable.size(); j++) {  
	               if(ProcessTable.get(i).getPID() > ProcessTable.get(j).getPID()) {  
	            	  PCB val= ProcessTable.get(i);
	            	  ProcessTable.set(i, ProcessTable.get(j));
	            	  ProcessTable.set(j, val);  
	               }     
	            }     
	        } 
			
			
			//Print ProcessTable Statistic from PCB
			//<process id> <total time running> <total time ready> <total time blocked>
			
			for(int i=0; i<ProcessTable.size();i++) {

				int printPID= ProcessTable.get(i).process_ID;
				int printRunTime= ProcessTable.get(i).TotalTimeRunning;
				int printReadyTime= ProcessTable.get(i).TotalTimeReady;
				int printBlockedTime= ProcessTable.get(i).TotalTimeBlocked;
				
				if(ProcessTable.get(i).getPID()==0) {
					System.out.println(printPID + " " + printRunTime);
					
				}
				
				else {
					System.out.println(printPID + " " + printRunTime + " "+ printReadyTime + " "+ printBlockedTime);
				}
			}
			
			}
		
		public static void UpdateProcesses(ArrayList<PCB> ProcessTable, int CurrentCPUTime, int PreviousCPUTime) {
			int addTime;
			int newTime;
			int PreviousTime;
			
			for(int x=0; x<ProcessTable.size();x++) {
				if(ProcessTable.get(x).getState()=="Running" ) {
					addTime= CurrentCPUTime - PreviousCPUTime;
					PreviousTime= ProcessTable.get(x).getTotalTimeRunning();
					
					newTime= PreviousTime + addTime;
					ProcessTable.get(x).setTotalTimeRunning(newTime);
					
					
				}
				if(ProcessTable.get(x).getState()=="Ready" && ProcessTable.get(x).getPID()!=0) {
					addTime= CurrentCPUTime - PreviousCPUTime;
					PreviousTime= ProcessTable.get(x).getTotalTimeReady();
					
					newTime= PreviousTime + addTime;
					ProcessTable.get(x).setTotalTimeReady(newTime);	
				}
				
				if(ProcessTable.get(x).getState()=="Blocked" && ProcessTable.get(x).getPID()!=0) {
					addTime= CurrentCPUTime-PreviousCPUTime;
					PreviousTime= ProcessTable.get(x).getTotalTimeBlocked();
					
					newTime= PreviousTime+addTime;
					ProcessTable.get(x).setTotalTmeBlocked(newTime);	
				}
				
				addTime=0;
				PreviousTime=0;
				newTime=0;	
			
			}
			
		}
		
		public static int SearchProcessTable(int InputPID, ArrayList<PCB> ProcessTable) {
			int i=0;
			int inputIndex;
			while(true) {
				if(ProcessTable.get(i).getPID()==InputPID) {
					inputIndex=i;
					break;
				}
				else {
					i++;
				}
			}
			return inputIndex;
		}
		
		public static int SearchList(int InputPID, ArrayList<Integer> ResourceList) {
			int i=0;
			int inputIndex;
			while(true) {
				if(ResourceList.get(i)==InputPID) {
					inputIndex=i;
					break;
				}
				else {
					i++;
				}
			}
			return inputIndex;
		}
		
		
		
	
	
		
	}

	
	


