package com.example.appdevwardrobeinf246;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class Label extends AppCompatActivity {

    ImageView imgPreview;
    EditText etName, etDescription;
    Spinner spinnerArea, spinnerType;
    Button btnSave;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);

        imgPreview = findViewById(R.id.imgPreview);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        spinnerArea = findViewById(R.id.spinnerArea);
        spinnerType = findViewById(R.id.spinnerType);
        btnSave = findViewById(R.id.btnSave);

        imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        imgPreview.setImageURI(imageUri);

        String[] areas = {"Top", "Bottom", "Headwear", "Footwear", "Accessory"};

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, areas);

        areaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerArea.setAdapter(areaAdapter);

        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTypeSpinner(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> {

            if (spinnerArea.getSelectedItem() == null || spinnerType.getSelectedItem() == null) {
                return;
            }

            String name = etName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String area = spinnerArea.getSelectedItem().toString();
            String type = spinnerType.getSelectedItem().toString();

            clothitem item = new clothitem(
                    name,
                    desc,
                    area,
                    type,
                    imageUri.toString()
            );

            tempdb.add(item);
            finish();
        });
    }

    private void updateTypeSpinner(String area) {
        String[] types;

        switch (area) {
            case "Top":
                types = new String[]{"Polo", "T-Shirt", "Long Sleeve", "Jacket"};
                break;
            case "Bottom":
                types = new String[]{"Jeans", "Shorts", "Slacks"};
                break;
            case "Footwear":
                types = new String[]{"Sneakers", "Sandals", "Shoes"};
                break;
            default:
                types = new String[]{"Other"};
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, types);

        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerType.setAdapter(typeAdapter);
    }
}