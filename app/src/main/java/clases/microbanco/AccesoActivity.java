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
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccesoActivity extends Activity
{
    private SharedPreferences prefs;
    private EditText txtContraseña;
    private TextView lblContraseña;
    private TextView lblConfContraseña;
    private EditText txtConfContraseña;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceso);

        prefs = getSharedPreferences("clases.microbanco.MicroBanco", MODE_PRIVATE);

        lblContraseña = (TextView)findViewById(R.id.lblContraseña);
        txtContraseña = (EditText)findViewById(R.id.txtContraseña);
        lblConfContraseña = (TextView)findViewById(R.id.lblConfContraseña);
        txtConfContraseña = (EditText)findViewById(R.id.txtConfContraseña);
        Button btnAceptar = (Button)findViewById(R.id.btnAceptar);
        Button btnCancelar = (Button)findViewById(R.id.btnCancelar);

        btnAceptar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SqlCliente sqlcte = new SqlCliente(AccesoActivity.this);
                
                String contraseña = txtContraseña.getText().toString();
                String confContraseña = txtConfContraseña.getText().toString();
                
                String cláusulaW = "Contraseña = ?";
                String[] argsW = new String[] { contraseña };
                
                // Si no son cadena vacía y son iguales
                if (!("").equals(contraseña) && contraseña.equals(confContraseña))
                {
                    if (sqlcte.Contar("Usuarios") > 0)
                    {
                        if (sqlcte.Buscar("Usuarios", cláusulaW, argsW))
                        {
                            txtContraseña.setText("");
                            
                            // Abrir actividad
                            startActivity(new Intent("clases.microbanco.AdminCuentaActivity"));
                        }
                        else
                        {
                            // Mostrar mensaje de contraseña incorrecta
                            Toast.makeText(AccesoActivity.this, "Contraseña incorrecta",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        // Primer inicio de sesión. Crear contraseña nueva.
                        ContentValues valores = new ContentValues();
                        valores.put("Contraseña", contraseña);
                        
                        if (sqlcte.Insertar("Usuarios", valores) != -1)
                        {
                            txtContraseña.setText("");
                            txtConfContraseña.setText("");
                            
                            prefs.edit().putBoolean("primerLanzamiento", false).commit();
                            
                            Toast.makeText(AccesoActivity.this,
                                    "Nueva contraseña creada con éxito", Toast.LENGTH_SHORT)
                                    .show();
                            
                            // Abrir actividad
                            startActivity(new Intent("clases.microbanco.AdminCuentaActivity"));
                        }
                    }
                }
                else if (txtConfContraseña.getVisibility() == View.INVISIBLE)
                {
                    if (sqlcte.Buscar("Usuarios", cláusulaW, argsW))
                    {
                        txtContraseña.setText("");
                        
                        // Abrir actividad
                        startActivity(new Intent("clases.microbanco.AdminCuentaActivity"));
                    }
                    else
                    {
                        // Mostrar mensaje de contraseña incorrecta
                        Toast.makeText(AccesoActivity.this, "Contraseña incorrecta",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(AccesoActivity.this, "Las contraseñas no coinciden",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btnCancelar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
    
    public void onResume()
    {
        super.onResume();
        if (prefs.getBoolean("primerLanzamiento", true))
        {
            lblContraseña.setText(R.string.nuevaContraseña);

        }
        else if (lblConfContraseña.getVisibility() == View.VISIBLE)
        {    
            // Ocultar la confirmación de contraseña
            lblContraseña.setText(R.string.contraseña);
            lblConfContraseña.setVisibility(View.INVISIBLE);
            txtConfContraseña.setVisibility(View.INVISIBLE);  
        }
    }
}
