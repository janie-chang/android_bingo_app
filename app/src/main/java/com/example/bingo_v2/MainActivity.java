package com.example.bingo_v2;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bingo_v2.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//MainActivity類別是程式主體，繼承AppCompatActivity
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //宣告變數
    GridLayout gridLayout;
    private ActivityMainBinding binding;
    //預設判斷模式選擇
    private boolean isInputMode = false;
    //預設格子數跟賓果數
    private int currentGridSize = 3;
    private int currentBingoCount = 1;

    //覆寫才能執行自訂的訪法
    @Override
    //onCreat是啟動後會馬上執行的方法
    protected void onCreate(Bundle savedInstanceState) {
        //呼叫父類別的onCreate來完成必須執行的方法
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        //載入layout
        setContentView(view);

        //塞入預設值
        binding.editGridSize.setText(String.valueOf(currentGridSize));
        binding.editBingoCount.setText(String.valueOf(currentBingoCount));

        //預設模式為遊戲
        binding.modeSwitch.setChecked(false);
        isInputMode = false;

        gridLayout = binding.gridLayout;
        gridLayout.setColumnCount(currentGridSize);
        gridLayout.setRowCount(currentGridSize);

        //生成按鈕
        initButtons(currentGridSize);

        //switch切換模式綁定
        binding.modeSwitch.setOnClickListener(this);

        //亂數按鈕綁定 (輸入模式)
        binding.randomBtn.setOnClickListener(v -> {

            if (!isInputMode) { return; }

            String editNum = binding.editNum.getText().toString().trim();

            if (editNum.isEmpty()) {
                Toast.makeText(this, "請輸入數字", Toast.LENGTH_SHORT).show();
                return;
            }

            int maxNum = Integer.parseInt(editNum);
            int requiredMinNum = currentGridSize * currentGridSize;
            if (maxNum < requiredMinNum) {
                Toast.makeText(this, "數字要大於" + requiredMinNum, Toast.LENGTH_SHORT).show();
                return;
            }

            //輸入模式下重新亂數填入，但不影響模式
            refillRandomNumbers(maxNum);
        });

        binding.startGameBtn.setOnClickListener(v -> {
            String gridSizeStr = binding.editGridSize.getText().toString().trim();
            String bingoCountStr = binding.editBingoCount.getText().toString().trim();

            if (gridSizeStr.isEmpty() || bingoCountStr.isEmpty()) {
                Toast.makeText(this, "請輸入格子數和賓果數", Toast.LENGTH_SHORT).show();
                return;
            }

            int newGridSize = Integer.parseInt(gridSizeStr);
            int newBingoCount = Integer.parseInt(bingoCountStr);

            if (newGridSize < 2) {
                Toast.makeText(this, "格子數至少為2", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newBingoCount < 1) {
                Toast.makeText(this, "賓果數至少為1", Toast.LENGTH_SHORT).show();
                return;
            }

            currentGridSize = newGridSize;
            currentBingoCount = newBingoCount;

            //模式切回遊戲
            isInputMode = false;
            binding.modeSwitch.setChecked(false);

            gridLayout.setColumnCount(newGridSize);
            gridLayout.setRowCount(newGridSize);

            initButtons(newGridSize);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //生成亂數
    private List<Integer> generateRandomNumbers(Integer gridSize) {
        int total = gridSize*gridSize;
        int maxNum = Math.max(total, 30);
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= maxNum; i++) { numbers.add(i); }
        //隨機排序物件
        Collections.shuffle(numbers);
        return numbers.subList(0, total);
    }

    private void initButtons(int gridSize) {
        gridLayout.removeAllViews();
        int total = gridSize*gridSize;

        String editNumStr = binding.editNum.getText().toString().trim();
        int maxNum = editNumStr.isEmpty() ? Math.max(total, 30) : Integer.parseInt(editNumStr);
        if (maxNum < total) { maxNum = total; }

        List<Integer> numbers = new ArrayList<>();

        for (int i = 1; i <= maxNum; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        List<Integer> picked = numbers.subList(0, total);

        for (int i = 1; i <= total; i++) {
            Button button = new Button(this);
            button.setText(picked.get(i));
            button.setTag(false);
            button.setBackgroundColor(Color.parseColor("#f2efde"));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 220;
            params.setMargins(8, 8, 8, 8);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                if (isInputMode) {
                    showInputDialog(button);
                }else {
                    handleButtonClick(button);
                }
            });

            gridLayout.addView(button);
        }
    }

    //根據模式處理按鈕點擊事件
    private void handleButtonClick(Button button) {
        boolean clicked = (boolean) button.getTag();
        clicked = !clicked;
        button.setTag(clicked);
        button.setBackgroundColor(clicked ? Color.parseColor("#caaa72") : Color.parseColor("#f2efde"));

        checkWin();
    }

    //判斷獲勝
    private void checkWin() {
        int size = currentGridSize;
        int bingoLine = 0;

        //橫排
        for (int r = 0; r < size; r++) {
            boolean line = true;
            for (int c = 0; c < size; c++) {
                Button button = (Button) gridLayout.getChildAt(r * size + c);
                if (!(boolean) button.getTag()) { line = false; break; }
            }
            if (line) { bingoLine++; }
        }

        //直排
        for (int c = 0; c < size; c++) {
            boolean line = true;
            for (int r = 0; r < size; r++) {
                Button button = (Button) gridLayout.getChildAt(r * size + c);
                if (!(boolean) button.getTag()) { line = false; break; }
            }
            if (line) { bingoLine++; }
        }

        //左上到右下
        boolean diag1 = true;
        for (int i = 0; i < size; i++) {
            Button button = (Button) gridLayout.getChildAt(i * size + i);
            if (!(boolean) button.getTag()) { diag1 = false; break; }
        }
        if (diag1) { bingoLine++; }

        //右上到左下
        boolean diag2 = true;
        for (int i = 0; i < size; i++) {
            Button button = (Button) gridLayout.getChildAt(i * size + (size - 1 - i));
            if(!(boolean) button.getTag()) { diag2 = false; break; }
        }
        if (diag2) { bingoLine++; }

        if (bingoLine >= currentBingoCount) {
            Intent intent = new Intent(MainActivity.this, WinActivity.class);
            startActivity(intent);
            finish();
;        }
    }

    //跳出輸入框
    private void showInputDialog(Button button) {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint("請輸入1-30");

        //塞入文字輸入框
        new AlertDialog.Builder(this)
                .setTitle("修改數字")
                .setView(editText)
                .setPositiveButton("確認", (dialog, which) -> {
                    String input = editText.getText().toString().trim();
                    if (input.isEmpty()) { return; }

                    int newNum;
                    try {
                        newNum = Integer.parseInt(input);
                    } catch (NumberFormatException e) {
                        return;
                    }

                    //檢查重複
                    if (isDuplicate(newNum, button)) {
                        Toast.makeText(this, "數字重複", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    button.setText(String.valueOf(newNum));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    //檢查輸入值重複
    private boolean isDuplicate(int num, Button self) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            if (button == self) continue;
            int existing = Integer.parseInt(button.getText().toString());
            if (existing == num) return true;
        }

        return false;
    }

    private void refillRandomNumbers(int maxNum) {
        Log.d("maxNum:", String.valueOf(maxNum));
        int total = gridLayout.getChildCount();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= maxNum; i++) { numbers.add(i); }
        Collections.shuffle(numbers);

        for (int i = 0; i < total; i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setText(String.valueOf(numbers.get(i)));
            button.setTag(false);
            button.setBackgroundColor(Color.parseColor("#f2efde"));
        }
    }

    //設定點擊事件
    @Override
    public void onClick(View view) {
        if (R.id.modeSwitch == view.getId() ) {
            isInputMode = binding.modeSwitch.isChecked();
            Log.d("isInputMode :", isInputMode ? "輸入" : "遊戲");

            for (int i = 0; i < gridLayout.getChildCount(); i++) {
                Button button = (Button) gridLayout.getChildAt(i);
                button.setTag(false);
                button.setBackgroundColor(Color.parseColor("#f2efde"));
            }
        }
    }
}