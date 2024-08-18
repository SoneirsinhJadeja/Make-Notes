package com.example.makenotes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class updateNotes extends AppCompatActivity {

    EditText ED_Title, ED_Desc;
    FirebaseFirestore db;
    String Date, Email, Title, Desc;
    int NoteCounter;
    TextView TX_Date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_notes);
        db = FirebaseFirestore.getInstance();

        // Retrieve Intent data
        Intent intent = getIntent();
        NoteCounter = intent.getIntExtra("NotesCounter", -1);
        Title = intent.getStringExtra("Title");
        Email = intent.getStringExtra("Email");
        Desc = intent.getStringExtra("Desc");
        String Date = intent.getStringExtra("Date");

        if (NoteCounter == -1) {
            Toast.makeText(this, "Fail to fetch NoteCounter val ", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "NoteCounter :"+NoteCounter, Toast.LENGTH_SHORT).show();
        }
        ED_Title = findViewById(R.id.Title);
        ED_Desc = findViewById(R.id.Desc);
        TX_Date = findViewById(R.id.DateTimeCharCount);

        ED_Title.setText(Title);
        ED_Desc.setText(Desc);
        TX_Date.setText(Date);

        ED_Desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateDateTimeCharacter();
            }
        });

        // Assuming you have a "Save" button to save the note
        Button saveNotes = findViewById(R.id.updateNotes);
        saveNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNote();  // Fetch the NotesCounter before saving the note
            }
        });
    }
    private void updateDateTimeCharacter() {
        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        int charCount = ED_Desc.getText().length();

        Date = currentDate + " " + currentTime;
        // Set the date, time, and character count
        TX_Date.setText(String.format("%s  %s  |  %d characters", currentDate, currentTime, charCount));
    }

    private void updateNote() {

        String title = ED_Title.getText().toString();
        String date = TX_Date.getText().toString();
        String desc = ED_Desc.getText().toString();

        if (Title.isEmpty() || Desc.isEmpty()) {
            Toast.makeText(updateNotes.this, "Title and Description cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        if (!title.equals(Title) || !desc.equals(Desc)) {
            Toast.makeText(this, "NoteCounter :"+NoteCounter, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "here it is come..", Toast.LENGTH_SHORT).show();
            Map<String, Object> noteDetails = new HashMap<>();
            noteDetails.put("Title", title);
            noteDetails.put("Desc", desc);
            noteDetails.put("Date", date);
            noteDetails.put("Note", NoteCounter);


            db.collection(Email)
                    .document("Notes " + NoteCounter)
                    .set(noteDetails)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {}
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(updateNotes.this, "Error saving note", Toast.LENGTH_LONG).show();
                        }});
        }

        Intent intent = new Intent(updateNotes.this, MainActivity.class);
        intent.putExtra("Email", Email);
        startActivity(intent);
        finish();  // Close createNotes activity

    }
}