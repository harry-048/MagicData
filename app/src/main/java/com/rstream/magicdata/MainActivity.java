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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    StringBuilder sb;
    MenuItem syncData;
    List<String> urlDataSet=null;
    Set<String> urlSet;
    Set<String> urlCollection;
    Set<String> updateUrls;
    Set<String> tempUrls;
    ImageView syncButton;
    Animation rotation;
    Snackbar snackbar;
    String allUrls;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String id = "https://script.google.com/macros/s/AKfycbzxK7k-V7Jnbc2qkdwDd4KY_VKkIDuYoic3pThtZzL5huxcOIb-/exec";

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        if (login)
            inflater.inflate(R.menu.main_menu,menu);

        logoutItem = menu.findItem(R.id.logout);
        syncData = menu.findItem(R.id.syncData);

        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        syncButton = (ImageView)layoutInflater.inflate(R.layout.iv_refresh, null);
       // final ImageView syncButton = (ImageView) menu.findItem(R.id.syncData).getActionView();
         rotation = AnimationUtils.loadAnimation(this, R.anim.anim);
        rotation.setRepeatCount(Animation.INFINITE);

        /*if (syncButton != null) {
            syncButton.setImageResource(R.drawable.ic_sync_white_24dp);
            // need some resize
           // syncButton.setScaleX(IMAGE_SCALE);
           // syncButton.setScaleY(IMAGE_SCALE);
            syncButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("rotationisdone","start");
                    syncButton.startAnimation(rotation);
                    menu.findItem(R.id.syncData).setActionView(syncButton);
                    Log.d("rotationisdone","working");
                  //  syncButton.startAnimation(rotation);
                    // create and use new data set

                }
            });
        }*/

       /* Set<String> tempSet = sharedPreferences.getStringSet("urlUpdateValues",null);
        if (tempSet!=null)
            if (tempSet.size()>0){
                syncData.setVisible(true);
                syncData.setEnabled(true);
            }
            else{
                syncData.setEnabled(false);
                syncData.setVisible(false);
            }*/

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

        if (item.getItemId()==R.id.syncData){



            syncData.setActionView(syncButton);
           // syncButton.setVisibility(View.VISIBLE);
           // syncData.setIcon(getDrawable(R.drawable.loadinganimation));



           // menu.findItem(R.id.syncData).setActionView(syncButton);

            if (checkInternet()){
                Log.d("syncbutton","internet available");
                urlSet =  sharedPreferences.getStringSet("urlvalues",null);


               // updateUrls = new HashSet<String>();
                tempUrls = new HashSet<String>();
                tempUrls = sharedPreferences.getStringSet("urlUpdateValues",null);
                //tempUrls = sharedPreferences.getStringSet("urlvalues",null);


                if ((urlSet!=null&& !urlSet.isEmpty())||(tempUrls!=null&& !tempUrls.isEmpty())){
                    Log.d("syncbutton","not null: "+urlSet);
                    if (urlSet!=null&& !urlSet.isEmpty()){
                        urlDataSet = new ArrayList<String>(urlSet);
                        urlCollection = urlSet;
                        sharedPreferences.edit().putStringSet("urlUpdateValues",urlSet).apply();
                        urlSet.clear();
                        urlSet = null;
                        sharedPreferences.edit().putStringSet("urlvalues",urlSet).apply();
                    }
                    else if (tempUrls!=null&& !tempUrls.isEmpty()){
                        urlDataSet = new ArrayList<String>(tempUrls);
                        urlCollection = tempUrls;
                        sharedPreferences.edit().putStringSet("urlUpdateValues",tempUrls).apply();
                    }



                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Syncing...", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                    syncButton.startAnimation(rotation);
                    syncData.setEnabled(false);
                    syncButton.setEnabled(false);
                    new UpdateData().execute(urlCollection);
                }
                else {
                    Log.d("syncbutton","everytihng synced");
                    Toast.makeText(this, "Everything is synced.", Toast.LENGTH_SHORT).show();
                    syncData.setActionView(null);
                }

            }
            else{
                syncData.setActionView(null);
                Toast.makeText(this, getString(R.string.checkinternet), Toast.LENGTH_SHORT).show();
            }




        }

        return super.onOptionsItemSelected(item);
    }

    public void saveUrl(String sUrl){
        allUrls = sharedPreferences.getString("allurls",null);
        Log.d("allurls",allUrls);
        if (allUrls!=null && !allUrls.trim().equals("")){
            allUrls = allUrls + ","+sUrl;
        }
        else {
            allUrls = sUrl;
        }
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
        sb=new StringBuilder();
        Log.d("urlsetvaluesare","first "+sharedPreferences.getStringSet("urlvalues",null)+"");


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



       urlSet =  sharedPreferences.getStringSet("urlvalues",null);
       Log.d("urlsetvaluesare","before "+urlSet+"");
       if (urlSet!=null&& !urlSet.isEmpty())
        urlSet.add(urlValue);
       else {
           urlSet = new HashSet<String>();
           urlSet.add(urlValue);
       }

       Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();


       /*if (urlDataSet!=null)
       if (urlDataSet.size()>0)
           syncData.setEnabled(true);
       else
           syncData.setEnabled(false);*/
       Log.d("urlsetvaluesare","after "+urlSet+"");
        sharedPreferences.edit().putStringSet("urlvalues",urlSet).apply();

       Log.d("urlsetvaluesare","after save "+sharedPreferences.getStringSet("urlvalues",null)+"");

       urlCollection = new HashSet<>();
       urlCollection=urlSet;

       usernameEditText.setText("");
       phoneEditText.setText("");
       emailEditText.setText("");
       usernameEditText.requestFocus();

        /*if (checkInternet()){
            progressBar.setVisibility(View.VISIBLE);
            sendButton.setText("");
            sendButton.setEnabled(false);
            new SendData().execute(urlValue);
        }
        else
            Toast.makeText(this, getString(R.string.checkinternet), Toast.LENGTH_SHORT).show();
           Log.d("httpsurlconnection",urlValue);*/
   }

   public class UpdateData extends AsyncTask<Set<String>,Void,String>{
       String server_response;
       @Override
       protected String doInBackground(Set<String>... sets) {
           URL url;
           HttpURLConnection urlConnection = null;

           try {
               for (int i=0;i<urlDataSet.size();){
                   url = new URL(urlDataSet.get(i));
                   urlConnection = (HttpURLConnection) url.openConnection();

                   int responseCode = urlConnection.getResponseCode();

                   if(responseCode == HttpURLConnection.HTTP_OK){
                       success =true;
                       Log.d("httpsurlconnectionab","success: "+ urlDataSet.get(i));
                       Log.d("httpsurlconnectionab",""+(urlDataSet.size()-1)+" "+getResources().getString(R.string.items_to_be_synced));
                       urlDataSet.remove(i);
                       Set<String> updateVal = new HashSet<>(urlDataSet);
                       sharedPreferences.edit().putStringSet("urlUpdateValues",updateVal).apply();

                       //server_response = readStream(urlConnection.getInputStream());

                   }
               }

               if (urlDataSet.size()==0)
                   success=true;
             //  url = new URL(strings[0]);

           } catch (IOException e) {
               success=false;
               Log.d("httpsurlconnectiona",e.getMessage());
               e.printStackTrace();
           }

           return null;
       }

       @Override
       protected void onPostExecute(String s) {
           if (success)
           {
               Toast.makeText(MainActivity.this, getString(R.string.succesfullusynced), Toast.LENGTH_SHORT).show();
           }
           else {
               Toast.makeText(MainActivity.this, getString(R.string.checkinternet), Toast.LENGTH_SHORT).show();

               updateUrls = new HashSet<String>();
               tempUrls = new HashSet<String>();
               updateUrls = sharedPreferences.getStringSet("urlUpdateValues",null);
               tempUrls = sharedPreferences.getStringSet("urlvalues",null);

               if (tempUrls!=null && !tempUrls.isEmpty()){
                   updateUrls.addAll(tempUrls);
               }

               if (updateUrls!=null && !updateUrls.isEmpty())
                   sharedPreferences.edit().putStringSet("urlvalues",updateUrls).apply();



           }
           syncData.setEnabled(true);
           syncButton.setEnabled(true);
           snackbar.dismiss();
           syncButton.clearAnimation();
           syncData.setActionView(null);

           super.onPostExecute(s);
       }
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
