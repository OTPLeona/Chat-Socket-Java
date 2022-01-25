import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList <ConnectionHandler> connections;//declaracion de variables
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){//conexiones de clientes
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run (){//conexion con el puerto 
        try {
            server = new ServerSocket(1919);
            pool = Executors.newCachedThreadPool();
            while(!done){
                Socket client = server.accept();//aceptar clientes
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);//accion donde los agrega
                pool.execute(handler);
            }
        } catch (Exception e){
            shutdown();//cerrar en caso de que sea neceario o se necesite
        }
    }

    public void broadcast(String message){//mensaje de aviso en caso de que alguien se conecte
        for (ConnectionHandler ch : connections){
            if (ch != null){
                ch.sendmessage(message);
            }
        }
    }

    public void shutdown(){//cierre del server
        try {
        done = true;
        pool.shutdown();
         if (!server.isClosed()){
             server.close();
            }
            for (ConnectionHandler ch : connections){//cerrar todas las conexiones
                ch.shutdown();
            }
        }catch (IOException e){
            //ignore
        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;//declaracion de mas variables
        private BufferedReader in;
        private PrintWriter out;
        private String nombre;
        
        public ConnectionHandler(Socket client){//conexion con el cliente
            this.client = client;
        }

        @Override
        public void run(){//correr servidor
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("pucha tu nombre: ");//primer anuncio y confirmacion de entrada al server
                nombre = in.readLine();//lectura del nombre
                System.out.println(nombre + " conectao!");//mensaje de confirmacion 
                broadcast(nombre + " a entrao!");//aviso a tod@s de una nueva conexion
                String message;
                while ((message = in.readLine()) != null){
                    if (message.startsWith("/nombre ")){//comando para cambiar el nombre
                        String[] messageSplit = message.split(" ");
                        if(messageSplit.length == 2){
                            broadcast(nombre + " se cambio el nombre a " + messageSplit[1]);//anuncio (all) de cambio de nombre
                            System.out.println(nombre + " se cambio el nombre a " + messageSplit[1]);
                            nombre = messageSplit[1];
                            out.println("cambio de nombre exitoso a " + nombre);//confirmacion de la operacion
                        } else {
                            out.println("nombre no proporcionado!");//negacion de la operacion
                        }
                    } else if (message.startsWith("/salir")){//comando para salir
                        broadcast(nombre + " dejo el chat!");//mensaje de salida del server
                        shutdown();
                    } else {
                        broadcast(nombre + ": " + message);
                    }
                }
            }catch (IOException e){
                shutdown();
            }
        }

        public void sendmessage(String message){
            out.println(message);
        }

        public void shutdown (){//otro apartado para cerrar el servidor
            try {
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
                }
            }catch (IOException e){
                //ignore
            }
        }
    }

    public static void main(String[] args){//main para correr el programa
        Server server = new Server();
        server.run();
    }
}