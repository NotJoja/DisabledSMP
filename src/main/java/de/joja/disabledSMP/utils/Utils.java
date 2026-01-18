package de.joja.disabledSMP.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Utils {

    public static final Random random = new Random();

    public static int[] asIntArray(List<Integer> integerList) {
        int[] ints = new int[integerList.size()];
        for (int i = 0; i < integerList.size(); i++)
            ints[i] = integerList.get(i);
        return ints;
    }

    public static List<Integer> asIntegerList(int[] ints, int cap) {
        List<Integer> integerList = new ArrayList<>(cap);
        for (int i = 0; i < cap; i++)
            integerList.add(ints[i]);
        return integerList;
    }
}
