package it.kytech.bowwarfare.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Connection extends Thread {

    BufferedReader in;
    DataOutputStream out;
    Socket skt;
    HashMap<String, String> html = new HashMap<String, String>();

    public Connection(Socket skt) {
        try {
            this.in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            this.out = new DataOutputStream(skt.getOutputStream());
            this.skt = skt;
        } catch (Exception e) {
        }
    }

    @Override
    public void run() {
        try {
            write("ADFSADFDSAF", out, in.readLine());
            skt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getHTML(String pageName) {

    }

    public void parseHTML(String page) {

    }

    public void write(String str, OutputStream out, String useragent) {
        StringBuilder header = new StringBuilder("HTTP/1.0 ");
        header.append("200 OK");
        header.append("\r\n");
        header.append("Connection: close\r\n");
        header.append("Server: BowWarfare v0\r\n");
        header.append("Content-Type: text/html\r\n");
        header.append("\r\n");

        String template = FileCache.getHTML("template", true);

        String[] args = useragent.split(" ")[1].trim().split("/");

        String page = template;

        page = parse(page);

        str = header.toString() + page;

        try {
            out.write(str.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String parse(String page) {
        return page;
    }

}
