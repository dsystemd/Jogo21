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
import java.util.Timer;
import java.util.TimerTask;
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
    String JogadorStart = "";
    int vencedor;
    int pontuacaovencedor = 0;
    Usuario jogadordavez;

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
                int nma = sentence.length();
                switch (numprot) {
                    case 1:
                        if (protocolo.length != 2) {
                            Enviar(receivePacket.getAddress().getHostAddress(), "99#ERRO_NICK_INVALIDO#", receivePacket.getPort());
                            break;

                        } else if (protocolo[1].contains(";")) {
                            Enviar(receivePacket.getAddress().getHostAddress(), "99#ERRO_NICK_INVALIDO#", receivePacket.getPort());
                            break;
                        } else if (protocolo[1].length() > 10) {
                            Enviar(receivePacket.getAddress().getHostAddress(), "99#ERRO_NICK_EXTENSO#", receivePacket.getPort());
                            break;
                        }
                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            if (protocolo[1] == ListaJogadores.get(n).getNome()) {
                                Enviar(receivePacket.getAddress().getHostAddress(), "99#ERRO_NICK_REPETIDO#", receivePacket.getPort());
                                break;
                            }
                        }

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
                        if(ListaUsuarios.size()<2){
                            Enviar(receivePacket.getAddress().getHostAddress(), "99#ERRO_POUCOS_JOGADORES#", receivePacket.getPort());
                            break;
                        }
                        
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
                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            if (receivePacket.getPort() == ListaJogadores.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaJogadores.get(n).getIp())) {
                                jogadordavez = ListaJogadores.get(n);
                            }
                        }
                        if (jogadordavez == ListaJogadores.get(indexPlayInteger)) {
                            PedirCarta(ListaJogadores.get(indexPlayInteger));
                        }

                        break;

                    case 6:
                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            if (receivePacket.getPort() == ListaJogadores.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaJogadores.get(n).getIp())) {
                                jogadordavez = ListaJogadores.get(n);
                            }
                        }
                        if (jogadordavez == ListaJogadores.get(indexPlayInteger)) {
                            ListaJogadores.get(indexPlayInteger).jogou = true;
                            Selecionar();
                            Jogada(ListaJogadores.get(indexPlayInteger));
                        }

                        break;
                    case 7:

                        if (jogadordavez == ListaJogadores.get(indexPlayInteger)) {
                            ListaUsuarios.add(ListaJogadores.get(indexPlayInteger));
                            ListaJogadores.remove(ListaUsuarios.get(indexPlayInteger));
                            AtualizaLista();
                            Selecionar();
                            Jogada(ListaJogadores.get(indexPlayInteger));
                        }

                        break;
                    case 8:
                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            if (receivePacket.getPort() == ListaJogadores.get(n).getPorta()
                                    && receivePacket.getAddress().getHostAddress().equals(ListaJogadores.get(n).getIp())) {
                                ListaJogadores.get(n).online = true;
                            }
                        }
                        
                        /*for (int n = 0; n < ListaJogadores.size(); n++) {
                          / if (ListaJogadores.get(n).getOnline()==false) {
                                //ListaJogadores.remove(n);
                            }

                        }*/
                        for (int n = 0; n < ListaJogadores.size(); n++) {
                            ListaJogadores.get(n).online = false;

                        }

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ControlServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
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

    public void Selecionar() throws InterruptedException {
        int contadorjogadas = 0;
        for (int n = 0; n < ListaJogadores.size(); n++) {
            if (ListaJogadores.get(n).jogou == true) {
                contadorjogadas++;
            }

        }

        if (contadorjogadas != ListaJogadores.size()) {
            if (indexPlayInteger < ListaJogadores.size() - 1) {
                indexPlayInteger++;
            } else {
                indexPlayInteger = 0;
            }
            for (int n = 0; n < ListaJogadores.size(); n++) {
                Enviar(ListaJogadores.get(n).getIp(), "55#" + indexPlayInteger, ListaJogadores.get(n).getPorta());
            }/*
             for (int n = 10; n > 0; n--) {
             if (ListaJogadores.get(indexPlayInteger).jogou==false)  {
             for (int i = 0; i < ListaJogadores.size(); i++) {
             Enviar(ListaJogadores.get(i).getIp(), "56#" + n, ListaJogadores.get(i).getPorta());
             }
             Thread.sleep(1 * 1000);
             }

             }*/
            /*if(ListaJogadores.get(indexPlayInteger).jogou==false){
             ListaJogadores.get(indexPlayInteger).jogou=true;
             Selecionar();
             }*/

        } else {
            FimDoJogo();
        }
    }

    public void PedirCarta(Usuario Jogador) throws InterruptedException {
        Jogador.pediucarta = true;
        Jogador.jogou = true;
        Jogada(ListaJogadores.get(indexPlayInteger));

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
        for (int n = 0; n < ListaJogadores.size(); n++) {
            DarCarta(ListaJogadores.get(n), 0);
            DarCarta(ListaJogadores.get(n));
        }

    }

    public void Jogada(Usuario JogadorAtual) throws InterruptedException {

        if (JogadorAtual.pediucarta) {
            DarCarta(JogadorAtual);
            MostrarCartas();
            Contagem();
        }
        JogadorAtual.pediucarta = false;
        JogadorAtual.passouvez = false;
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

    public void Jogo() throws InterruptedException {

        DistribuirCartasIniciais();

        MostrarCartas();
        Selecionar();
        for (int n = 0; n < ListaJogadores.size(); n++) {
            Jogada(ListaJogadores.get(indexPlayInteger));
        }
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public void Contagem() throws InterruptedException {
        int presencaAS[] = new int[ListaJogadores.size()];
        int contas = 0;
        int pontos[] = new int[ListaJogadores.size()];
        for (int n = 0; n < ListaJogadores.size(); n++) {
            ArrayList<Carta> cartas = ListaJogadores.get(n).Baralho.getBaralho();
            for (int q = 0; q < cartas.size(); q++) {
                if (cartas.get(q).valorpublico != 1) {
                    if (cartas.get(q).valorpublico == 11 || cartas.get(q).valorpublico == 12 || cartas.get(q).valorpublico == 13) {
                        pontos[n] = pontos[n] + 10;
                    } else {
                        pontos[n] = pontos[n] + cartas.get(q).valorprivado;
                    }

                } else {
                    presencaAS[contas] = n;
                    contas++;
                }

            }

        }
        if (contas > 0) {

            for (int n = 0; n < presencaAS.length; n++) {
                if (pontos[presencaAS[n]] + 11 == 21) {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 11;
                } else if (pontos[presencaAS[n]] + 1 == 21) {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 1;
                } else if (pontos[presencaAS[n]] + 11 < 21) {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 11;
                } else {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 1;
                }
            }
        }
        for (int q = 0; q < ListaJogadores.size(); q++) {
            if (pontos[q] > 21 && q == indexPlayInteger) {
                for (int n = 0; n < ListaJogadores.size(); n++) {
                    Enviar(ListaJogadores.get(n).getIp(), "54#Jogador " + ListaJogadores.get(q).getNome() + " estourou!", ListaJogadores.get(n).getPorta());
                }
                Selecionar();
            }

        }

    }

    private void FimDoJogo() {
        int presencaAS[] = new int[ListaJogadores.size()];
        int contas = 0;
        for (int n = 0; n < ListaJogadores.size(); n++) {
            Enviar(ListaJogadores.get(n).getIp(), "54#Jogo Finalizado!", ListaJogadores.get(n).getPorta());
        }

        int pontos[] = new int[ListaJogadores.size()];
        for (int n = 0; n < ListaJogadores.size(); n++) {
            ArrayList<Carta> cartas = ListaJogadores.get(n).Baralho.getBaralho();
            for (int q = 0; q < cartas.size(); q++) {
                if (cartas.get(q).valorpublico != 1) {
                    if (cartas.get(q).valorpublico == 11 || cartas.get(q).valorpublico == 12 || cartas.get(q).valorpublico == 13) {
                        pontos[n] = pontos[n] + 10;
                    } else {
                        pontos[n] = pontos[n] + cartas.get(q).valorprivado;
                    }

                } else {
                    presencaAS[contas] = n;
                    contas++;
                }

            }

        }
        if (contas > 0) {

            for (int n = 0; n < presencaAS.length; n++) {
                if (pontos[presencaAS[n]] + 11 == 21) {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 11;
                } else if (pontos[presencaAS[n]] + 1 == 21) {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 1;
                } else if (pontos[presencaAS[n]] + 11 < 21) {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 11;
                } else {
                    pontos[presencaAS[n]] = pontos[presencaAS[n]] + 1;
                }
            }
        }

        for (int n = 0; n < ListaJogadores.size(); n++) {
            if (pontos[n] == 21) {
                pontuacaovencedor = pontos[n];
                vencedor = n;
            }
        }
        if (pontuacaovencedor == 0) {
            for (int n = 0; n < ListaJogadores.size(); n++) {
                Enviar(ListaJogadores.get(n).getIp(), "54#Ninguem venceu!", ListaJogadores.get(n).getPorta());
            }

            for (int n = 0; n < ListaJogadores.size(); n++) {
                if (pontos[n] < 21 && pontos[n] > pontuacaovencedor) {
                    vencedor = n;
                    pontuacaovencedor = pontos[n];
                }
            }
            if (pontos[vencedor] < 21) {
                for (int n = 0; n < ListaJogadores.size(); n++) {
                    Enviar(ListaJogadores.get(n).getIp(), "54#Quem chegou mais perto foi:" + ListaJogadores.get(vencedor).nome, ListaJogadores.get(n).getPorta());
                }
            }

        } else {
            for (int n = 0; n < ListaJogadores.size(); n++) {
                Enviar(ListaJogadores.get(n).getIp(), "54#Vencedor:" + ListaJogadores.get(vencedor).nome + "-> " + pontos[n] + "\n", ListaJogadores.get(n).getPorta());
            }
        }
        for (int n = 0; n < ListaJogadores.size(); n++) {
            Enviar(ListaJogadores.get(n).getIp(), "54#Sua pontuação:" + ListaJogadores.get(n).nome + "-> " + pontos[n] + "\n", ListaJogadores.get(n).getPorta());
        }
        for (int n = 0; n < ListaJogadores.size(); n++) {
            ListaUsuarios.add(ListaJogadores.get(n));
        }

        ListaJogadores.removeAll(ListaUsuarios);
        indexPlayInteger = null;

        AtualizaLista();
    }
}
