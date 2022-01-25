import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    
    private Socket client;//declaracion de variables
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    
    @Override
    public void run() {//conexion con el socket del servidor y con el VPN
        try{
            client = new Socket("10.10.10.5", 1919);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();//inicio de la conexion

            String inMessage;
            while ((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }//input de mensajes
        }catch(IOException e){
            shutdown();
        }
    }

    public void shutdown(){//accion de cerrar el cliente
        done = true;
        try{
            in .close();
            out.close();
            if (!client.isClosed()){
                client.close();
            }
        }catch (IOException e){
            //ignore 
        }
    }

    class InputHandler implements Runnable{//inputs

        @Override
        public void run (){
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inReader.readLine();//entrada del mensaje
                    if (message.equals("/salir")){//comando para salir 
                        out.println(message);
                        inReader.close();
                        shutdown();//cerrar cliente
                    } else{
                        out.println(message);//mensaje send
                    }
                }
            }catch(IOException e){
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();//main para correr el programa
    }
}
