package com.example.stegnography;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity implements View.OnClickListener {
	
	private Button userdetails,logout,encrypt,decrypt;
	private FirebaseAuth firebaseAuth2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		firebaseAuth2 = FirebaseAuth.getInstance();
		if (firebaseAuth2.getCurrentUser()==null){
			finish();
			startActivity(new Intent(getApplicationContext(),Login.class));
		}
		userdetails=findViewById(R.id.btnuserdetails);
		logout=findViewById(R.id.btnlogout);
		encrypt=findViewById(R.id.btnencrypt);
		decrypt=findViewById(R.id.btndecrypt);
		userdetails.setOnClickListener(this);
		logout.setOnClickListener(this);
		encrypt.setOnClickListener(this);
		decrypt.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if (v==userdetails){
			startActivity(new Intent(getApplicationContext(),UserDetails.class));
		}else if (v==logout){
			firebaseAuth2.signOut();
			finish();
			startActivity(new Intent(getApplicationContext(),Login.class));
		}else if(v==encrypt){
			startActivity(new Intent(getApplicationContext(),Encrypt.class));
			
		}else if(v==decrypt){
			startActivity(new Intent(getApplicationContext(),Decrypt.class));
			
		}
		
	}
}