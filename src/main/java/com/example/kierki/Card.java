package com.example.kierki;

import javafx.scene.control.Button;

public class Card extends Button {
    private String symbol;
    private int value; //2-10, 11-walet 12-dama 13-krol 14-as


    public Card(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getValue() {
        return value;
    }

}
