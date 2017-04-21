package br.edu.ffb.pedro.aulafacilaluno.events;

public class MessageEvent {
    public static final String EXIT_APP = "Sair do app";
    public final String message;

    public MessageEvent(String message) {
        this.message = message;
    }
}
