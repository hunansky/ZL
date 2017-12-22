package com.wdtx.littlebell.other;

import java.util.Iterator;
import java.util.Random;

/**
 * Created by wending on 2017/6/28.
 */

public class DataGenerate implements Iterator<byte[]> {

    private int tempPos = 0;
    private int heartPos = 0;
    private int period = 5;
    private Random random = new Random();

    //15
    private short temp[] = {
            3200,3250,3300,3350,3400,
            3450,3500,3550,3600,3650,
            3700,3750,3800,3850,3900
    };

    //10
    private short heartRate[] = {
            100,90,80,70,60,70,80,90,100,110
    };

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public byte[] next() {
        short[] src = new short[10];
        src[0] = (short) (temp[tempPos] + random.nextInt() % 100 - 50);
        src[1] = (short) (heartRate[heartPos] + random.nextInt() % 20 - 10);
        PosPlus();
        return toByteArray(src);
    }

    private void PosPlus() {
        period++;
        if (period == 5){
            period = 0;
            heartPos = ++heartPos % 10;
            tempPos = ++tempPos % 15;
        }
    }

    private byte[] toByteArray(short[] src) {

        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i] >> 8);
            dest[i * 2 + 1] = (byte) (src[i] >> 0);
        }

        return dest;
    }

}
