import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;


public class RingElection {

	public static final int Base_Port=1400;
	public static final int procCount=5;
	
	JFrame w;
	
	public int my_PRIO;
	LinkedList<rProcess> ring;
	Messenger ms;
	rProcess me;
	rProcess coordinator;
	boolean I_STARTED_ELECTION;
	boolean I_AM_INITIATOR;
	
	RingElection(int myPrio){
		this.my_PRIO=myPrio;
		I_AM_INITIATOR=false;
		I_STARTED_ELECTION=false;
		loadProcesses();
		
		ms=new Messenger(this); //to listen
		
		(new Thread(){			//to speak
			@Override
			public void run() {
				// TODO Auto-generated method stub
				w=new JFrame("Process" + me.priority);
				w.setSize(400, 400);
				w.setLayout(new FlowLayout());
				JButton btnElect=new JButton("ELECTION");
				w.add(btnElect);
				btnElect.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						startElection();
						//JOptionPane.showMessageDialog(new JFrame(), "Eggs are not supposed to be green.");
					}
				});
				
				w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				w.setVisible(true);
			}
			
		}).start();
	}
	
	void loadProcesses(){
		me=new rProcess("localhost",Base_Port+my_PRIO,my_PRIO);
		ring=new LinkedList<rProcess>();
		for(int i=1;i<procCount;i++){
			int round=my_PRIO + i;
			round=round % procCount;
			ring.add(new rProcess("localhost",Base_Port+round,round));
		}
	}
	
	void startElection(){ //When we want t initiate election
		LinkedList<rProcess> lst=new LinkedList<rProcess>();
		lst.addLast(me);
		I_STARTED_ELECTION=true;
		if(forwardMessage("E",lst)==false){
			coordinator=me;
			I_STARTED_ELECTION=false;
			System.out.println("Nobody alive. I am cordinator");
		}
	}
	
	boolean forwardMessage(String msg,LinkedList<rProcess> lst){
		rProcess tmp;
		for(int i=0;i<ring.size();i++){
			tmp=ring.get(i);
			if(ms.send(tmp.site,tmp.port,msg,lst) == true){
				System.out.println("Sent " + msg + " to " + tmp.port);
				return true;
			}
			System.out.println("Sending :" + msg + " to " + tmp.port + " failed. Sending to next in ring.");
		}
		return false;
	}
	
	void receivedEMessage(LinkedList<rProcess> lst){ //predessor sent e msg
		if(I_STARTED_ELECTION == true){
			//find out highest priority
			rProcess max=me;
			for(int i=0;i<lst.size();i++){
				rProcess tmp=lst.get(i);
				if(tmp.priority>max.priority){
						max=tmp;
				}
			}
			
			lst=new LinkedList<rProcess>();
			lst.add(max);
			I_AM_INITIATOR=true;
			System.out.println("IAM:" + me.port + " Just elected " + max.port);
			coordinator=max;
			forwardMessage("C",lst);
		}else{
			lst.addLast(me);
			forwardMessage("E",lst);
		}
	}
	
	public void receivedMessage(String msg,LinkedList<rProcess> lst){
		if(msg.equalsIgnoreCase("E")){
			receivedEMessage(lst);
		}else if(msg.equalsIgnoreCase("C")){
			rProcess tmp=lst.get(0);
			coordinator=tmp;
			System.out.println("New Coordinator : " + coordinator.port);
			if(!I_AM_INITIATOR){
				forwardMessage("C",lst);
			}
		}
	}
	
	public static void main(String args[]){
		new RingElection(0);
	}
}
