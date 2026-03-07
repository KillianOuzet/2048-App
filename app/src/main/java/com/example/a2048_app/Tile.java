package com.example.a2048_app;

import java.util.Random;

public class Tile {
    private int value;


    public Tile(){
        Random random = new Random();
        this.value = random.nextInt(100) < 70 ? 2 : 4;
    }

    public int getValue() { return value; }

    public int merge(Tile tile){
        if (tile.getValue() == this.value){
            this.value *= 2;
            return this.value;
        }
        return 0;
    }

}


