package br.edu.ffb.pedro.aulafacilaluno.payload;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonIgnore;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.ffb.pedrosilveira.easyp2p.payloads.Payload;

@JsonObject
public class Quiz extends Payload {
    @JsonIgnore
    public static final String TYPE = "quiz";

    @JsonField
    public boolean isLeader;

    public Quiz() {
        super(TYPE);
    }
}
