import java.util.LinkedList;

public class ActiveThread extends Thread {

	LinkedList<SocketThread> list;

	public ActiveThread(LinkedList<SocketThread> list) {
		this.list = list;
	}

	public void run() {
		SocketThread currentClient;

		while (true) {
			synchronized (list) {
				while (list.isEmpty()) {
					try {
						list.wait();
					} catch (InterruptedException error) {
					}
				}
				currentClient = list.removeFirst();
			}

			try {
				currentClient.run();
			} catch (RuntimeException e) {
				System.err.println("Error running a client Socket thread");
			}
		}
	}

}
