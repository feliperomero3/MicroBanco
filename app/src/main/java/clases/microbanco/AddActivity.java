package clases.microbanco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends Activity
{
    private ArrayAdapter<String> adapter;
    private String origen;
    private ListView lstDatos;
    private TextView lblAñadir;
    private int huboCambios;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        
        huboCambios = 0;
        final LinearLayout llAñadir = (LinearLayout)findViewById(R.id.cabecera);
        lblAñadir = (TextView)findViewById(R.id.lblAñadir);
        lstDatos = (ListView)findViewById(R.id.lstDatos);
        final Button btnAceptar = (Button)findViewById(R.id.btnAceptar);
        registerForContextMenu(lstDatos);
        
        origen = getIntent().getStringExtra("BotónOrigen");
        ActualizarListaDatos();
        
        llAñadir.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
                builder.setTitle(R.string.añadir);
                
                // Get the layout inflater
                LayoutInflater inflater = AddActivity.this.getLayoutInflater();
                
                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                final View layout = inflater.inflate(R.layout.xml_custom_alert_dialog, null);
                builder.setView(layout);
                
                builder.setPositiveButton(R.string.guardar, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        EditText elem = (EditText)layout.findViewById(R.id.txtNuevoItem);
                        
                        // Si no es cadena vacía, haz...
                        if (!("").equals(elem.getText().toString()))
                        {
                            SqlCliente sqlcte = new SqlCliente(AddActivity.this);
                            ContentValues valores = new ContentValues();
                            
                            valores.put("Nombre", elem.getText().toString());
                            
                            if (origen.equals("ibtnBanco"))
                            {
                                sqlcte.Insertar("Bancos", valores);
                            }
                            else if (origen.equals("ibtnTipo"))
                            {
                                sqlcte.Insertar("TiposCuenta", valores);
                            }
                            else if (origen.equals("ibtnConcepto"))
                            {
                                sqlcte.Insertar("Conceptos", valores);
                            }
                            
                            ActualizarListaDatos();
                            huboCambios = 1;
                        }
                        else
                        {
                            Toast.makeText(AddActivity.this, "Nombre no válido",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
                builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.cancel();
                    }
                });
                
                // Create the AlertDialog object & show it
                builder.create().show();
            }
        });
        
        llAñadir.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                if (v.getId() == R.id.cabecera)
                {
                    switch (e.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            llAñadir.setBackgroundColor(Color.parseColor("#0000FF"));
                            break;
                        
                        case MotionEvent.ACTION_UP:
                            llAñadir.setBackgroundColor(Color.parseColor("#4682B4"));
                            break;
                    }
                }
                return false;
            }
        });
        
        btnAceptar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(huboCambios == 1)
                {
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();
                }
                else
                {
                    Intent i = new Intent();
                    setResult(RESULT_CANCELED, i);
                    finish();
                }
            }
        });   
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(huboCambios == 1)
            {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
            }
            else
            {
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void ActualizarListaDatos()
    {
        SqlCliente sqlcte = new SqlCliente(this);
        
        // Mostrar la lista respectiva de cada Activity que inició esta otra
        if (origen.equals("ibtnBanco"))
        {
            Cursor c = sqlcte.ObtenerTodos("Bancos", null, "Nombre ASC");
            
            if (c.moveToFirst())
            {
                String[] datos = new String[c.getCount()];
                
                for (int i = 0; i < c.getCount(); i++)
                {
                    datos[i] = c.getString(0);
                    c.moveToNext();
                }
                c.close();
                sqlcte.Cerrar();
                
                // Cargar bancos
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                        datos);
                lstDatos.setAdapter(adapter);
                
                lblAñadir.setText("Añadir bancos");
            }
        }
        else if (origen.equals("ibtnTipo"))
        {
            sqlcte = new SqlCliente(this);
            Cursor c = sqlcte.ObtenerTodos("TiposCuenta", null, "Nombre ASC");
            
            if (c.moveToFirst())
            {
                String[] datos = new String[c.getCount()];
                
                for (int i = 0; i < c.getCount(); i++)
                {
                    datos[i] = c.getString(0);
                    c.moveToNext();
                }
                c.close();
                sqlcte.Cerrar();
                
                // Cargar bancos
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                        datos);
                lstDatos.setAdapter(adapter);
                
                lblAñadir.setText("Añadir tipos de cuenta");
            }
        }
        else if (origen.equals("ibtnConcepto"))
        {
            sqlcte = new SqlCliente(this);
            Cursor c = sqlcte.ObtenerTodos("Conceptos", null, "Nombre ASC");
            
            if (c.moveToFirst())
            {
                String[] datos = new String[c.getCount()];
                
                for (int i = 0; i < c.getCount(); i++)
                {
                    datos[i] = c.getString(0);
                    c.moveToNext();
                }
                c.close();
                sqlcte.Cerrar();
                
                // Cargar bancos
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                        datos);
                lstDatos.setAdapter(adapter);
                
                lblAñadir.setText("Añadir conceptos");
            }
        }
    }
    
    public void onCreateContextMenu(ContextMenu menú, View v, ContextMenuInfo infoMenú)
    {
        if (v.getId() == R.id.lstDatos)
        {
            menú.setHeaderTitle(R.string.eligaAcción);
            String[] opcionesMenú = getResources().getStringArray(R.array.accionesMovimiento);
            for (int i = 0; i < opcionesMenú.length; i++)
            {
                menú.add(Menu.NONE, i, i, opcionesMenú[i]);
            }
        }
    }
    
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item
                .getMenuInfo();
        int opciónElegida = item.getItemId();
        String elem = adapter.getItem(info.position);
        
        SqlCliente sqlcte = new SqlCliente(this);
        String cláusulaW = "Nombre = ?";
        String[] argsW = new String[] { elem };
        
        // Eliminar
        if (opciónElegida == 0)
        {
            // No permitir eliminar bancos en uso
            if (origen.equals("ibtnBanco"))
            {
                String clauW = "Banco = ?";
                
                if (!sqlcte.Buscar("Cuentas", clauW, argsW))
                {
                    sqlcte.Eliminar("Bancos", cláusulaW, argsW);
                }
                else
                {
                    Toast.makeText(this,
                            "Existen cuentas asociadas a este banco (operación cancelada)",
                            Toast.LENGTH_SHORT).show();
                }
            }
            // No permitir eliminar tipos de cuenta en uso
            else if (origen.equals("ibtnTipo"))
            {
                String clauW = "Tipo = ?";
                
                if (!sqlcte.Buscar("Cuentas", clauW, argsW))
                {
                    sqlcte.Eliminar("TiposCuenta", cláusulaW, argsW);
                }
                else
                {
                    Toast.makeText(this,
                            "Existen cuentas asociadas a este tipo (operación cancelada)",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else if (origen.equals("ibtnConcepto"))
            {
                sqlcte.Eliminar("Conceptos", cláusulaW, argsW);
            }
        }
        return true;
    }
  
}
