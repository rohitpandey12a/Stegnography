 package com.example.stegnography;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DisplayImageActivity extends Activity {
	private FirebaseFirestore db;
	private FirebaseStorage storage;
	private StorageReference storageRef;
	private DocumentReference dref;
	private FirebaseAuth firebaseAuth2;
	private String fname,lname,UserId,name,email,Database_Path = "All_Image_Uploads_Database";
	
	private FirebaseDatabase database;
	private DatabaseReference databaseReference;
	
	RecyclerView recyclerView;
	RecyclerView.Adapter adapter ;
	ProgressDialog progressDialog;
	List<ImageUploadInfo> list = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_images);
		db=FirebaseFirestore.getInstance();
		storage=FirebaseStorage.getInstance();
		firebaseAuth2 = FirebaseAuth.getInstance();
		if (firebaseAuth2.getCurrentUser()==null){
			finish();
			startActivity(new Intent(getApplicationContext(),Login.class));
		}
		
		database=FirebaseDatabase.getInstance();
		databaseReference=database.getReference(Database_Path);
		
		UserId= firebaseAuth2.getCurrentUser().getUid();
		dref = db.collection("User").document("UserId")
				.collection(UserId).document("Details");
		dref.get().addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
			if (documentSnapshot.exists()) {
				email = documentSnapshot.getString("Email");
				fname = documentSnapshot.getString("First Name");
				lname = documentSnapshot.getString("Last Name");
			}
		});
		name=fname+lname;
		recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(DisplayImageActivity.this));
		progressDialog = new ProgressDialog(DisplayImageActivity.this);
		progressDialog.setMessage("Loading Images From Firebase.");
		progressDialog.show();
		//Adding Add Value Event Listener to databaseReference.
				databaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NotNull DataSnapshot snapshot) {
				
				for (DataSnapshot postSnapshot : snapshot.getChildren()) {
					ImageUploadInfo imageUploadInfo = postSnapshot.getValue(ImageUploadInfo.class);
					list.add(imageUploadInfo);
				}
				
				adapter = new RecyclerViewAdapter(getApplicationContext(), list);
				recyclerView.setAdapter(adapter);
				
				// Hiding the progress dialog.
				progressDialog.dismiss();
			}
			
			@Override
			public void onCancelled(@NotNull DatabaseError databaseError) {
				// Hiding the progress dialog.
				progressDialog.dismiss();
			}
		});
	}
}
