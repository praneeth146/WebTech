package com.example.calculator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // ─── Views ────────────────────────────────────────────────────────────
    private TextView tvDisplay, tvExpression;
    private Button btnClear, btnSign, btnPercent;
    private Button btnDivide, btnMultiply, btnSubtract, btnAdd, btnEquals;
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    private Button btnDot;
    private ImageButton btnThemeToggle, btnHistory;
    private ConstraintLayout rootLayout;
    private LinearLayout displayArea, historyPanel, historyList;
    private Button btnClearHistory;
    private ViewGroup buttonGrid;

    // ─── State ────────────────────────────────────────────────────────────
    private String currentInput   = "0";
    private String previousInput  = "";
    private String operator       = "";
    private boolean justEvaluated = false;
    private boolean isDarkTheme   = true;
    private boolean historyVisible = false;
    private Button activeOpButton = null;

    private final List<String[]> historyEntries = new ArrayList<>();

    // ─── Shared Preferences key ───────────────────────────────────────────
    private static final String PREF_THEME = "pref_theme";
    private SharedPreferences prefs;

    // ─── Vibrator ─────────────────────────────────────────────────────────
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs     = getSharedPreferences("calc_prefs", MODE_PRIVATE);
        isDarkTheme = prefs.getBoolean(PREF_THEME, true);
        vibrator  = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        bindViews();
        applyTheme(false);
        setupButtonListeners();
        updateDisplay();
    }

    // ─── Bind all views ───────────────────────────────────────────────────
    private void bindViews() {
        rootLayout       = findViewById(R.id.rootLayout);
        tvDisplay        = findViewById(R.id.tvDisplay);
        tvExpression     = findViewById(R.id.tvExpression);
        displayArea      = findViewById(R.id.displayArea);
        historyPanel     = findViewById(R.id.historyPanel);
        historyList      = findViewById(R.id.historyList);
        btnClearHistory  = findViewById(R.id.btnClearHistory);
        buttonGrid       = findViewById(R.id.buttonGrid);

        btnClear    = findViewById(R.id.btnClear);
        btnSign     = findViewById(R.id.btnSign);
        btnPercent  = findViewById(R.id.btnPercent);
        btnDivide   = findViewById(R.id.btnDivide);
        btnMultiply = findViewById(R.id.btnMultiply);
        btnSubtract = findViewById(R.id.btnSubtract);
        btnAdd      = findViewById(R.id.btnAdd);
        btnEquals   = findViewById(R.id.btnEquals);

        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btnDot = findViewById(R.id.btnDot);

        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnHistory     = findViewById(R.id.btnHistory);
    }

    // ─── Setup Listeners ──────────────────────────────────────────────────
    private void setupButtonListeners() {
        // Number buttons
        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                        R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        String[] numVals = {"0","1","2","3","4","5","6","7","8","9"};
        for (int i = 0; i < numIds.length; i++) {
            final String val = numVals[i];
            Button b = findViewById(numIds[i]);
            setupPressAnimation(b);
            b.setOnClickListener(v -> onNumber(val));
        }

        setupPressAnimation(btnDot);
        btnDot.setOnClickListener(v -> onDecimal());

        // Operator buttons
        setupPressAnimation(btnDivide);
        setupPressAnimation(btnMultiply);
        setupPressAnimation(btnSubtract);
        setupPressAnimation(btnAdd);
        btnDivide.setOnClickListener(v   -> onOperator("÷", btnDivide));
        btnMultiply.setOnClickListener(v -> onOperator("×", btnMultiply));
        btnSubtract.setOnClickListener(v -> onOperator("−", btnSubtract));
        btnAdd.setOnClickListener(v      -> onOperator("+", btnAdd));

        // Equals
        setupPressAnimation(btnEquals);
        btnEquals.setOnClickListener(v -> onEquals());

        // Function buttons
        setupPressAnimation(btnClear);
        setupPressAnimation(btnSign);
        setupPressAnimation(btnPercent);
        btnClear.setOnClickListener(v   -> onClear());
        btnSign.setOnClickListener(v    -> onSign());
        btnPercent.setOnClickListener(v -> onPercent());

        // Theme toggle
        btnThemeToggle.setOnClickListener(v -> toggleTheme());

        // History
        btnHistory.setOnClickListener(v -> toggleHistory());

        // Clear history
        btnClearHistory.setOnClickListener(v -> {
            historyEntries.clear();
            historyList.removeAllViews();
        });

        // Long press backspace on display
        tvDisplay.setOnLongClickListener(v -> {
            onBackspace();
            return true;
        });
        tvDisplay.setOnClickListener(v -> onBackspace());
    }

    // ─── Press / Release animation (scale) ───────────────────────────────
    private void setupPressAnimation(View v) {
        v.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                animatePress(view);
                vibrateLight();
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                       event.getAction() == MotionEvent.ACTION_CANCEL) {
                animateRelease(view);
            }
            return false;
        });
    }

    private void animatePress(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.92f).setDuration(80);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.92f).setDuration(80);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    private void animateRelease(View v) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 0.92f, 1f).setDuration(80);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 0.92f, 1f).setDuration(80);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    private void vibrateLight() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(18);
        }
    }

    // ─── Digit input ──────────────────────────────────────────────────────
    private void onNumber(String digit) {
        if (justEvaluated) {
            currentInput   = digit;
            justEvaluated  = false;
            clearActiveOp();
        } else {
            if (currentInput.equals("0") && !digit.equals(".")) {
                currentInput = digit;
            } else {
                if (currentInput.replace("-","").replace(".","").length() < 10) {
                    currentInput += digit;
                }
            }
        }
        btnClear.setText("C");
        animateDisplayIn();
        updateDisplay();
    }

    // ─── Decimal ──────────────────────────────────────────────────────────
    private void onDecimal() {
        if (justEvaluated) {
            currentInput  = "0.";
            justEvaluated = false;
        } else if (!currentInput.contains(".")) {
            currentInput += ".";
        }
        animateDisplayIn();
        updateDisplay();
    }

    // ─── Operator ─────────────────────────────────────────────────────────
    private void onOperator(String op, Button opBtn) {
        if (!previousInput.isEmpty() && !operator.isEmpty() && !justEvaluated) {
            double result = evaluate();
            currentInput  = formatResult(result);
            previousInput = currentInput;
        } else {
            previousInput = currentInput;
        }
        operator      = op;
        justEvaluated = true;
        tvExpression.setText(previousInput + " " + op);
        setActiveOp(opBtn);
        updateDisplay();
    }

    // ─── Equals ───────────────────────────────────────────────────────────
    private void onEquals() {
        if (previousInput.isEmpty() || operator.isEmpty()) return;

        String expr = previousInput + " " + operator + " " + currentInput;
        double result = evaluate();
        String resultStr = formatResult(result);

        tvExpression.setText(expr + " =");
        addToHistory(expr, resultStr);

        currentInput   = resultStr;
        previousInput  = "";
        operator       = "";
        justEvaluated  = true;

        clearActiveOp();
        animateDisplayIn();
        updateDisplay();
    }

    // ─── Clear ────────────────────────────────────────────────────────────
    private void onClear() {
        if (currentInput.equals("0") || currentInput.isEmpty()) {
            // Full AC
            previousInput = "";
            operator      = "";
            tvExpression.setText("");
            clearActiveOp();
        }
        currentInput  = "0";
        justEvaluated = false;
        btnClear.setText("AC");
        animateDisplayIn();
        updateDisplay();
    }

    // ─── Backspace ────────────────────────────────────────────────────────
    private void onBackspace() {
        if (justEvaluated || currentInput.equals("0")) return;
        if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.equals("-")) currentInput = "0";
        } else {
            currentInput = "0";
        }
        animateDisplayIn();
        updateDisplay();
    }

    // ─── Sign toggle ──────────────────────────────────────────────────────
    private void onSign() {
        if (currentInput.equals("0") || currentInput.equals("Error")) return;
        if (currentInput.startsWith("-")) {
            currentInput = currentInput.substring(1);
        } else {
            currentInput = "-" + currentInput;
        }
        updateDisplay();
    }

    // ─── Percent ──────────────────────────────────────────────────────────
    private void onPercent() {
        try {
            double val = Double.parseDouble(currentInput);
            currentInput = formatResult(val / 100.0);
            updateDisplay();
        } catch (NumberFormatException ignored) {}
    }

    // ─── Core evaluation ──────────────────────────────────────────────────
    private double evaluate() {
        try {
            double a = Double.parseDouble(previousInput);
            double b = Double.parseDouble(currentInput);
            switch (operator) {
                case "+": return a + b;
                case "−": return a - b;
                case "×": return a * b;
                case "÷":
                    if (b == 0) {
                        currentInput = "Error";
                        updateDisplay();
                        return 0;
                    }
                    return a / b;
            }
        } catch (NumberFormatException ignored) {}
        return 0;
    }

    // ─── Format result nicely ─────────────────────────────────────────────
    private String formatResult(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return "Error";
        if (val == Math.floor(val) && !Double.isInfinite(val) && Math.abs(val) < 1e10) {
            return String.valueOf((long) val);
        }
        DecimalFormat df = new DecimalFormat("#.##########");
        String s = df.format(val);
        if (s.length() > 12) {
            return String.format("%.6e", val);
        }
        return s;
    }

    // ─── Update main display ──────────────────────────────────────────────
    private void updateDisplay() {
        tvDisplay.setText(currentInput);

        // Auto-resize font based on length
        int len = currentInput.length();
        float sp;
        if (len <= 6)       sp = 64f;
        else if (len <= 9)  sp = 52f;
        else if (len <= 12) sp = 40f;
        else                sp = 30f;
        tvDisplay.setTextSize(sp);
    }

    // ─── Slide-in display animation ───────────────────────────────────────
    private void animateDisplayIn() {
        tvDisplay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.display_slide_in));
    }

    // ─── Operator highlight ───────────────────────────────────────────────
    private void setActiveOp(Button btn) {
        clearActiveOp();
        activeOpButton = btn;
        btn.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.btn_op_active)));
        btn.setTextColor(ContextCompat.getColor(this, R.color.accent_orange));
    }

    private void clearActiveOp() {
        if (activeOpButton != null) {
            activeOpButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.btn_op_dark)));
            activeOpButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            activeOpButton = null;
        }
    }

    // ─── History ──────────────────────────────────────────────────────────
    private void addToHistory(String expression, String result) {
        historyEntries.add(0, new String[]{expression, result});

        View item = LayoutInflater.from(this)
                        .inflate(R.layout.item_history, historyList, false);
        ((TextView) item.findViewById(R.id.tvHistoryExpression)).setText(expression);
        ((TextView) item.findViewById(R.id.tvHistoryResult)).setText(result);

        // Tap history item to restore result
        item.setOnClickListener(v -> {
            currentInput  = result;
            justEvaluated = true;
            clearActiveOp();
            updateDisplay();
            toggleHistory();
        });

        historyList.addView(item, 0);
    }

    private void toggleHistory() {
        historyVisible = !historyVisible;
        historyPanel.setVisibility(historyVisible ? View.VISIBLE : View.GONE);

        // Adjust the historyPanel height via ConstraintLayout percent
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams lp =
            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                historyPanel.getLayoutParams();
        lp.matchConstraintPercentHeight = historyVisible ? 0.20f : 0f;
        historyPanel.setLayoutParams(lp);
    }

    // ─── Theme toggle ─────────────────────────────────────────────────────
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        prefs.edit().putBoolean(PREF_THEME, isDarkTheme).apply();
        applyTheme(true);
    }

    private void applyTheme(boolean animate) {
        if (isDarkTheme) {
            applyDarkTheme();
            btnThemeToggle.setImageResource(R.drawable.ic_dark_mode);
            btnThemeToggle.setColorFilter(Color.WHITE);
        } else {
            applyLightTheme();
            btnThemeToggle.setImageResource(R.drawable.ic_light_mode);
            btnThemeToggle.setColorFilter(Color.BLACK);
        }

        if (animate) {
            // Flash animation on theme change
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(300);
            anim.addUpdateListener(a -> {
                float alpha = (float) a.getAnimatedValue();
                rootLayout.setAlpha(alpha);
            });
            rootLayout.setAlpha(0f);
            anim.start();
        }
    }

    private void applyDarkTheme() {
        int bgDark     = ContextCompat.getColor(this, R.color.bg_dark);
        int bgDisplay  = ContextCompat.getColor(this, R.color.bg_display);
        int btnNum     = ContextCompat.getColor(this, R.color.btn_num_dark);
        int btnTop     = ContextCompat.getColor(this, R.color.btn_top_dark);
        int btnOp      = ContextCompat.getColor(this, R.color.btn_op_dark);
        int textPrim   = ContextCompat.getColor(this, R.color.text_primary);
        int textSec    = ContextCompat.getColor(this, R.color.text_secondary);

        rootLayout.setBackgroundColor(bgDark);
        displayArea.setBackgroundColor(bgDisplay);
        tvDisplay.setTextColor(textPrim);
        tvExpression.setTextColor(textSec);

        applyButtonColors(btnNum, btnTop, btnOp, textPrim);

        btnHistory.setColorFilter(Color.WHITE);
    }

    private void applyLightTheme() {
        int bgLight    = ContextCompat.getColor(this, R.color.bg_light);
        int bgDisp     = ContextCompat.getColor(this, R.color.bg_display_light);
        int btnNum     = ContextCompat.getColor(this, R.color.btn_num_light);
        int btnTop     = ContextCompat.getColor(this, R.color.btn_top_light);
        int btnOp      = ContextCompat.getColor(this, R.color.btn_op_dark);
        int textPrim   = ContextCompat.getColor(this, R.color.text_primary_light);
        int textSec    = ContextCompat.getColor(this, R.color.text_secondary_light);

        rootLayout.setBackgroundColor(bgLight);
        displayArea.setBackgroundColor(bgDisp);
        tvDisplay.setTextColor(textPrim);
        tvExpression.setTextColor(textSec);

        applyButtonColors(btnNum, btnTop, btnOp, textPrim);

        btnHistory.setColorFilter(Color.BLACK);
    }

    private void applyButtonColors(int numColor, int topColor, int opColor, int textColor) {
        int[] numBtns = {R.id.btn0,R.id.btn1,R.id.btn2,R.id.btn3,R.id.btn4,
                         R.id.btn5,R.id.btn6,R.id.btn7,R.id.btn8,R.id.btn9,R.id.btnDot};
        for (int id : numBtns) {
            Button b = findViewById(id);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(numColor));
            b.setTextColor(textColor);
        }

        int[] topBtns = {R.id.btnClear, R.id.btnSign, R.id.btnPercent};
        for (int id : topBtns) {
            Button b = findViewById(id);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(topColor));
            b.setTextColor(textColor);
        }

        int[] opBtns = {R.id.btnDivide, R.id.btnMultiply, R.id.btnSubtract,
                        R.id.btnAdd, R.id.btnEquals};
        for (int id : opBtns) {
            Button b = findViewById(id);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(opColor));
            b.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }
}
