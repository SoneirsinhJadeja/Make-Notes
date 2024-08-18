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
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class createNotes extends AppCompatActivity {
    EditText Desc_ET, Title_ET;
    TextView DateTimeCharCount;
    String Email;
    int NotesCounter;
    Map<String, Object> NotesCounter_Details;
    FirebaseFirestore db;
    String Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notes);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve Intent data
        Intent intent = getIntent();
        Email = intent.getStringExtra("Email");

        Desc_ET = findViewById(R.id.Desc);
        Title_ET = findViewById(R.id.Title);
        DateTimeCharCount = findViewById(R.id.DateTimeCharCount);

        // Set the current date and time
        updateDateTimeCharacter();

        // Update character count on text change
        Desc_ET.addTextChangedListener(new TextWatcher() {
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
        Button saveNotes = findViewById(R.id.saveNotes);
        saveNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchNotesCounter();  // Fetch the NotesCounter before saving the note
            }
        });
    }

    private void updateDateTimeCharacter() {
        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        int charCount = Desc_ET.getText().length();

        Date = currentDate + " " + currentTime;
        // Set the date, time, and character count
        DateTimeCharCount.setText(String.format("%s  %s  |  %d characters", currentDate, currentTime, charCount));
    }

    private void fetchNotesCounter() {
        db.collection(Email).document("Notes Counter").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Map<String, Object> data = document.getData();
                                if (data != null && data.containsKey("Counter")) {
                                    NotesCounter = document.getLong("Counter").intValue();
                                    NotesCounter = NotesCounter + 1;
                                    saveNote();  // Save the note only after the counter is fetched
                                }
                            } else {
                                Toast.makeText(createNotes.this, "Document not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(createNotes.this, "Failed to fetch document", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    private void saveNote() {

        String Title = Title_ET.getText().toString();
        String Desc = Desc_ET.getText().toString();

        if (Title.isEmpty() || Desc.isEmpty()) {
            Toast.makeText(createNotes.this, "Title and Description cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> noteDetails = new HashMap<>();
        noteDetails.put("Title", Title);
        noteDetails.put("Desc", Desc);
        noteDetails.put("Date", Date);
        noteDetails.put("Note", NotesCounter);


        db.collection(Email)
                .document("Notes " + NotesCounter)
                .set(noteDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateNoteCounter();
                        Intent intent = new Intent(createNotes.this, MainActivity.class);
                        intent.putExtra("Email", Email);
                        startActivity(intent);
                        finish();  // Close createNotes activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(createNotes.this, "Error saving note", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void updateNoteCounter() {
        Map<String, Object> NoteCounter = new HashMap<>();
        NoteCounter.put("Counter", NotesCounter);

        db.collection(Email)
                .document("Notes Counter")
                .set(NoteCounter)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {}
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(createNotes.this, "Data Not stored successfully...", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}