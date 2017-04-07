package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by faebuk on 07.04.2017.
 */

public class EnterCodeActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "EnterCodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.enter_code);

        findViewById(R.id.submitForm).setOnClickListener(this);
        ((EditText)findViewById(R.id.codeText)).setFilters(new InputFilter[]{ new InputFilter.AllCaps()});
        findViewById(R.id.progressBarHolder).setVisibility(View.GONE);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submitForm) {

            String code = ((EditText)findViewById(R.id.codeText)).getText().toString().toUpperCase();
            Log.d("CUSTOM", code);

            try {
                String result = new LongOperation().execute(code).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            // launch Ocr capture activity.
            //Intent intent = new Intent(this, EnterCodeActivity.class);

            //startActivityForResult(intent, RC_OCR_CAPTURE);
        }
    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost("http://192.168.1.78:8080/api/chesterfieldocr");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("code", params[0]));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream inputstream = entity.getContent();

                StringWriter writer = new StringWriter();
                IOUtils.copy(inputstream, writer, "UTF-8");
                String theString = writer.toString();

                return theString;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            findViewById(R.id.progressBarHolder).setVisibility(View.GONE);
            Intent data = new Intent();
            data.putExtra(OcrCaptureActivity.TextBlockObject, result);
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progressBarHolder).setVisibility(View.VISIBLE);
            findViewById(R.id.progressBarHolder).bringToFront();
            findViewById(R.id.progressBarHolder).invalidate();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
