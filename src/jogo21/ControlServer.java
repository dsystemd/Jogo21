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
    Integer ListaDeConfirmados = 0;
    Integer nJogador = 0;
    Usuario JogadorAtual;

    public ControlServer(ServerGUI server, int porta) {
        try {
            this.server = server;
            this.porta = porta;
            serverSocket = new DatagramSocket(porta);
            ListaUsuarios = new ArrayList<>();

        } catch (SocketException ex) {
            Logger.getLogger(ControlServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ServerGUI server;
    int porta;
    List<Usuario> ListaUsuarios;

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
                        AtualizaLista();
                        break;
                    case 3:/*
                         for (int n = 0; n < ListaUsuarios.size(); n++) {
                         if (receivePacket.getPort() == ListaUsuarios.get(n).getPorta()
                         && receivePacket.getAddress().getHostAddress().equals(ListaUsuarios.get(n).getIp())) {
                         ListaUsuarios.get(n).setJogando(true);
                         }
                         }
                         AtualizaLista();*/

                        ListaDeConfirmados++;
                        if (ListaDeConfirmados == ListaUsuarios.size()) {

                            server.getText_area().append("Iniciando Jogo\n");
                            Jogo();

                        }
                        break;

                    case 4:
                        String nome = "";
                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            if (receivePacket.getPort() == ListaUsuarios.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaUsuarios.get(n).getIp())) {
                                nome = ListaUsuarios.get(n).getNome();
                            }
                        }

                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            Enviar(ListaUsuarios.get(n).getIp(), "54#" + nome + ": " + protocolo[1], ListaUsuarios.get(n).getPorta());
                        }

                    case 5:
                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            if (receivePacket.getPort() == ListaUsuarios.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaUsuarios.get(n).getIp())) {
                                PedirCarta(ListaUsuarios.get(n));
                            }
                        }

                    case 6:
                        for (int n = 0; n < ListaUsuarios.size(); n++) {
                            if (receivePacket.getPort() == ListaUsuarios.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaUsuarios.get(n).getIp())) {
                                PassarVez(ListaUsuarios.get(n));
                            }
                        }

                    case 7:
                        DesistirPartida();
                        break;

                }
            }
        } catch (IOException ex) {

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

        for (Usuario nome : ListaUsuarios) {

            if (nome.getJogando() == false) {
                nomes = nomes + nome.getNome() + ";";
            }
            if (nome.getJogando() == true) {
                nomesjogando = nomesjogando + nome.getNome() + ";";
            }
        }

        for (int n = 0; n < ListaUsuarios.size(); n++) {
            Enviar(ListaUsuarios.get(n).getIp(), "51#" + nomes.substring(0, nomes.length() - 1) + "#" + nomesjogando, ListaUsuarios.get(n).getPorta());
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
        carta.valor = valor;
        Jogador.Baralho.addCarta(carta);
    }

    public void DarCarta(Usuario Jogador) {
        Carta carta = new Carta();
        carta.valor = aleatoriar(1, 13);
        Jogador.Baralho.addCarta(carta);
    }

    public int aleatoriar(int minimo, int maximo) {
        Random random = new Random();
        return random.nextInt((maximo - minimo) + 1) + minimo;
    }

    public void DistribuirCartasIniciais() {
        JogadorAtual = ListaUsuarios.get(nJogador);
        for (int n = 0; n < ListaUsuarios.size(); n++) {
            DarCarta(ListaUsuarios.get(n), 0);
            DarCarta(ListaUsuarios.get(n));
        }

    }

    public void Jogada(Usuario JogadorAtual) {
        if (JogadorAtual.passouvez) {
            ProximoJogador();
        }
        if (JogadorAtual.pediucarta) {
            DarCarta(JogadorAtual);
        }
        JogadorAtual.pediucarta = false;
        JogadorAtual.passouvez = false;
    }

    public void ProximoJogador() {
        nJogador = nJogador++;
        JogadorAtual = ListaUsuarios.get(nJogador);
    }

    public void Jogo() {

        DistribuirCartasIniciais();
        String msg = "";
        for (int n = 0; n < ListaUsuarios.size(); n++) {
            ArrayList<Carta> aux = ListaUsuarios.get(n).Baralho.getBaralho();
            for (int i = 0; i < aux.size(); i++) {
                msg = msg.concat(String.valueOf(aux.get(i).getValor()));
                msg = msg.concat(ListaUsuarios.get(n).getNome());
                msg = msg.concat(";");
            }

        }
        System.out.println(msg);
        for (int n = 0; n < ListaUsuarios.size(); n++) {
            Enviar(ListaUsuarios.get(n).getIp(), "52#" + msg, ListaUsuarios.get(n).getPorta());
        }

        for (int n = 0; n < ListaUsuarios.size(); n++) {
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
