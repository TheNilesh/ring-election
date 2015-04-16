import java.io.Serializable;


public class rProcess implements Serializable{
	private static final long serialVersionUID = 1L;
	String site;
	int port;
	int priority;
	rProcess(String site, int port, int prio){
		this.site=site;
		this.port=port;
		priority=prio;
	}
	
	public String toString(){
		return site + ":" + port + "[" + priority + "]";
	}
}
