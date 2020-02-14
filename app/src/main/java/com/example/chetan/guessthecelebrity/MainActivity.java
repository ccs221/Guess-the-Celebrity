package com.example.chetan.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    /*grab the image url and the celeb name put them in individual arrays with same array indexes
      generate random number between 1-100 to select index to be chosen for the image source
      generate random number to select which button will have the correct answer
      assign the correct answer to the button
      generate random numbers for the other three buttons of the name array and add the names to the buttons
      when picking the answer, if correct, set a toast, and set up a new celebrity

    */

    ArrayList<String> celebsURLs = new ArrayList<>();
    ArrayList<String> celebsNames = new ArrayList<>();
    int chosenCeleb = 0;
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    ImageView celebImageView;
    Button answerButton0;
    Button answerButton1;
    Button answerButton2;
    Button answerButton3;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }
    }

    public void pickAnswer(View view){
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
            newQuestion();
        }else{
            Toast.makeText(getApplicationContext(), "Wrong! The celebrity is " + celebsNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
            newQuestion();
        }

    }

    public void newQuestion(){
        try {
            Random rand = new Random();
            chosenCeleb = rand.nextInt(celebsNames.size());
            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebsURLs.get(chosenCeleb)).get();
            celebImageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;
            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebsNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = rand.nextInt(celebsURLs.size());
                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt(celebsURLs.size());
                    }
                    answers[i] = celebsNames.get(incorrectAnswerLocation);
                }
            }

            answerButton1.setText(answers[1]);
            answerButton2.setText(answers[2]);
            answerButton3.setText(answers[3]);
            answerButton0.setText(answers[0]);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImageView = findViewById(R.id.celebImageView);
        answerButton1 = findViewById(R.id.answerButton1);
        answerButton2 = findViewById(R.id.answerButton2);
        answerButton3 = findViewById(R.id.answerButton3);
        answerButton0 = findViewById(R.id.answerButton0);

        DownloadTask task = new DownloadTask();
        String result;

        try{
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticles\">");

            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebsURLs.add(m.group(1));

            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebsNames.add(m.group(1));

            }

            newQuestion();


        } catch(Exception e){
            e.printStackTrace();
        }


    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            URL url;

            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
