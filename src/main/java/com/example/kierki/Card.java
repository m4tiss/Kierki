package com.example.kierki;


import java.io.Serializable;

public class Card implements Serializable {

    private int clientID;
    private String symbol;
    private int value; //2-10, 11-walet 12-dama 13-krol 14-as


    public Card(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
        this.clientID = 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card otherCard = (Card) obj;
        return value == otherCard.value && symbol.equals(otherCard.symbol);
    }

    public String getSymbol() {
        return symbol;
    }

    public int getValue() {
        return value;
    }

    public void displayCard() {
        System.out.println(value + " " + symbol+" "+clientID);
    }
    public void setClientID(int clientID){
        this.clientID=clientID;
    }

    public int getClientID() {
        return clientID;
    }
}
