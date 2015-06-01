package nespoli.dolci.wat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This activity is used to take a picture and store it into the memory of the device
 *
 * @author Mirko
 */
public class TakePhoto extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private static String pathPic = null;
    Uri imageUri = null;
    TakePhoto CameraActivity = null;

    /**
     * This method let us to convert to file the URI of the image just taken with
     * the camera of the device
     *
     * @param imageUri URI of the photo
     * @param activity activity handler
     */
    public static void convertImageUriToFile(Uri imageUri, Activity activity) {

        Cursor cursor = null;

        try {
            String[] proj = {
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.ImageColumns.ORIENTATION
            };

            cursor = activity.managedQuery(
                    imageUri,         //  Get data for specific image URI
                    proj,             //  Which columns to return
                    null,             //  WHERE clause; which rows to return (all rows)
                    null,             //  WHERE clause selection arguments (none)
                    null              //  Order-by clause (ascending by name)
            );
            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor.moveToFirst()) {
                String Path = cursor.getString(file_ColumnIndex);
                pathPic = Path;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        CameraActivity = this;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String fileName = sdf.format(now);
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                convertImageUriToFile(imageUri, CameraActivity);

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, " Picture was not taken ", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, " Picture was not taken ", Toast.LENGTH_SHORT).show();
            }
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor edit = settings.edit();
        edit.putString("image_path", pathPic);
        edit.commit();
        finish();
    }
}