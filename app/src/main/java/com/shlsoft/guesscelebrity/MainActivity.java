package com.shlsoft.guesscelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();

    int choosenCeleb = 0;

    int locationOfCorrectAnswer = 0;

    String[] answers = new String[4];

    ImageView imageView;
    Button btn0, btn1, btn2, btn3;

    ProgressBar progressBar;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection)url.openConnection();

                InputStream in = urlConnection.getInputStream();

                BufferedReader reader = new BufferedReader( new InputStreamReader(in) );

                String data=reader.readLine();

                while(data!=null){

                    result+=data;
                    data=reader.readLine();
                }
                return result;

            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern pattern = Pattern.compile("img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);

            while (matcher.find()){
                celebURLs.add(matcher.group(1));
            }

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);

            while (matcher.find()){
                celebNames.add(matcher.group(1));
            }


        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        createNewQuestion();
    }

    private void initViews() {
        imageView = findViewById(R.id.imageView);
        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        progressBar = findViewById(R.id.progressBar);
    }

    public void createNewQuestion(){
        Random random = new Random();
        choosenCeleb = random.nextInt(celebURLs.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;

        try {
            celebImage = imageTask.execute(celebURLs.get(choosenCeleb)).get();

            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswerLocation;

            for(int i = 0; i < 4; i++){
                if(i == locationOfCorrectAnswer){
                    answers[i] = celebNames.get(choosenCeleb);
                }
                else {
                    incorrectAnswerLocation = random.nextInt(celebURLs.size());

                    while(incorrectAnswerLocation == choosenCeleb){
                        incorrectAnswerLocation = random.nextInt(celebURLs.size());
                    }

                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }
            btn0.setText(answers[0]);
            btn1.setText(answers[1]);
            btn2.setText(answers[2]);
            btn3.setText(answers[3]);

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void celebChosen(View view) {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(),"Correct!",Toast.LENGTH_SHORT).show();
            createNewQuestion();
        }
        else{
            Toast.makeText(getApplicationContext(),"Wrong! It was " +
                    celebNames.get(choosenCeleb),Toast.LENGTH_SHORT).show();
            createNewQuestion();
        }
    }
}
