package nespoli.dolci.wat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

/**
 * This activity permits to retrieve an image from the server
 *
 * @author Mirko
 */
public class PicFromUrl extends Activity {
    ImageView img;
    Bitmap bitmap;
    ProgressDialog pDialog;
    String url = "http://watlocate.altervista.org/imgupload/uploadedimages/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_from_url);
        img = (ImageView) findViewById(R.id.img);
        Intent intent = getIntent();
        String easyPuzzle = intent.getExtras().getString("nome_pic");
        String completeUrl = url + easyPuzzle;
        new LoadImage().execute(completeUrl);
    }

    @Override
    public void onBackPressed() {

        finish();
        Intent read_comments = new Intent(PicFromUrl.this, ReadComments.class);
        startActivity(read_comments);
        System.exit(0);
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PicFromUrl.this);
            pDialog.setMessage("Loading Image ....");
            pDialog.show();
        }

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                img.setImageBitmap(image);
                img.setRotation(90);
                pDialog.dismiss();
            } else {
                pDialog.dismiss();
                Toast.makeText(PicFromUrl.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}