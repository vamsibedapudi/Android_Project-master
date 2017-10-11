package com.example.bhavan.android_outlab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SeePosts extends AppCompatActivity {
    SharedPreferences prefs;
    CookieManager msCookieManager = new java.net.CookieManager();
    ArrayAdapter<JSONObject> adapter;
    ArrayList<JSONObject> data;
    String uid;
    TextView loadmore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String COOKIES_HEADER = "Set-Cookie";
        prefs = getSharedPreferences("MyPrefs",MODE_PRIVATE);
        String cookies = prefs.getString("cookie","");
        if(cookies.equals("")){
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finish();
        }
        JSONObject postData = new JSONObject();

        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("uid");
        try {
            postData.put("uid", uid);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String postUid = null;
        try {
            postUid = JsontoPost(postData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_see_posts);

        data = new ArrayList();



        adapter = new UsersAdapter(SeePosts.this, data);
        ListView listView = (ListView)findViewById(R.id.posts);
        listView.setAdapter(adapter);


        new connect().execute("http://"+MainActivity.IP+":"+MainActivity.port+"/"+MainActivity.projec_name+"/SeeUserPosts",cookies,postUid);




    }


    public class UsersAdapter extends ArrayAdapter<JSONObject> {
        public UsersAdapter(Context context, ArrayList<JSONObject> users) {
            super(context, 0, users);
        }

        JSONObject post;

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            post = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_post, parent, false);
            try {
                ((TextView)convertView.findViewById(R.id.uid)).setText(post.getString("uid"));
                ((TextView)convertView.findViewById(R.id.time)).setText(post.getString("timestamp"));
                ((TextView)convertView.findViewById(R.id.text)).setText(post.getString("text"));
                final TextView addComment= (convertView.findViewById(R.id.addComment));
                addComment.setTag(R.id.addComment,post.get("postid"));
                addComment.setOnClickListener(new View.OnClickListener() {
                                                  public void onClick(View v) {

                                                      String postid = v.getTag(R.id.addComment).toString();
                                                      Log.e("postid-----","--------------"+postid);
                                                      Intent intent = new Intent(SeePosts.this, AddComment.class);
                                                      intent.putExtra("postid", postid);
                                                      intent.putExtra("return","SeePosts");
                                                      intent.putExtra("uid",uid);
                                                      startActivity(intent);

                                                  }
                                              }
                );
                Log.e("===============",post.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                final JSONArray comments = post.getJSONArray("Comment");
                Log.e("---------------Comments",""+post.getString("uid")+comments.toString()+"--------------------");

                final LinearLayout linearLayout = convertView.findViewById(R.id.comments);

                if(comments.length()<=3) {
                    for (int i = 0; i < comments.length(); i++) {
                        View comment_view = LayoutInflater.from(getContext()).inflate(R.layout.single_comment, parent, false);
                        JSONObject jsonObject = comments.getJSONObject(i);
                        ((TextView) comment_view.findViewById(R.id.uid)).setText(jsonObject.getString("uid"));
                        ((TextView) comment_view.findViewById(R.id.text)).setText(jsonObject.getString("text"));
                        linearLayout.addView(comment_view);
                    }
                    TextView loadmore = (TextView) convertView.findViewById(R.id.loadmore);
                    loadmore.setVisibility(View.GONE) ;

                }else{

                    for (int i = 0; i < 3; i++) {
                        View comment_view = LayoutInflater.from(getContext()).inflate(R.layout.single_comment, parent, false);
                        JSONObject jsonObject = comments.getJSONObject(i);
                        ((TextView) comment_view.findViewById(R.id.uid)).setText(jsonObject.getString("uid"));
                        ((TextView) comment_view.findViewById(R.id.text)).setText(jsonObject.getString("text"));
                        linearLayout.addView(comment_view);
                    }

                    loadmore = (TextView) convertView.findViewById(R.id.loadmore);
                    loadmore.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            for (int i = 3; i < comments.length(); i++) {
                                View comment_view = LayoutInflater.from(getContext()).inflate(R.layout.single_comment, parent, false);
                                JSONObject jsonObject;
                                try {
                                    jsonObject = comments.getJSONObject(i);
                                    ((TextView) comment_view.findViewById(R.id.uid)).setText(jsonObject.getString("uid"));
                                    ((TextView) comment_view.findViewById(R.id.text)).setText(jsonObject.getString("text"));
                                    linearLayout.addView(comment_view);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                            loadmore.setVisibility(View.GONE) ;

                        }
                    });


                }





            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Return the completed view to render on screen

            return convertView;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_home:

                if(!this.getClass().getName().equals(HomeActivity.class.getClass().getName())){
                    intent = new Intent(this, HomeActivity.class);
                    this.startActivity(intent);
                }


                break;
            case R.id.action_search:
                if(!this.getClass().getName().equals(SearchActivity.class.getClass().getName())){
                    intent = new Intent(this, SearchActivity.class);
                    this.startActivity(intent);
                }

                break;
            case R.id.action_logout:
                SharedPreferences.Editor editor= prefs.edit();
                editor.clear();
                editor.apply();
                intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }








    private class connect extends AsyncTask<String, String,  String> {
        @Override
        protected String doInBackground(String... params) {
            String response=null;
            URL url= null;
            String COOKIES_HEADER = "Set-Cookie";
            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);
            try {
                conn.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            conn.setRequestProperty("Cookie",params[1]);
            try {
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                OutputStreamWriter writer= new OutputStreamWriter(conn.getOutputStream());
                Log.e("LOG", params[2]);
                writer.write(params[2]);
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                InputStream inputStream = new BufferedInputStream(conn.getInputStream());

                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                response = total.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }



            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    Log.e("Cookie", cookie);

                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(SeePosts.this, result, Toast.LENGTH_SHORT).show();
            Log.e("result...........",result);
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                if(!jsonObject.getBoolean("status")){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(SeePosts.this, MainActivity.class);
                    SeePosts.this.startActivity(intent);
                    SeePosts.this.finish();


                }else{
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    data.clear();
                    for (int i=0;i<jsonArray.length();i++){
                        data.add(jsonArray.getJSONObject(i));
//                        Log.e("**********",jsonArray.getJSONObject(i).toString());
                    }

                    adapter.notifyDataSetChanged();

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }



    public  String JsontoPost(JSONObject jsonObject) throws Exception{
        StringBuilder result = new StringBuilder();
        Boolean first =true;
        Iterator<String> itr = jsonObject.keys();
        while(itr.hasNext()){
            String key = itr.next();
            Object value = jsonObject.get(key);
            if(first){
                first=false;
            }else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key,"UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(),"UTF-8"));
        }
        return  result.toString();
    }


}
