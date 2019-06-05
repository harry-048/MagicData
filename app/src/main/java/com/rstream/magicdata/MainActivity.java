package com.rstream.magicdata;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText nameEditText;
    EditText passwordEditText;
    Button loginButton;
    SharedPreferences sharedPreferences;
    Boolean login = false;
    String name="";
    String password="";
    LinearLayout loginLayout;
    LinearLayout dataLayout;
    ProgressBar progressBar;
    ProgressBar loginProgress;

    EditText usernameEditText;
    EditText phoneEditText;
    EditText emailEditText;
    Button sendButton;
    String userName;
    String mailId;
    String phoneNo;
    Menu mainMenu=null;
    Boolean success=false;
    MenuItem logoutItem;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String id = "https://script.google.com/macros/s/AKfycbzxK7k-V7Jnbc2qkdwDd4KY_VKkIDuYoic3pThtZzL5huxcOIb-/exec";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        if (login)
            inflater.inflate(R.menu.main_menu,menu);

        logoutItem = menu.findItem(R.id.logout);
        mainMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId()==R.id.logout){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.logoutquestion));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            usernameEditText.setText("");
                            phoneEditText.setText("");
                            emailEditText.setText("");
                            loginLayout.setVisibility(View.VISIBLE);
                            dataLayout.setVisibility(View.INVISIBLE);
                            nameEditText.setText("");
                            passwordEditText.setText("");

                            login=false;
                            sharedPreferences.edit().putBoolean("login",false).apply();

                            logoutItem.setVisible(false);
                            //onCreateOptionsMenu(mainMenu);
                            dialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        loginButton = findViewById(R.id.loginButton);
        sendButton = findViewById(R.id.sendButton);
        loginLayout = findViewById(R.id.loginLayout);
        dataLayout = findViewById(R.id.dataLayout);
        progressBar = findViewById(R.id.progressBar);
        loginProgress = findViewById(R.id.progressBar2);
        sharedPreferences = getSharedPreferences("prefs.xml",MODE_PRIVATE);
        login=sharedPreferences.getBoolean("login",false);


        if (login){
            dataLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.INVISIBLE);
            if (logoutItem!=null)
                logoutItem.setVisible(true);
        }
        else {
            loginLayout.setVisibility(View.VISIBLE);
            dataLayout.setVisibility(View.INVISIBLE);

        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginClearTextField()){
                    checkTextField();
                    /*if (login){
                        nameEditText.setText("");
                        passwordEditText.setText("");
                        onCreateOptionsMenu(mainMenu);
                        loginLayout.setVisibility(View.INVISIBLE);
                        dataLayout.setVisibility(View.VISIBLE);
                        if (logoutItem!=null)
                            logoutItem.setVisible(true);
                       // collectData();
                    }*/
                }

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Boolean fieldsReady =  checkclearTextField();
                if (fieldsReady){
                    Boolean validEmail = verifyEmail();
                    Boolean validNumber = verifyPhone();
                    if (validEmail && validNumber){
                        collectData();
                    }
                }
            }
        });

    }

    public boolean checkInternet(){
       // boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }

   public void collectData(){
        userName= usernameEditText.getText().toString().trim();
        phoneNo = phoneEditText.getText().toString().trim();
        name = sharedPreferences.getString("username","");
        String urlValue= id+"?name="+userName+"&phone="+phoneNo+"&email="+mailId+"&username="+name;
        if (checkInternet()){
            progressBar.setVisibility(View.VISIBLE);
            sendButton.setText("");
            sendButton.setEnabled(false);
            new SendData().execute(urlValue);
        }
        else
            Toast.makeText(this, getString(R.string.checkinternet), Toast.LENGTH_SHORT).show();
           Log.d("httpsurlconnection",urlValue);
   }

    public class SendData extends AsyncTask<String , Void ,String> {
        String server_response;

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    success =true;
                    //server_response = readStream(urlConnection.getInputStream());
                    Log.d("httpsurlconnectionab","success");
                }

            } catch (IOException e) {
                success=false;
                Log.d("httpsurlconnectiona",e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("httpsurlconnectionq", "done" );
            if (success){
                usernameEditText.setText("");
                phoneEditText.setText("");
                emailEditText.setText("");
                usernameEditText.requestFocus();
                Toast.makeText(getApplicationContext(),getString(R.string.success),Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, getString(R.string.checkinternet), Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.INVISIBLE);
            sendButton.setEnabled(true);
            sendButton.setText(getString(R.string.savebuttontext));

        }
    }

    public boolean verifyPhone(){
        String phonePattern = "^[6-9]\\d{9}$";
        phoneNo = phoneEditText.getText().toString().trim();
        if (phoneNo.matches(phonePattern)){
            return true;
        }
        else {
            Toast.makeText(getApplicationContext(),getString(R.string.checknumber), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

   public boolean verifyEmail(){
       String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
       mailId = emailEditText.getText().toString().trim();
       if (mailId.matches(emailPattern))
       {
          // Toast.makeText(getApplicationContext(),"valid email address",Toast.LENGTH_SHORT).show();
           return true;
       }
       else
       {
           Toast.makeText(getApplicationContext(),getString(R.string.invalidemail), Toast.LENGTH_SHORT).show();
           return false;
       }
   }

   public boolean checkclearTextField(){
       if (usernameEditText.getText().toString().trim().equals("")||usernameEditText.getText()==null){
           Toast.makeText(this, getString(R.string.enterusername), Toast.LENGTH_SHORT).show();
           usernameEditText.setText("");
           usernameEditText.requestFocus();
           return false;
       }
       if (phoneEditText.getText().toString().trim().equals("")||phoneEditText.getText()==null){
           phoneEditText.setText("");
           phoneEditText.requestFocus();
           Toast.makeText(this, getString(R.string.enterphoneno), Toast.LENGTH_SHORT).show();
           return false;
       }
       if (emailEditText.getText().toString().trim().equals("")||emailEditText.getText()==null){
           emailEditText.setText("");
           emailEditText.requestFocus();
           Toast.makeText(this, getString(R.string.enteremail), Toast.LENGTH_SHORT).show();
           return false;
       }
       return true;
   }

   public boolean loginClearTextField(){
       if (nameEditText.getText().toString().trim().equals("")||nameEditText.getText()==null){
           Toast.makeText(this, getString(R.string.entername), Toast.LENGTH_SHORT).show();
           nameEditText.setText("");
           nameEditText.requestFocus();
           return false;
       }
       if (passwordEditText.getText().toString().trim().equals("")||passwordEditText.getText()==null){
           Toast.makeText(this, getString(R.string.enterpassword), Toast.LENGTH_SHORT).show();
           passwordEditText.setText("");
           passwordEditText.requestFocus();
           return false;
       }
        return true;
   }

    public void checkTextField(){
        loginProgress.setVisibility(View.VISIBLE);
        loginButton.setText("");
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("firebasedata", document.getId() + " => " + document.getData());
                                name = document.getData().get("username").toString();
                                password = document.getData().get("password").toString();
                                Log.d("firebasedataava", "username : "+ name + " , password"+ password);
                                sharedPreferences.edit().putString("username",document.getData().get("username").toString()).apply();

                                if (nameEditText.getText().toString().trim().equals(document.getData().get(getString(R.string.firebaseusername)).toString())&& passwordEditText.getText().toString().trim().equals(document.getData().get(getString(R.string.firebasepassword)).toString())){
                                    login=true;
                                    sharedPreferences.edit().putBoolean("login",true).apply();
                                        nameEditText.setText("");
                                        passwordEditText.setText("");
                                        onCreateOptionsMenu(mainMenu);
                                        loginLayout.setVisibility(View.INVISIBLE);
                                        dataLayout.setVisibility(View.VISIBLE);
                                        if (logoutItem!=null)
                                            logoutItem.setVisible(true);
                                    loginProgress.setVisibility(View.INVISIBLE);
                                    loginButton.setText(getString(R.string.loginbuttontext));
                                        // collectData();

                                    Log.d("uesrnameandpass",name+" ,"+nameEditText.getText()+" , "+passwordEditText.getText()+" , "+password);
                                }
                                else {
                                    Log.d("uesrnameandpass",name+" ,"+nameEditText.getText()+" , "+passwordEditText.getText()+" , "+password);
                                    Toast.makeText(MainActivity.this, getString(R.string.incorrectnmandpass), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            Log.d("firebasedataerror", "Error getting documents.", task.getException());
                        }
                    }
                });


        /*if (nameEditText.getText().toString().trim().equals(name)&& passwordEditText.getText().toString().trim().equals(password)){
            login=true;
            sharedPreferences.edit().putBoolean("login",true).apply();
        }
        else {
            Log.d("uesrnameandpass",name+" ,"+nameEditText.getText()+" , "+passwordEditText.getText()+" , "+password);
            Toast.makeText(this, getString(R.string.incorrectnmandpass), Toast.LENGTH_SHORT).show();
        }*/

    }
}
