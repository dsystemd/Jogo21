/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogo21;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author deny_domin
 */
public class ControlServer extends Thread {

    DatagramSocket serverSocket;
    Integer indexPlayInteger;
//    ArrayList<String> arrayJogadores;
    Integer nJogador = 0;
    Usuario JogadorAtual;
    String JogadorStart = "";

    public ControlServer(ServerGUI server, int porta) {
        try {
            this.server = server;
            this.porta = porta;
            serverSocket = new DatagramSocket(porta);
            ListaUsuarios = new ArrayList<>();
            ListaJogadores = new ArrayList<>();
            indexPlayInteger = -1;
//            this.arrayJogadores = new ArrayList<>();
        } catch (SocketException ex) {
            Logger.getLogger(ControlServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ServerGUI server;
    int porta;
    List<Usuario> ListaUsuarios;
    List<Usuario> ListaJogadores;
    static Thread timer = new Thread();

    public void run() {
        try {

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData,
                        receiveData.length);

                server.getText_area().append("Esperando por datagrama UDP na porta " + porta + "\n");

                serverSocket.receive(receivePacket);

                String sentence = new String(receivePacket.getData()).trim();
                server.getText_area().append("Datagrama UDP recebido: '" + sentence + "'\n");

                String protocolo[] = sentence.split("#");
                int numprot = Integer.parseInt(protocolo[0]);

                switch (numprot) {
                    case 1:
                        Usuario usuario = new Usuario(receivePacket.getPort(),
                                receivePacket.getAddress().getHostAddress(), protocolo[1]);
                        usuario.setJogando(false);
                        ListaUsuarios.add(usuario);

                        AtualizaLista();

                        break;

                    case 2:
                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            if (receivePacket.getPort() == ListaUsuarios.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaUsuarios.get(n).getIp())) {
                                ListaUsuarios.remove(n);
                            }
                        }

                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            if (receivePacket.getPort() == ListaJogadores.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaJogadores.get(n).getIp())) {
                                ListaJogadores.remove(n);
                            }
                        }
                        AtualizaLista();
                        break;

                    case 3:
//                        int comecar = 0;
//                        for (int i = 10; i >=0; i--) {
//                            timer.sleep(1000);
//                            System.out.println(i);  
//                            if (i == 0){
//                                comecar = 1;
//                            }
//                        }
                        if (ListaJogadores.size() == 0) {
                            for (int n = 0; n < ListaUsuarios.size(); n++) {
                                ListaJogadores.add(ListaUsuarios.get(n));
                            }
                            for (int n = 0; n < ListaJogadores.size(); n++) {
                                ListaUsuarios.remove(ListaJogadores.get(n));
                            }

                            AtualizaLista();

                            for (int n = 0; n < ListaJogadores.size(); n++) {
                                if (receivePacket.getPort() == ListaJogadores.get(n).getPorta()
                                        && receivePacket.getAddress().getHostAddress().equals(ListaJogadores.get(n).getIp())) {
                                    JogadorStart = ListaJogadores.get(n).getNome();
                                }
                            }

                            for (int n = 0; n < ListaJogadores.size(); n++) {
                                Enviar(ListaJogadores.get(n).getIp(), "54#" + JogadorStart + " iniciou um novo jogo", ListaJogadores.get(n).getPorta());
                            }
                            EnviaOponentes();

                            server.getText_area().append("Iniciando Jogo\n");
                            Jogo();
//                            Selecionar();                                                       
                        }
                        //Enviar 99#

                        break;

                    case 4:
                        String nome = "";
                        String nomejogando = "";
                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            if (receivePacket.getPort() == ListaUsuarios.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaUsuarios.get(n).getIp())) {
                                nome = ListaUsuarios.get(n).getNome();
                            }
                        }

                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            if (receivePacket.getPort() == ListaJogadores.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaJogadores.get(n).getIp())) {
                                nomejogando = ListaJogadores.get(n).getNome();
                            }
                        }

                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            Enviar(ListaUsuarios.get(n).getIp(), "54#" + nome + ": " + protocolo[1], ListaUsuarios.get(n).getPorta());
                        }

                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            Enviar(ListaJogadores.get(n).getIp(), "54#" + nomejogando + ": " + protocolo[1], ListaJogadores.get(n).getPorta());
                        }
                        break;

                    case 5:
                        PedirCarta(ListaJogadores.get(indexPlayInteger));
                        //DarCarta(ListaJogadores.get(indexPlayInteger));         
                    case 6:
                       // Selecionar();
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ControlServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void EnviaOponentes() {
        String nomes = "";

        for (Usuario nome : ListaJogadores) {
            nomes = nomes + nome.getNome() + ";";
        }

        String nomes_separados[] = nomes.split(";");

        for (int n = 0; n < ListaJogadores.size(); n++) {
            String msg1 = "";
            for (int j = 0; j < nomes_separados.length; j++) {
                if (nomes_separados[j].equals(ListaJogadores.get(n).getNome())) {

                } else {
                    System.out.println("nome_separado: " + nomes_separados[j] + " nomes: " + ListaJogadores.get(n).getNome());
                    msg1 = msg1.concat(nomes_separados[j]);
                    msg1 = msg1.concat(" - ");
                }
            }
            Enviar(ListaJogadores.get(n).getIp(), "54#" + "Seus oponentes sao: - " + msg1, ListaJogadores.get(n).getPorta());
        }
    }

    public void Selecionar() {
        if (indexPlayInteger < ListaJogadores.size() - 1) {
            indexPlayInteger++;
        } else {
            indexPlayInteger = 0;
        }
        for (int n = 0; n < ListaJogadores.size(); n++) {
            Enviar(ListaJogadores.get(n).getIp(), "55#" + indexPlayInteger, ListaJogadores.get(n).getPorta());
        }
    }

    public void DesistirPartida() {

    }

    public void PedirCarta(Usuario Jogador) {
        Jogador.pediucarta = true;
    }

    public void PassarVez(Usuario Jogador) {
        Jogador.passouvez = true;
    }

    public void AtualizaLista() {

        String nomes = "";
        String nomesjogando = "";

//        this.arrayJogadores = new ArrayList<>();
        for (Usuario nome : ListaUsuarios) {
            nomes = nomes + nome.getNome() + ";";
        }
        for (Usuario nome : ListaJogadores) {
            nomesjogando = nomesjogando + nome.getNome() + ";";
        }
//                if (nome.getJogando() == false) {
//                    nomes = nomes + nome.getNome() + ";";
//                }
//                if (nome.getJogando() == true) {
//                    nomesjogando = nomesjogando + nome.getNome() + ";";
//                    this.arrayJogadores.add(nome.getNome());
//                }

        for (int n = 0; n < ListaUsuarios.size(); n++) {
            Enviar(ListaUsuarios.get(n).getIp(), "51#" + nomes + "#" + nomesjogando, ListaUsuarios.get(n).getPorta());
//            nomes.substring(0, nomes.length()-1)
        }
        for (int n = 0; n < ListaJogadores.size(); n++) {
            Enviar(ListaJogadores.get(n).getIp(), "51#" + nomes + "#" + nomesjogando, ListaJogadores.get(n).getPorta());
        }
    }

    public void Enviar(String ip, String msg, int porta) {

        try {
            byte[] sendData = new byte[1024];
            InetAddress IPAddress = InetAddress.getByName(ip);

            sendData = msg.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData,
                    sendData.length, IPAddress, porta);

            server.getText_area().append("Enviando Datagrama UDP: '" + msg + "' para IP " + ip + "/porta " + porta + "\n");

            serverSocket.send(sendPacket);

        } catch (Exception e) {

        }
    }

    public void DarCarta(Usuario Jogador, int valor) {
        Carta carta = new Carta();
        int aux = aleatoriar(1, 13);
        carta.valorpublico = 0;
        carta.valorprivado = aux;
        Jogador.Baralho.addCarta(carta);
    }

    public void DarCarta(Usuario Jogador) {
        Carta carta = new Carta();
        int aux = aleatoriar(1, 13);
        carta.valorpublico = aux;
        carta.valorprivado = aux;
        Jogador.Baralho.addCarta(carta);
    }

    public int aleatoriar(int minimo, int maximo) {
        Random random = new Random();
        return random.nextInt((maximo - minimo) + 1) + minimo;
    }

    public void DistribuirCartasIniciais() {
        JogadorAtual = ListaJogadores.get(nJogador);
        for (int n = 0; n < ListaJogadores.size(); n++) {
            DarCarta(ListaJogadores.get(n), 0);
            DarCarta(ListaJogadores.get(n));
        }

    }

    public void Jogada(Usuario JogadorAtual) {
        if (JogadorAtual.passouvez) {
            //Selecionar();
        }
        if (JogadorAtual.pediucarta) {
            DarCarta(JogadorAtual);
            MostrarCartas();
            //Selecionar();
        }
        JogadorAtual.pediucarta = false;
        JogadorAtual.passouvez = false;
    }

    public void ProximoJogador() {
        nJogador = nJogador++;
        JogadorAtual = ListaJogadores.get(nJogador);
    }

    public void MostrarCartas() {
        for (int q = 0; q < ListaJogadores.size(); q++) {
            String msg = "";
            for (int n = 0; n < ListaJogadores.size(); n++) {

                ArrayList<Carta> aux = ListaJogadores.get(n).Baralho.getBaralho();
                int contador = aux.size();
                for (int i = 0; i < aux.size(); i++) {
                    if (ListaJogadores.get(n) != ListaJogadores.get(q)) {
                        msg = msg.concat(String.valueOf(aux.get(i).getValorPublico()));
                    } else {
                        msg = msg.concat(String.valueOf(aux.get(i).getValorPrivado()));
                    }
                    //msg = msg.concat(ListaJogadores.get(n).getNome());
                    if (i == contador - 1) {
                        msg = msg.concat("#");
                    } else {
                        msg = msg.concat(";");
                    }

                }
            }
            Enviar(ListaJogadores.get(q).getIp(), "52#" + msg, ListaJogadores.get(q).getPorta());
        }
    }

    public void Jogo() {

        DistribuirCartasIniciais();

        for (int q = 0; q < ListaJogadores.size(); q++) {
            String msg = "";
            for (int n = 0; n < ListaJogadores.size(); n++) {

                ArrayList<Carta> aux = ListaJogadores.get(n).Baralho.getBaralho();
                int contador = aux.size();
                for (int i = 0; i < aux.size(); i++) {
                    if (ListaJogadores.get(n) != ListaJogadores.get(q)) {
                        msg = msg.concat(String.valueOf(aux.get(i).getValorPublico()));
                    } else {
                        msg = msg.concat(String.valueOf(aux.get(i).getValorPrivado()));
                    }
                    //msg = msg.concat(ListaJogadores.get(n).getNome());
                    if (i == contador - 1) {
                        msg = msg.concat("#");
                    } else {
                        msg = msg.concat(";");
                    }

                }
            }
            Enviar(ListaJogadores.get(q).getIp(), "52#" + msg, ListaJogadores.get(q).getPorta());
        }
        Selecionar();
        for (int n = 0; n < ListaJogadores.size(); n++) {
            Jogada(JogadorAtual);
        }
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }
}
