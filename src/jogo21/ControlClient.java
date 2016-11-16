/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogo21;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author deny_domin
 */
public class ControlClient extends Thread {

    ClientGUI client;
    Login login;
    DatagramSocket clientSocket;
    String servidor;
    int porta;
    String nome;

    public ControlClient(ClientGUI client, Login login) {
        try {
            this.client = client;
            this.login = login;
            clientSocket = new DatagramSocket();

        } catch (SocketException ex) {
            Logger.getLogger(ControlClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void run() {

        try {
            
            while (true) {                
                byte[] receiveData = new byte[1024];

                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);
                            
                clientSocket.receive(receivePacket);
                
                String recebido = new String(receivePacket.getData()).trim();
                System.out.println("Datagrama recebido: '" + recebido + "'\n");
                             
                String protocolo[] = recebido.split("#");
                int numprot = Integer.parseInt(protocolo[0]);                               

                switch (numprot) {
                    case 51: 
                        client.setVisible(true);
                        login.setVisible(false);
                        client.getLabel_nome().setText(getNome());
                        
//                        51#Denyson;Fulano#Teste;Carlos;
                        
                        String nomes[] = protocolo[1].split(";");                        
                                              
                        client.getArea_espera().setText("");
                        for (String nome: nomes){                            
                            client.getArea_espera().append(nome + "\n");
                        }
  
                        String nomesjog[] = protocolo[2].split(";");
                        
                        client.getArea_jogando().setText("");
                        for (String nome: nomesjog){                            
                            client.getArea_jogando().append(nome + "\n");
                        }
                    break;
                    
                    case 54:
                        System.out.println(protocolo[1]);
                        client.getArea_msg().append(protocolo[1] + "\n");
                    break;
                }
                
                Enviar("08#");
                

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Enviar(String msg) {

        try {
            byte[] sendData = new byte[1024];

            InetAddress IPAddress = InetAddress.getByName(servidor);

            sendData = msg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);

            System.out.println("Enviando pacote UDP:'" + msg + "'");
            clientSocket.send(sendPacket);

        } catch (Exception e) {

        }
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    

}
