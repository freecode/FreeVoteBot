package org.freecode.irc;

import org.freecode.irc.event.internal.DelegateListener;
import org.freecode.irc.event.internal.RawIrcListener;
import org.freecode.irc.event.internal.RawNoticeProcessor;
import org.freecode.irc.event.internal.RawPrivateMessageProcessor;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: Shivam
 * Date: 16/06/13
 * Time: 22:30
 */
public class IrcConnection implements Runnable {

	private BufferedReader reader;
	private BufferedWriter writer;
	private Socket socket;
	private String host;
	private int port;
	private volatile List<RawIrcListener> listeners;
	private volatile List<DelegateListener> delegateListeners;
	private ScheduledExecutorService executor;
	private Future readerFuture;
	public static final int ERR_NICKNAMEINUSE = 433;
	private ConcurrentLinkedQueue<String> rawLines = new ConcurrentLinkedQueue<>();
	private final ScheduledFuture<?> writerFuture;

	public IrcConnection(final String host, final int port) throws IOException {
		this.host = host;
		this.port = port;
		this.socket = new Socket(host, port);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		listeners = new LinkedList<RawIrcListener>();
		delegateListeners = new LinkedList<DelegateListener>();
		executor = Executors.newScheduledThreadPool(2);
		addListener(new RawPrivateMessageProcessor(this));
		addListener(new RawNoticeProcessor(this));
		readerFuture = executor.scheduleAtFixedRate(this, 100L, 100L, TimeUnit.MILLISECONDS);
		writerFuture = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if(socket.isOutputShutdown()) {
					writerFuture.cancel(true);
				}
				if (!rawLines.isEmpty()) {
					String s = rawLines.poll();
					System.out.println("Out: " + s);
					try {
						writer.write(s);
						writer.newLine();
						writer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}, 100L, 100L, TimeUnit.MILLISECONDS);
	}

	public void joinChannel(String channel) {
		try {
			sendRaw("JOIN :" + channel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addListener(final DelegateListener listener) {
		delegateListeners.add(listener);
	}

	public void removeListener(final DelegateListener listener) {
		delegateListeners.remove(listener);
	}


	public void addListener(final RawIrcListener listener) {
		listeners.add(listener);
	}

	public void removeListener(final RawIrcListener listener) {
		listeners.remove(listener);
	}

	public void register(final String nick, final String user, final String realName) throws IOException {
		sendRaw("NICK " + nick);
		sendRaw("USER " + user + " 0 * :" + realName);
	}

	public <T> List<T> getDelegates(Class<T> type) {
		List<T> list = new LinkedList<T>();
		for (DelegateListener l : delegateListeners) {
			if (type.isAssignableFrom(l.getClass())) {
				list.add((T) l);
			}
		}
		return list;
	}

	public void sendMessage(String target, String message) {
		try {
			sendRaw(String.format("PRIVMSG %s :%s", target, message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendRaw(String s) throws IOException {
		if (s.endsWith("\n")) {
			s = s.replaceAll("[\n\r]", "");
		}
		if (!socket.isOutputShutdown()) {
			rawLines.offer(s);
		}
	}


	public void run() {
		if (socket.isInputShutdown()) {
			readerFuture.cancel(true);
		} else {

			try {
				String raw = reader.readLine();
				System.out.println(raw);
				if (!Character.isLetterOrDigit(raw.charAt(0))) {
					raw = raw.substring(1);
				}
				if (raw.startsWith("PING ")) {
					sendRaw(raw.replaceFirst("PING", "PONG"));
				} else {
					for (RawIrcListener listener : listeners) {
						if (listener.qualifies(raw)) {
							listener.execute(raw);
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public void send(final Transmittable transmittable) {
		try {
			sendRaw(transmittable.getRaw());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendNotice(String target, String message) {
		try {
			sendRaw(String.format("NOTICE %s :%s", target, message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BufferedWriter getWriter() {
		return writer;
	}
}
