package com.example.kierki;
import java.io.Serializable;


/**
 * Klasa reprezentująca kartę w grze.
 * <p>
 * Karty mają symbole (np. kier, karo, trefl, pik) i wartości
 * (2-10, walet, dama, król, as). Każda karta może być przypisana
 * do konkretnego klienta (clientID).
 * </p>
 *
 * @version 1.0
 */
public class Card implements Serializable {

    private int clientID;
    private String symbol;
    private int value;


    /**
     * Konstruktor tworzący nową kartę o określonym symbolu i wartości.
     *
     * @param symbol symbol karty (np. kier, karo, trefl, pik)
     * @param value  wartość karty (2-10, walet, dama, król, as)
     */
    public Card(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
        this.clientID = 0;
    }

    /**
     * Metoda porównująca dwie karty pod względem wartości i symbolu.
     *
     * @param obj inny obiekt do porównania
     * @return true, jeśli karty są identyczne; false w przeciwnym razie
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card otherCard = (Card) obj;
        return value == otherCard.value && symbol.equals(otherCard.symbol);
    }

    /**
     * Metoda zwracająca symbol karty.
     *
     * @return symbol karty
     */
    public String getSymbol() {
        return symbol;
    }


    /**
     * Metoda zwracająca wartość karty.
     *
     * @return wartość karty
     */
    public int getValue() {
        return value;
    }


    /**
     * Metoda wyświetlająca informacje o karcie (wartość, symbol, klientID).
     */
    public void displayCard() {
        System.out.println(value + " " + symbol+" "+clientID);
    }


    /**
     * Metoda ustawiająca identyfikator klienta, do którego przypisana jest karta.
     *
     * @param clientID identyfikator klienta
     */
    public void setClientID(int clientID){
        this.clientID=clientID;
    }


    /**
     * Metoda zwracająca identyfikator klienta, do którego przypisana jest karta.
     *
     * @return identyfikator klienta
     */
    public int getClientID() {
        return clientID;
    }
}
