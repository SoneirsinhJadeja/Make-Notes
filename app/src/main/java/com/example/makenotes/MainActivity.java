package com.example.makenotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    LinearLayout parentLayout;
    ImageButton  logoutButton;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferences_Universer;
    String Email;
    int NotesCounter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button MakeNotes;
        parentLayout = findViewById(R.id.parentLayout);

        db = FirebaseFirestore.getInstance();
        MakeNotes = findViewById(R.id.CreateMatch);

        // Retrieve Intent data
        Intent intent = getIntent();
        Email = intent.getStringExtra("Email");

        logoutButton = findViewById(R.id.btn_logout);



        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences = getSharedPreferences("Notes-CheckUserExists", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("Email");
                editor.remove("Password");
                editor.remove("Name");
                editor.apply();
                Intent intent = new Intent(MainActivity.this, signIn.class);
                startActivity(intent);
            }
        });

        MakeNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, createNotes.class);
                intent.putExtra("Email", Email);
                startActivity(intent);
            }
        });
        // Call method to fetch user record with the retrieved Email
        fetchUserRecord(Email);
    }

    @Override
    protected void onResume() {
        super.onResume();
        alreadyAuth();
    }
    private void alreadyAuth() {
        sharedPreferences_Universer = getSharedPreferences("Notes-CheckUserExists", MODE_PRIVATE);
        String Email = sharedPreferences_Universer.getString("Email", "");
        String Pass = sharedPreferences_Universer.getString("Password", "");

        if (Pass.isEmpty() && Email.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, signIn.class);
            startActivity(intent);
            finish();
        }
    }
    private void fetchUserRecord(String Email) {
        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot userSnapshot : list) {
                            String email = userSnapshot.getString("D_Email");

                            if (email != null && email.equals(Email)) {
                                fetchNotesDetails(email);
                            }
                        }
                    }
                });
    }
    private void fetchNotesDetails(String Email) {

        db.collection(Email).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult().getDocuments()) {

                                Map<String, Object> data = document.getData();
                                if (data != null && data.containsKey("Desc") && data.containsKey("Title")) {
                                    String title = document.getString("Title");
                                    String desc = document.getString("Desc");
                                    String Date = document.getString("Date");
                                    int Note = document.getLong("Note").intValue();

                                    if (title != null && desc != null && Date != null) {
                                        createCardViewForMatch(title, desc, Date, Note);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Title or Desc is null", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to fetch documents", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void createCardViewForMatch(String Title, String Desc, String Date, int Note) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.cardview_for_list_of_notes, parentLayout, false);

        // Get references to the TextViews in the CardView
        TextView TX_Title = cardView.findViewById(R.id.Title);
        TextView TX_Desc = cardView.findViewById(R.id.Desc);
        TextView TX_Date = cardView.findViewById(R.id.Date);

        LinearLayout LL_cardView_Match = cardView.findViewById(R.id.cardView_Match);

        // Set the click listener for navigating to the update page
        LL_cardView_Match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, updateNotes.class);
                intent.putExtra("Title", Title);
                intent.putExtra("NotesCounter", NotesCounter);
                intent.putExtra("Email", Email);
                intent.putExtra("Desc", Desc);
                intent.putExtra("NotesCounter", Note);
                intent.putExtra("Date", Date);
                startActivity(intent);
            }
        });

        // Set the long click listener to delete the note
        LL_cardView_Match.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show a confirmation dialog before deleting
                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDocument(Note, cardView);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        // Populate the TextViews with team data
        TX_Title.setText(Title);
        TX_Desc.setText(Desc);
        TX_Date.setText(Date);

        // Add the CardView to the parent layout
        parentLayout.addView(cardView);
    }


    private void deleteDocument(int Note, View cardView) {
        db.collection(Email).document("Notes " + Note)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                        // Remove the card view from the parent layout
                        parentLayout.removeView(cardView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to delete the note", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}