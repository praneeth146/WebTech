package com.example.emailapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText email, subject, message;
    Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        subject = findViewById(R.id.subject);
        message = findViewById(R.id.message);
        sendBtn = findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailAddress = email.getText().toString();
                String emailSubject = subject.getText().toString();
                String emailMessage = message.getText().toString();

                if(emailAddress.isEmpty() || emailSubject.isEmpty() || emailMessage.isEmpty()){
                    Toast.makeText(MainActivity.this,
                            "Please fill all fields",
                            Toast.LENGTH_SHORT).show();
                }
                else{

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + emailAddress));
                    intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                    intent.putExtra(Intent.EXTRA_TEXT, emailMessage);

                    try{
                        startActivity(Intent.createChooser(intent,"Send Email"));
                    }
                    catch(Exception e){
                        Toast.makeText(MainActivity.this,
                                "No Email App Found",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
