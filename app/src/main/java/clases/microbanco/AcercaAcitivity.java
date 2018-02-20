package clases.microbanco;


import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AcercaAcitivity extends Activity
{
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);
        
        TextView lblLink = (TextView)findViewById(R.id.lblLink);
        TextView lblLinkWeb = (TextView)findViewById(R.id.lblLinkWeb);
        lblLink.setMovementMethod(LinkMovementMethod.getInstance());
        lblLinkWeb.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
