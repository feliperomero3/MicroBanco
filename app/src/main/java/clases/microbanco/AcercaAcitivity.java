/*
 * Copyright (c) 2018 Felipe Romero
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

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
