package nespoli.dolci.wat;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Here a HELP item is created to guide an user
 */

public class HelpActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    public void dismiss(View v) {
        finish();
    }

}