package it.kytech.bowwarfare.net;

import java.net.ServerSocket;
import java.net.Socket;

import it.kytech.bowwarfare.BowWarfare;

public class Webserver extends Thread {

    @Override
    public void run() {
        try {
            ServerSocket st = new ServerSocket(8080);

            while (!BowWarfare.isDisabling()) {

                Socket skt = st.accept();

                // Spin off request to a new thread to be handled
                Connection c = new Connection(skt);
                c.start();
                // st.close();
            }
            st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
