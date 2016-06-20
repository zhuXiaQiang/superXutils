package zxq.org.superxutiltext;

import android.os.Bundle;

import zxq.org.superxutil.baseSupper.NewBaseActivity;

public class MainActivity extends NewBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSuperView(R.layout.activity_main);
    }
}
