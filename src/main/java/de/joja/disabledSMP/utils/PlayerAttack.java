package de.joja.disabledSMP.utils;

import java.util.UUID;

public class PlayerAttack {

    UUID attackerUUID;
    long time;

    public PlayerAttack(UUID attackerUUID, long time) {
        this.attackerUUID = attackerUUID;
        this.time = time;
    }

    public UUID getAttackerUUID() {
        return attackerUUID;
    }

    public long getTime() {
        return time;
    }

}
