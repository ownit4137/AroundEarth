import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Player {
    public static void main(String[] args) {
        System.out.println("echo client start");
        try {
            InetAddress localAddress = InetAddress.getLocalHost();

            try(Socket cSocket = new Socket(localAddress, 10000);
                PrintWriter out = new PrintWriter(cSocket.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(cSocket.getInputStream()))
            ){
                System.out.println("connected");
                Scanner scv = new Scanner(System.in);
                while(true){
                    System.out.print("input message : ");
                    String inputLine = scv.nextLine();
                    if("quit".equalsIgnoreCase(inputLine)){
                        break;
                    }
                    out.println(inputLine);
                    String response = br.readLine();
                    System.out.println("server response : " + response);
                }
                scv.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}