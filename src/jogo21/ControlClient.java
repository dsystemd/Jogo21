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
    String nomesjog[] = null;
    int jogvez;

    public ControlClient(ClientGUI client, Login login) {
        try {
            this.client = client;
            this.login = login;
            clientSocket = new DatagramSocket();
            jogvez = -1;

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

                        String nomes[] = protocolo[1].split(";");
                        client.getArea_espera().setText("");
                        for (String nome : nomes) {
                            client.getArea_espera().append(nome + "\n");
                        }

                        nomesjog = null;
                        if (protocolo.length == 3) {
                            nomesjog = protocolo[2].split(";");
                        }

                        client.getArea_jogando().setText("");
                        if (nomesjog != null) {
                            for (String nome : nomesjog) {
                                client.getArea_jogando().append(nome + "\n");
                            }
                        }

                        break;

                    case 52:/*
                        String cartas[]=null;
                        for(int i = 1;i<=nomesjog.length;i++){
                            cartas = protocolo[i].split(";");
                        }
                        for(int i = 0;i<nomesjog.length;i++){
                            System.out.println("teste cartas"+cartas[i]);
                        }
                        */
                        String cartas[][] = new String[nomesjog.length][];
                        for(int i = 0;i<nomesjog.length;i++){
                            String aux[] = protocolo[i++].split(";");
                            for(int j = 0;j<aux.length;j++){
                                cartas[i][j] = "";
                            }
                            
                        }
                        /*
                        
                        //String teste[] = nomescartas[0].split("(?<=\\D)(?=\\d)");
                        String matriz [][];
                        for (int i = 0; i < nomesjog.length; i++) {
                            for (int j = 0; j < nomescartas.length; j++) {
                                if (nomescartas[i].contains(nomesjog[j])) {
                                    matriz [i][j] = nomescartas[j].
                                }
                            }
                        }
                        
                        for(int i = 0;i<nomesjog.length;i++){
                            client.getArea_jogo().append("Nome: "+nomesjog[i]);
                            for(int j=0;j<cartas[i].length;j++){
                               client.getArea_jogo().append(cartas[i][j]+", ");
                            }
                            client.getArea_jogo().append("\n");
                        }
                        */

                        break;

                    case 54:
                        System.out.println(protocolo[1]);
                        if ((protocolo[1]).contains("iniciou um novo jogo")) {
                            client.getArea_jogo().append(protocolo[1] + "\n");
                        } else if ((protocolo[1]).contains("Seus oponentes sao: -")) {
                            client.getArea_jogo().append(protocolo[1] + "\n");
                        } else {
                            client.getArea_msg().append(protocolo[1] + "\n");
                        }
                        break;

                    case 55:
                        jogvez = Integer.parseInt(protocolo[1]);

                        client.getArea_jogo().append("Vez do jogador:" + nomesjog[jogvez] + "\n");

                        System.out.println(protocolo[1]);
                        break;
                }

//                Enviar("08#");
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
