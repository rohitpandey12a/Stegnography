package com.example.stegnography;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener{
	
	
	private Button buttonSignIn;
	private EditText editTextEmail,editTextPass;
	private TextView textViewSignIn;
	private ProgressDialog progressDialog;
	private FirebaseAuth firebaseAuth1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		firebaseAuth1=FirebaseAuth.getInstance();
		
		if (firebaseAuth1.getCurrentUser()!=null){
			finish();
			startActivity(new Intent(getApplicationContext(),Home.class));
		}
		
		editTextEmail=findViewById(R.id.edittextemail_login);
		editTextPass=findViewById(R.id.edittextpass_login);
		buttonSignIn=findViewById(R.id.buttonSignIn__login);
		textViewSignIn=findViewById(R.id.textviewsignin_login);
		
		progressDialog = new ProgressDialog(this);
		buttonSignIn.setOnClickListener(this);
		textViewSignIn.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
			if (v==buttonSignIn){
				userLogin();
			}else if (v==textViewSignIn) {
				startActivity(new Intent(this,MainActivity.class));
			}
		}
		else{
			Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
		}
	}
	
	private void userLogin(){
		String email1=editTextEmail.getText().toString().trim();
		String pass1=editTextPass.getText().toString().trim();
		if(TextUtils.isEmpty(email1)){
			Toast.makeText(this,"Please Enter email",Toast.LENGTH_SHORT).show();
			return;
		}
		if(TextUtils.isEmpty(pass1)){
			Toast.makeText(this,"Please Enter password",Toast.LENGTH_SHORT).show();
			return;
		}
		progressDialog.setMessage("Signing In");
		progressDialog.show();
		firebaseAuth1.signInWithEmailAndPassword(email1,pass1)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						progressDialog.dismiss();
						if(task.isSuccessful()){
							finish();
							startActivity(new Intent(getApplicationContext(),Home.class));
						}
						else {
							Toast.makeText(Login.this, "SignIn Error", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}
}