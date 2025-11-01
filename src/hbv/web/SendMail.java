package hbv.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendMail {

    public static void sendConfirmation(final String appointmentId, final String center,
                                        final String date, final String timeSlot,
                                        final String vaccine, final String userId) {
        // Faire le message
        final String message = appointmentId + "|" + center + "|" + date + "|" + timeSlot + "|" + vaccine + "|" + userId;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                Socket sock = null;
                PrintWriter pw = null;
                BufferedReader br = null;
                try {
                    sock = new Socket("localhost", 2222);
                    pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
                    br = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                    // Sende die Nachricht an den Server
                    pw.println(message);

                   
                    br.readLine();
                } catch (IOException e) {
                    // Fehler
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                        if (pw != null) {
                            pw.close();
                        }
                        if (sock != null) {
                            sock.close();
                        }
                    } catch (IOException e) {
                        // Fehler
                    }
                }
            }
        });
        thread.start();
    }

}
