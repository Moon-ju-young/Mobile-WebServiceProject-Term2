package com.example.photoviewer;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    ImageView imgView;
    TextView textView;
    String site_url = "https://amymoon07.pythonanywhere.com"; // "http://10.0.2.2:8000";
    JSONObject post_json;
    String imageUrl = null;
    Bitmap bmImg = null;

    CloadImage taskDownload;
    PutPost taskUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= 37 &&
                checkSelfPermission("android.permission.ACCESS_LOCAL_NETWORK")
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{"android.permission.ACCESS_LOCAL_NETWORK"},
                    100
            );
        }

//imgView = (ImageView) findViewById(R.id.imgView);
        textView = (TextView)findViewById(R.id.textView);
    }

    public void onClickDownload(View v) {
//...생략...
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }

        taskDownload = new CloadImage();

        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v) {
//...여기에 코드추가...
        if (taskUpload != null && taskUpload.getStatus() == AsyncTask.Status.RUNNING) {
            taskUpload.cancel(true);
        }
        // 샘플 파란색 이미지 생성
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        new Canvas(bitmap).drawColor(Color.BLUE);

        taskUpload = new PutPost("테스트 제목", "테스트 내용입니다.", bitmap);
        taskUpload.execute(site_url + "/api_root/Post/");

        Toast.makeText(getApplicationContext(), "Upload", Toast.LENGTH_LONG).show();
    }

    private class CloadImage extends AsyncTask<String, Integer, List<ImageAdapter.Post>> {
        @Override
        protected List<ImageAdapter.Post> doInBackground(String... urls) {
            List<ImageAdapter.Post> postList = new ArrayList<>();

            try {
                String apiUrl = urls[0];
                String token = "eb94ccee2d73b971c091889e06cba4829f0cb594";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);

                    // 배열내모든 게시글(제목, 내용, 이미지) 다운로드
                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);
                        String title = post_json.optString("title", "");
                        String text = post_json.optString("text", "");
                        imageUrl = post_json.optString("image", "");

                        if (!imageUrl.equals("")) {
                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            postList.add(new ImageAdapter.Post(title, text, imageBitmap));
                            imgStream.close();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return postList;
        }

        @Override
        protected void onPostExecute(List<ImageAdapter.Post> posts) {
            if (posts.isEmpty()) {
                textView.setText("불러올 이미지가없습니다.");
            } else {
                textView.setText("이미지 로드성공!");
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(posts);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }

    private class PutPost extends AsyncTask<String, Void, Boolean> {
    //...여기에 코드추가...
        private String title, text;
        private Bitmap bitmap;

        public PutPost(String title, String text, Bitmap bitmap) {
            this.title = title;
            this.text = text;
            this.bitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            String boundary = "----Boundary" + System.currentTimeMillis();
            String token = "eb94ccee2d73b971c091889e06cba4829f0cb594"; // 본인 Django 토큰 값

            try {
                String now = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US).format(new java.util.Date());
                HttpURLConnection conn = (HttpURLConnection) new URL(urls[0]).openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream out = conn.getOutputStream();

                // 1. author, title, text 필드 루프 전송
                String[] keys = {"author", "title", "text", "created_date", "published_date"};
                String[] values = {"1", title, text, now, now};
                for (int i = 0; i < keys.length; i++) {
                    String part = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"" + keys[i] + "\"\r\n\r\n" +
                            values[i] + "\r\n";
                    out.write(part.getBytes("UTF-8"));
                }

                // 2. 이미지 바이너리 전송
                String imgPart = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"\r\n" +
                        "Content-Type: image/jpeg\r\n\r\n";
                out.write(imgPart.getBytes("UTF-8"));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                out.write(baos.toByteArray());
                out.write("\r\n".getBytes("UTF-8"));

                // 3. 전송 마감
                out.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
                out.flush();
                out.close();

                int code = conn.getResponseCode();
                return (code == 201 || code == 200);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
