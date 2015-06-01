package nespoli.dolci.wat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Activity that permits to an user to choose if login
 * to an existing trip or create a new one
 *
 * @author Fabio
 */
public class JoinCreate extends Activity implements View.OnClickListener {

    public String username;
    private Button mJoin, mCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_create);
        mJoin = (Button) findViewById(R.id.button_join);
        mCreate = (Button) findViewById(R.id.button_create);
        mJoin.setOnClickListener(this);
        mCreate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_join:
                //when join button is pressed go to select the group that user wants to join
                Intent i_join = new Intent(this, JoinGroup.class);
                startActivity(i_join);
                break;
            case R.id.button_create:
                //when create button is pressed go to the creation of a new group
                Intent i_create = new Intent(this, CreateGroup.class);
                startActivity(i_create);
                break;
            default:
                break;
        }
    }

    /**
     * When the system back button is pressed ends the actual activity
     */
    @Override
    public void onBackPressed() {

        finish();
    }

}
