/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogo21;

/**
 *
 * @author deny_domin
 */
public class Usuario {
    
    int porta;
    String ip;
    String nome;
    Baralho Baralho = new Baralho();
    
    boolean passouvez;
    boolean pediucarta;
    public Usuario(int porta, String ip, String nome) {
        this.porta = porta;
        this.ip = ip;
        this.nome = nome;
    }

    
    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
    
    
    
}
