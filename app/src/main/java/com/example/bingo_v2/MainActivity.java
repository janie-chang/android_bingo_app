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

import com.example.bingo_v2.model.MainActivityModel;

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

        //預設不能編輯賓果盤
        binding.editNum.setEnabled(false);
        binding.editGridSize.setEnabled(false);
        binding.editBingoCount.setEnabled(false);

        //生成按鈕
        initButtons(currentGridSize);

        //switch切換模式綁定
        binding.modeSwitch.setOnClickListener(this);

        //亂數按鈕綁定 (輸入模式)
        binding.randomBtn.setOnClickListener(v -> {

            if (!isInputMode) { return; }

            String editNumStr = binding.editNum.getText().toString().trim();
            String gridSizeStr = binding.editGridSize.getText().toString().trim();
            String bingoCountStr = binding.editBingoCount.getText().toString().trim();

            //判斷輸入格子數
            if (gridSizeStr.isEmpty()) {
                Toast.makeText(this, "請輸入格子數", Toast.LENGTH_SHORT).show();
                return;
            }

            int newGridSize = Integer.parseInt(gridSizeStr);

            if (newGridSize < 2) {
                Toast.makeText(this, "至少輸入2", Toast.LENGTH_SHORT).show();
                return;
            }

            //判斷亂數
            if (editNumStr.isEmpty()) {
                Toast.makeText(this, "請輸入數字", Toast.LENGTH_SHORT).show();
                return;
            }

            int maxNum = Integer.parseInt(editNumStr);
            int requiredMinNum = newGridSize * newGridSize;

            if (maxNum < requiredMinNum) {
                Toast.makeText(this, "數字要大於" + requiredMinNum, Toast.LENGTH_SHORT).show();
                return;
            }

            //判斷輸入賓果數
            if (bingoCountStr.isEmpty()) {
                Toast.makeText(this, "請輸入賓果數", Toast.LENGTH_SHORT).show();
                return;
            }

            int newBingoCount = Integer.parseInt(bingoCountStr);

            if (newBingoCount < 1) {
                Toast.makeText(this, "至少輸入1", Toast.LENGTH_SHORT).show();
                return;
            }

            currentBingoCount = newBingoCount;

            if (newGridSize != currentGridSize) {
                currentGridSize = newGridSize;
                gridLayout.removeAllViews();
                gridLayout.setColumnCount(newGridSize);
                gridLayout.setRowCount(newGridSize);
                initButtons(newGridSize);
            } else {
                refillRandomNumbers(maxNum);
            }
        });

        binding.startGameBtn.setOnClickListener(v -> {
            //模式切回遊戲
            isInputMode = false;
            binding.modeSwitch.setChecked(false);

            binding.editNum.setEnabled(false);
            binding.editGridSize.setEnabled(false);
            binding.editBingoCount.setEnabled(false);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int dp10 = (int) (10 * getResources().getDisplayMetrics().density);
            v.setPadding(
            systemBars.left + dp10,
            systemBars.top + dp10,
            systemBars.right + dp10,
            systemBars.bottom + dp10
            );
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initButtons(int gridSize) {
        gridLayout.removeAllViews();
        int total = gridSize*gridSize;

        String editNumStr = binding.editNum.getText().toString().trim();
        int maxNum = editNumStr.isEmpty() ? Math.max(total, 30) : Integer.parseInt(editNumStr);
        if (maxNum < total) { maxNum = total; }

        //List<Integer> numbers = new ArrayList<>();
        //for (int i = 1; i <= maxNum; i++) {
            //numbers.add(i);
        //}
        //Collections.shuffle(numbers);

        List<Integer> picked = MainActivityModel.generateNumbers(maxNum, gridSize);

        for (int i = 0; i < total; i++) {
            Button button = new Button(this);
            button.setText(String.valueOf(picked.get(i)));
            button.setTag(false);
            button.setBackgroundColor(Color.parseColor("#fdfee9"));

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
        button.setBackgroundColor(clicked ? Color.parseColor("#f8b037") : Color.parseColor("#fdfee9"));

        Log.d("handleButtonClick", "tag=" + clicked);  // 加這行
        checkWin();
    }

    //判斷獲勝
    private void checkWin() {
        int size = currentGridSize;
        int bingoLine = 0;

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button b = (Button) gridLayout.getChildAt(i);
            Log.d("checkWin", "i=" + i + " tag=" + b.getTag());
        }

        //橫排，檢查每一橫排
        //把二為座標轉為一維陣列
        //index=row*欄數+col
        //列=r,欄=c
        for (int r = 0; r < size; r++) {
            boolean line = true;
            for (int c = 0; c < size; c++) {
                Button button = (Button) gridLayout.getChildAt(r * size + c);
                Log.d("checkWin", "  r=" + r + " c=" + c + " tag=" + button.getTag());
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
        //row=i，col=size-1-i
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
;       }else {
            Log.d("checkWin", "bingoLine=" + bingoLine + ", need=" + currentBingoCount);
        }
    }

    //跳出輸入框
    private void showInputDialog(Button button) {
        EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint("請輸入");

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
        Log.d("maxNum ya:", String.valueOf(maxNum));
        int total = gridLayout.getChildCount();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= maxNum; i++) { numbers.add(i); }
        Collections.shuffle(numbers);

        for (int i = 0; i < total; i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setText(String.valueOf(numbers.get(i)));
            button.setTag(false);
            button.setBackgroundColor(Color.parseColor("#fdfee9"));
        }
    }

    //設定點擊事件
    @Override
    public void onClick(View view) {
        if (R.id.modeSwitch == view.getId() ) {
            isInputMode = binding.modeSwitch.isChecked();
            Log.d("isInputMode :", isInputMode ? "輸入" : "遊戲");

            binding.editNum.setEnabled(isInputMode);
            binding.editGridSize.setEnabled(isInputMode);
            binding.editBingoCount.setEnabled(isInputMode);

            for (int i = 0; i < gridLayout.getChildCount(); i++) {
                Button button = (Button) gridLayout.getChildAt(i);
                button.setTag(false);
                button.setBackgroundColor(Color.parseColor("#fdfee9"));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LifeCycle", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LifeCycle", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LifeCycle", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LifeCycle", "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("LifeCycle", "onRestart");

        //gridLayout.removeAllViews();
        //initButtons(currentGridSize);
    }
}