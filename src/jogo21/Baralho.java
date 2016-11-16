/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jogo21;

import java.util.ArrayList;

/**
 *
 * @author ivy
 */
public class Baralho {
    private ArrayList<Carta>Baralho = new ArrayList<Carta>();
    
    public void addCarta(Carta carta){
        getBaralho().add(carta);
    }


    public ArrayList<Carta> getBaralho() {
        return Baralho;
    }
    
    
}
