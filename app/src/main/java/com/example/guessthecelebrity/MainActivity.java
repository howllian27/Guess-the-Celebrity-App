package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView celebrityPic;
    String celebAnswer;
    String celebUrl;
    Button celeb1, celeb2, celeb3, celeb4;
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();


    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                StringBuilder stringBuilder = new StringBuilder();

                while (data != -1){
                    char current = (char) data;
                    stringBuilder.append(current);
                    data = reader.read();
                }

                result = stringBuilder.toString();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL newCelebUrl = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) newCelebUrl.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap image = BitmapFactory.decodeStream(in);
                return image;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void updateCeleb(){
        Random rd = new Random();
        String[] celebList = new String[4];
        int index = rd.nextInt(100);
        celebAnswer = celebNames.get(index);

        Random newRd = new Random();
        celebList[newRd.nextInt(4)] = celebAnswer;
        HashMap<String, Integer> celebMap = new HashMap<>();
        for (int i=0; i < celebList.length; i++){
            if (celebList[i] != celebAnswer){
                celebList[i] = celebNames.get(rd.nextInt(100));
                while (celebList[i] == celebAnswer || celebMap.containsKey(celebList[i])){
                    celebList[i] = celebNames.get(rd.nextInt(100));
                }
            }
            celebMap.put(celebList[i], 1);
        }

        for (int i=0; i < celebList.length; i++){
            System.out.println("The answer is " + celebAnswer + " and celebList[i] is " + celebList[i]);
        }

        celeb1.setText(celebList[0]);
        celeb2.setText(celebList[1]);
        celeb3.setText(celebList[2]);
        celeb4.setText(celebList[3]);



        celebUrl = celebURLs.get(index);
        try {
            DownloadImage celebImageDownload = new DownloadImage();
            Bitmap celebImage = celebImageDownload.execute(celebUrl).get();
            celebrityPic.setImageBitmap(celebImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectAns(View view){
        Button userAns = (Button) view;
        if (userAns.getText().equals(celebAnswer)){
            Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Wrong, the celebrity is " + celebAnswer, Toast.LENGTH_LONG).show();
        }

        CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
                celeb1.setEnabled(false);
                celeb2.setEnabled(false);
                celeb3.setEnabled(false);
                celeb4.setEnabled(false);
            }

            @Override
            public void onFinish() {
                updateCeleb();
                celeb1.setEnabled(true);
                celeb2.setEnabled(true);
                celeb3.setEnabled(true);
                celeb4.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityPic = findViewById(R.id.celebrityPic);
        celeb1 = findViewById(R.id.celebrity1);
        celeb2 = findViewById(R.id.celebrity2);
        celeb3 = findViewById(R.id.celebrity3);
        celeb4 = findViewById(R.id.celebrity4);
        DownloadTask task = new DownloadTask();
        String result = null;

        try{
            result = task.execute("https://www.imdb.com/list/ls052283250/").get();
            String[] splitResult = result.split("<div class=\"lister-list\">");

            Pattern p = Pattern.compile("src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[1]);

            while (m.find()){
                System.out.println(m.group(1));
                if (celebURLs.size() < 100){
                    celebURLs.add(m.group(1));
                }
            }

            p = Pattern.compile("img alt=\"(.*?)\"");
            m = p.matcher(splitResult[1]);

            while (m.find()){
                System.out.println(m.group(1));
                celebNames.add(m.group(1));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        updateCeleb();
    }
}