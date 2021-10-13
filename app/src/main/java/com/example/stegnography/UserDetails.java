package com.example.stegnography;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UserDetails extends AppCompatActivity implements View.OnClickListener{
	private EditText editTextfirstname,editTextlastname,editTextdob,editTextaddress,editTextemail,editTextphonenumber;
	private RadioGroup radioGroupgender;
	private RadioButton radioButtongender;
	private Button submitButton,selectimage;
	private FirebaseFirestore db = FirebaseFirestore.getInstance();
	private FirebaseStorage storage = FirebaseStorage.getInstance();
	private StorageReference storageRef;
	private DocumentReference dref;
	private Uri mImageUri;
	private String fname;
	private String lname;
	private String number;
	private String address;
	private String dob;
	private String gender="";
	private String name;
	private DatabaseReference databaseReference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_details);
		FirebaseAuth firebaseAuth2 = FirebaseAuth.getInstance();
		if (firebaseAuth2.getCurrentUser()==null){
			finish();
			startActivity(new Intent(getApplicationContext(),Login.class));
		}
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		String database_Path = "All_Image_Uploads_Database";
		databaseReference= database.getReference(database_Path);
		String userId = firebaseAuth2.getCurrentUser().getUid();
		dref = db.collection("User").document("UserId")
				.collection(userId).document("Details");
		dref.get().addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
			if (documentSnapshot.exists()) {
				String email = documentSnapshot.getString("Email");
				String fname = documentSnapshot.getString("First Name");
				String lname = documentSnapshot.getString("Last Name");
				String number = documentSnapshot.getString("Phone Number");
				String address = documentSnapshot.getString("Address");
				String dob = documentSnapshot.getString("Date Of Birth");
				editTextfirstname.setText(fname);
				editTextlastname.setText(lname);
				editTextaddress.setText(address);
				editTextphonenumber.setText(number);
				editTextdob.setText(dob);
				editTextemail.setText(email);
			}
		});
		
		editTextfirstname=findViewById(R.id.editTextTextFirstName);
		editTextlastname=findViewById(R.id.editTextTextLastName);
		editTextdob=findViewById(R.id.editTextDate);
		editTextaddress=findViewById(R.id.editTextAddress);
		editTextemail=findViewById(R.id.editTextTextEmailAddress);
		editTextphonenumber=findViewById(R.id.editTextPhone);
		radioGroupgender=findViewById(R.id.radioGroup);
		submitButton=findViewById(R.id.submit);
		selectimage=findViewById(R.id.selectimage);
		
		submitButton.setOnClickListener(this);
		selectimage.setOnClickListener(this);
		
		editTextfirstname.addTextChangedListener(submittextwatcher);
		editTextlastname.addTextChangedListener(submittextwatcher);
		editTextdob.addTextChangedListener(submittextwatcher);
		editTextaddress.addTextChangedListener(submittextwatcher);
		editTextemail.addTextChangedListener(submittextwatcher);
		editTextphonenumber.addTextChangedListener(submittextwatcher);
		radioGroupgender.setOnCheckedChangeListener((group, checkedId) -> {
			radioButtongender=findViewById(radioGroupgender.getCheckedRadioButtonId());
			gender=radioButtongender.getText().toString();
		});
	}
	
	private TextWatcher submittextwatcher=new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			fname = editTextfirstname.getText().toString().trim();
			number = editTextphonenumber.getText().toString().trim();
			lname = editTextlastname.getText().toString().trim();
			address = editTextaddress.getText().toString().trim();
			dob = editTextdob.getText().toString().trim();
			String email = editTextemail.getText().toString().trim();
			submitButton.setEnabled(!fname.isEmpty()&&!lname.isEmpty()&&!address.isEmpty()&&!number.isEmpty()&&!dob.isEmpty()&&!email.isEmpty());
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		
		}
	};
	@Override
	public void onClick(View v) {
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
			if (v==submitButton){
				saveUserInformation();
			}else if(v==selectimage){
				openImage();
			}
		}
		else{
			Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
		}
	}
	
	private void openImage() {
		mGetContent.launch("image/*");
	}
	
	ActivityResultLauncher<String> mGetContent = registerForActivityResult(
			new ActivityResultContracts.GetContent(),
				new ActivityResultCallback<Uri>() {
					@Override
					public void onActivityResult(Uri result) {
						if(result!=null){
							mImageUri=result;
							uploadImage();
						}
					}
				});
	
	private String getFileName(Uri mImageUri) {
		Cursor c = getContentResolver().query(mImageUri, null, null, null, null);
		c.moveToFirst();
		return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
	}
	
	private void uploadImage() {
		ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("Uploading");
		pd.show();
		name=fname+lname;
		storageRef=
				storage.getReference().child(name).child("ouploads").child(getFileName(mImageUri));
		storageRef.putFile(mImageUri).addOnCompleteListener(task -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
			String url=uri.toString();
			Log.d("download url",url);
			Toast.makeText(UserDetails.this, "image upload successfull", Toast.LENGTH_SHORT).show();
			ImageUploadInfo imageUploadInfo = new ImageUploadInfo(name,getFileName(mImageUri),url);
			String ImageUploadId = databaseReference.push().getKey();
			databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
			pd.dismiss();
			
		})).addOnFailureListener(e -> {
			pd.dismiss();
			Toast.makeText(UserDetails.this,e.getMessage(),Toast.LENGTH_LONG).show();
		});
	}
	
	private void saveUserInformation() {
		
		Map<String, Object> note = new HashMap<>();
		note.put("First Name", fname);
		note.put("Last Name",lname);
		note.put("Address",address);
		note.put("Date Of Birth",dob);
		note.put("Gender",gender);
		note.put("Phone Number", number);
		dref.update(note)
				.addOnSuccessListener(aVoid -> Toast.makeText(UserDetails.this,"Info Saved...",
						Toast.LENGTH_LONG).show()).addOnFailureListener(e -> Toast.makeText(UserDetails.this,"Info not Saved...",Toast.LENGTH_LONG).show());
		dref.get().addOnSuccessListener(documentSnapshot -> {
			if (documentSnapshot.exists()) {
				startActivity(new Intent(getApplicationContext(),Home.class));
			}
		});
	}
}