/**
 *
 *  @author Ossowski Marcin S16425
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.xml.internal.ws.client.SenderException;

public class ChatClient {
	
	String host;
	int port;
	String id;
	SocketChannel channel;
	StringBuilder widokKlienta;
	final Thread wywolanie = new Thread(this::run);
	final Lock lock = new ReentrantLock();
	InetSocketAddress inetSocketAddress;


	public ChatClient(String host, int port, String id) {
		inetSocketAddress = new InetSocketAddress(host,port);
		this.id=id;
		widokKlienta = new StringBuilder("=== " + id + " chat view\n");
	}
	
	public void login() {
		
		try {
			channel = SocketChannel.open(inetSocketAddress);
			channel.configureBlocking(false);
//			while(!channel.finishConnect()) {
//				Thread.sleep(1000);
//			}
			send("log in " + id);
			wywolanie.start();
			
		}catch (UnknownHostException e) {
			System.err.println("Uknown host " + host);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void logout() {
		send("log out" + "#");
		try {
			lock.lock();
			wywolanie.interrupt();
		}finally {
			lock.unlock();
		}
		
	}
	
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        int bytesRead = 0;
        while (!wywolanie.isInterrupted()) {
            do {
                try {
                    lock.lock();
                    bytesRead = channel.read(buffer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } while (bytesRead == 0 && !wywolanie.isInterrupted());
            buffer.flip();
            String response = StandardCharsets.UTF_8.decode(buffer).toString();
            widokKlienta.append(response);
            buffer.clear();
        }
	}
	
	
	

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void send(String s) {
		try {
			Thread.sleep(100);
			channel.write(StandardCharsets.UTF_8.encode(s + "#"));
			Thread.sleep(100);
			
		}catch (IOException  | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String getChatView() {
		return widokKlienta.toString();
	}
}
