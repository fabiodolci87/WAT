package nespoli.dolci.wat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used to upload a picture to the server
 *
 * @author Mirko
 */
public class InsertPic extends Activity implements OnClickListener {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static int RESULT_LOAD_IMG = 1;
    private boolean takePic = false;
    private int serverResponseCode = 0;
    private Button uploadButton, btnselectpic, mbuttonPhoto;
    private ImageView imageview;
    private ProgressDialog dialog = null;
    private String upLoadServerUri = "http://watlocate.altervista.org/imgupload/upload_image.php";
    private String imagepath = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);

        uploadButton = (Button) findViewById(R.id.buttonLoadPicture);
        btnselectpic = (Button) findViewById(R.id.button_selectpic);
        mbuttonPhoto = (Button) findViewById(R.id.buttonPhoto);
        imageview = (ImageView) findViewById(R.id.imgView);

        mbuttonPhoto.setOnClickListener(this);
        btnselectpic.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View arg0) {
        if (arg0 == btnselectpic) {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        } else if (arg0 == uploadButton) {

            dialog = ProgressDialog.show(InsertPic.this, "", "Uploading file...", true);
            new Thread(new Runnable() {
                public void run() {

                    runOnUiThread(new Runnable() {
                        public void run() {
                        }
                    });
                    int response = uploadFile(imagepath);
                    System.out.println("RES : " + response);
                    finish();
                }
            }).start();
        } else if (arg0 == mbuttonPhoto) {
            takePic = true;
            Intent pic = new Intent(this, TakePhoto.class);
            startActivityForResult(pic, RESULT_LOAD_IMG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK && takePic == false) {
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            imageview.setImageBitmap(bitmap);
        } else if (takePic == true) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            imagepath = settings.getString("image_path", "xxx");
            dialog = ProgressDialog.show(InsertPic.this, "", "Uploading file...", true);
            new Thread(new Runnable() {
                public void run() {

                    runOnUiThread(new Runnable() {
                        public void run() {
                        }
                    });
                    int response = uploadFile(imagepath);
                    System.out.println("RES : " + response);
                    finish();
                }
            }).start();
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * This method let us uploading a file to the server
     *
     * @param sourceFileUri Is the namefile and the path of the file that we are going to upload
     * @return returns the response code of the operation
     */
    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File Does not exist");
            return 0;
        }
        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection(); // Opens a HTTP  connection to the URL
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necessary after file data
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if (serverResponseCode == 200) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(InsertPic.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //close the file streams
            fileInputStream.close();
            dos.flush();
            dos.close();

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor edit = settings.edit();
            edit.putString("file_name", fileName);
            edit.commit();

        } catch (MalformedURLException ex) {
            dialog.dismiss();
            ex.printStackTrace();
            Toast.makeText(InsertPic.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            dialog.dismiss();
            e.printStackTrace();
            Toast.makeText(InsertPic.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Upload file Exception", "Exception : " + e.getMessage(), e);
        }
        dialog.dismiss();
        return serverResponseCode;
    }
}