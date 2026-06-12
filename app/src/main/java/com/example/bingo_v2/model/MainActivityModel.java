package com.example.bingo_v2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivityModel {

    //產生隨機數字陣列
    public static List<Integer> generateNumbers(int maxNum, int gridSize) {

        int total = gridSize*gridSize;
        if (maxNum < total) { maxNum = total; }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= maxNum; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers);

        return new ArrayList<>(numbers.subList(0, total));
    }


}
