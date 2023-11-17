package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

//        nextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
//                startActivity(intent);            }
//        });

        // Assurez-vous que vous référencez le bon ID du bouton
        Button nextButton = findViewById(R.id.nextButton);

        // Vérifiez si le bouton est non null avant de définir le OnClickListener
        if (nextButton != null) {
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Ajoutez votre logique de clic ici

                    Intent intent = new Intent(MainActivity2.this, MainActivity.class);
              startActivity(intent);
                }
            });


    }




    }
}