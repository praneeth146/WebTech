package com.example.fontadjuster;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView textPreview;
    private SeekBar sizeSeekBar;
    private Spinner fontSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPreview = findViewById(R.id.textPreview);
        sizeSeekBar = findViewById(R.id.sizeSeekBar);
        fontSpinner = findViewById(R.id.fontSpinner);

        // Text Size Logic
        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newSize = progress > 10 ? progress : 10;
                textPreview.setTextSize(newSize);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Font Style Logic
        String[] fontOptions = {"Default", "Monospace", "Serif", "Sans Serif"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_dropdown_item, fontOptions);
        fontSpinner.setAdapter(adapter);

        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: textPreview.setTypeface(Typeface.DEFAULT); break;
                    case 1: textPreview.setTypeface(Typeface.MONOSPACE); break;
                    case 2: textPreview.setTypeface(Typeface.SERIF); break;
                    case 3: textPreview.setTypeface(Typeface.SANS_SERIF); break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
