package com.example.basketball.enums;

import java.util.Objects;

public enum PlayerPosition {

    POINT_GUARD("PG"),
    SHOOTING_GUARD("SG"),
    SMALL_FORWARD("SF"),
    POWER_FORWARD("PF"),
    CENTER("C");

    private final String position;
    PlayerPosition(String pos) {
        this.position = pos;
    }

    public static boolean isValidPosition(String pos){
        for(PlayerPosition position : PlayerPosition.values()){
            if(Objects.equals(pos, position.toString())){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return position;
    }


}
