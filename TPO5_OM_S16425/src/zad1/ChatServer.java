/**
 *
 *  @author Ossowski Marcin S16425
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatServer {
	
	Thread thread;
	String host;
	int port;
	ServerSocketChannel serverSocketChannel;
	Selector selector;
	Map <SocketChannel, String> clients; 
	StringBuilder serverLog;
	Lock lock = new ReentrantLock();
	InetSocketAddress inetSocketAddress;
	

	public ChatServer(String host, int port) {
		clients = new HashMap<>();
		serverLog = new StringBuilder();
		inetSocketAddress = new InetSocketAddress(host, port);
		thread = new Thread(()->{
			try {
				selector = Selector.open();
				serverSocketChannel = ServerSocketChannel.open();
				serverSocketChannel.bind(inetSocketAddress);
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				while(!thread.isInterrupted()) {
					selector.select();
					if(thread.isInterrupted()) {
						break;
					}
					Set keys = selector.selectedKeys();
					Iterator iterator = keys.iterator();
					while(iterator.hasNext()) {
						SelectionKey key = (SelectionKey) iterator.next();
						iterator.remove();
						
						
						if(key.isAcceptable()) {
							SocketChannel socketClient = serverSocketChannel.accept();
							socketClient.configureBlocking(false);
							socketClient.register(selector, SelectionKey.OP_READ);
						}
						
						if(key.isReadable()) {
							SocketChannel sChannel = (SocketChannel)key.channel();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							StringBuilder clientRequest = new StringBuilder();
							int readBytes = 0;
							for(int i = readBytes ; i < 1 ; i++) {
								try {
									lock.lock();
									readBytes = sChannel.read(buffer);
									buffer.flip();
									clientRequest.append(StandardCharsets.UTF_8.decode(buffer).toString());
									buffer.clear();
									readBytes = sChannel.read(buffer);
								}catch (Exception e) {
									e.printStackTrace();
								}finally {
									lock.unlock();
								}
							}
						
							
							String [] parts = clientRequest.toString().split("#");
							for(String req : parts) {
								String clientResponse = odpowiedz(sChannel,req);
							
							
							for(Map.Entry<SocketChannel, String> entry : clients.entrySet()) {
								ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(clientResponse);
								entry.getKey().write(byteBuffer);
							}
							}
						}
					}
				}
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private String odpowiedz(SocketChannel sChannel, String req) {
		// TODO Auto-generated method stub
		String response = "";
		if(req.matches("log in .+")) {
			clients.put(sChannel, req.substring(7));
			
			serverLog.append(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss.SSS"))).append(" ").append(req.substring(7)).append(" logged in" + "\n");
			
			response += (req.substring(7) + " logged in" + "\n");
		}
		
		if(req.matches("log out")) {
			serverLog.append(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss.SSS"))).append(" ").append(clients.get(sChannel)).append(" logged out" + "\n");
			
			response += clients.get(sChannel) + " logged out" + "\n";
			
			ByteBuffer buffer = StandardCharsets.UTF_8.encode(response);
			try {
				sChannel.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			clients.remove(sChannel);
		}else {
			serverLog.append(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss.SSS"))).append(" ").append(clients.get(sChannel)).append(": ").append(req+ "\n");
			
			response += clients.get(sChannel) + ": " + req+ "\n";
		}
		
		return response;
	}

	public void startServer() {
		thread.start();
		System.out.println("Server started\n");
	}

	public void stopServer() {
		try {
			lock.lock();
			thread.interrupt();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			selector.close();
			serverSocketChannel.close();
			System.out.println("Server stopped");
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
}
		
	}

	public String getServerLog() {
		// TODO Auto-generated method stub
		return serverLog.toString();
	}
}
