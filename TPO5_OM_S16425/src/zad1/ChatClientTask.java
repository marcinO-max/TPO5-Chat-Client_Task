/**
 *
 *  @author Ossowski Marcin S16425
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.omg.CORBA.PUBLIC_MEMBER;

public class ChatClientTask extends FutureTask<ChatClient>{

	public ChatClientTask(Callable<ChatClient> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}

	public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
		// TODO Auto-generated method stub
		return new ChatClientTask(()-> {
//			@Override
//			public ChatClient call() throws Exception{
			try {
				c.login();
				if(wait != 0) {
					Thread.sleep(wait);
				}
				for(String msg : msgs) {
					c.send(msg);
					if(wait != 0)
						Thread.sleep(wait);
				}
				c.logout();
				if(wait !=  0) {
					Thread.sleep(wait);
				}
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			return c;
//			}
		});

		
	}
	
	public ChatClient getClient() {
		try {
			return this.get();
		}catch (InterruptedException  | ExecutionException e) {
			e.printStackTrace();
			return null;
			// TODO: handle exception
		}
	}
}
