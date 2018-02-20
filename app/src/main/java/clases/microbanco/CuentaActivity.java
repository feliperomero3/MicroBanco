package clases.microbanco;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CuentaActivity extends Activity
{
    private final int CÓDIGO_NUEVO = 0;
    private int huboCambios = 0;
    private String númeroEdición;
    private String bancoEdición;
    private ArrayAdapter<String> adapBanco;
    private Spinner cmbBanco;
    private Spinner cmbTipo;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);
        
        final EditText txtNúmero = (EditText)findViewById(R.id.txtNúmero);
        cmbBanco = (Spinner)findViewById(R.id.cmbBanco);
        final ImageButton ibtnBanco = (ImageButton)findViewById(R.id.ibtnBanco);
        cmbTipo = (Spinner)findViewById(R.id.cmbTipo);
        final ImageButton ibtnTipo = (ImageButton)findViewById(R.id.ibtnTipo);
        final TextView lblSaldoInicial2 = (TextView)findViewById(R.id.lblSaldoInicial2);
        final EditText txtSaldo = (EditText)findViewById(R.id.txtSaldo);
        
        Button btnGuardar = (Button)findViewById(R.id.btnGuardar);
        Button btnCancelar = (Button)findViewById(R.id.btnCancelar);

        ActualizarContenidoSpinner();
        
        // Checar si la Activity se inició con la acción "Editar" del menú contextual
        númeroEdición = getIntent().getStringExtra("Número");
        bancoEdición = getIntent().getStringExtra("Banco");
        
        if (númeroEdición != null)
        {
            txtNúmero.setText(númeroEdición);
            cmbBanco.setSelection(adapBanco.getPosition(bancoEdición));
            LlenarWidgets(númeroEdición, bancoEdición);
        }
        
        txtNúmero.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus && txtNúmero.getText().toString().length() > 0)
                {
                    String número = txtNúmero.getText().toString();
                    String banco = (String)cmbBanco.getSelectedItem();
                    
                    LlenarWidgets(número, banco);
                }
            }
        });
        
        cmbBanco.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                if (txtNúmero.getText().toString().length() > 0)
                {
                    String número = txtNúmero.getText().toString();
                    String banco = (String)arg0.getSelectedItem();
                    
                    LlenarWidgets(número, banco);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });
        
        ibtnBanco.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent("clases.microbanco.AddActivity");
                i.putExtra("BotónOrigen", "ibtnBanco");
                startActivityForResult(i, CÓDIGO_NUEVO);
            }
            
        });
        
        ibtnTipo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent("clases.microbanco.AddActivity");
                i.putExtra("BotónOrigen", "ibtnTipo");
                startActivityForResult(i, CÓDIGO_NUEVO);
            }
            
        });
        
        btnGuardar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SqlCliente sqlcte = new SqlCliente(CuentaActivity.this);
                
                String número = txtNúmero.getText().toString();
                String banco = (String)cmbBanco.getSelectedItem();
                String tipo = (String)cmbTipo.getSelectedItem();
                
                ContentValues valores = new ContentValues();
                valores.put("Número", número);
                valores.put("Banco", banco);
                valores.put("Tipo", tipo);
                
                String cláusulaW = "Número = ? AND Banco = ?";
                String[] argsW = new String[] { número, banco };
                
                Cursor c = sqlcte.Obtener("Cuentas", new String[] { "Tipo" }, cláusulaW, argsW,
                        null);
                
                if (c.moveToFirst())
                {
                    // Edición de una cuenta existente
                    c.close();
                    sqlcte.Cerrar();
                    
                    ContentValues cvTipo = new ContentValues();
                    cvTipo.put("Tipo", tipo);
                    
                    if (sqlcte.Actualizar("Cuentas", cvTipo, cláusulaW, argsW))
                    {
                        Toast.makeText(CuentaActivity.this, "Cuenta actualizada con éxito",
                                Toast.LENGTH_SHORT).show();
                        
                        // Borrar texto de los widgets
                        txtNúmero.setText("");
                        txtSaldo.setText("");
                        txtSaldo.setEnabled(true);
                        lblSaldoInicial2.setText("");
                        huboCambios = 1;
                        
                        if (númeroEdición != null)
                        {
                            // Fue edición desde el menú contextual.
                            // Salir después de guardar cambios
                            Salir();
                        }
                    }
                    
                }
                else
                {
                    // Cuenta nueva
                    float saldo = 0.0F;
                    try
                    {
                        saldo = Float.parseFloat(txtSaldo.getText().toString());
                    }
                    catch (NumberFormatException ex)
                    {
                        Toast.makeText(CuentaActivity.this, "Faltan datos", Toast.LENGTH_SHORT)
                                .show();
                        ex.printStackTrace();
                        return;
                    }
                    
                    float saldoInicial = saldo;
                    valores.put("Saldo", saldo);
                    valores.put("SaldoInicial", saldoInicial);
                    
                    if (!número.equals(""))
                    {
                        if (sqlcte.Insertar("Cuentas", valores) != -1)
                        {
                            Toast.makeText(CuentaActivity.this, "Cuenta almacenada con éxito",
                                    Toast.LENGTH_SHORT).show();
                            
                            // Borrar texto de los widgets
                            txtNúmero.setText("");
                            txtSaldo.setText("");
                            lblSaldoInicial2.setText("");
                            huboCambios = 1;
                        }
                        else
                        {
                            Toast.makeText(CuentaActivity.this,
                                    "Error al intentar almacenar la cuenta", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    else
                    {
                        Toast.makeText(CuentaActivity.this, "Faltan datos", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                c.close();
                sqlcte.Cerrar();
            }
        });
        
        btnCancelar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Salir();
            }
        });
    }
    
    private void ActualizarContenidoSpinner()
    {
        SqlCliente sqlcte = new SqlCliente(this);
        Cursor c = sqlcte.ObtenerTodos("Bancos", null, "Nombre ASC");
        String[] datos;
        adapBanco = null;
        
        if (c.moveToFirst())
        {
            datos = new String[c.getCount()];
            
            for (int i = 0; i < c.getCount(); i++)
            {
                datos[i] = c.getString(0);
                c.moveToNext();
            }
            
            // Cargar nombres en cmbBanco
            adapBanco = new ArrayAdapter<String>(this, R.layout.xml_custom_simple_spinner_item, datos);
            adapBanco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbBanco.setAdapter(adapBanco);
        }
        c.close();
        sqlcte.Cerrar();
        
        c = sqlcte.ObtenerTodos("TiposCuenta", null, "Nombre ASC");
        
        if (c.moveToFirst())
        {
            datos = new String[c.getCount()];
            
            for (int i = 0; i < c.getCount(); i++)
            {
                datos[i] = c.getString(0);
                c.moveToNext();
            }
            
            // Cargar nombres en cmbTipo
            ArrayAdapter<String> adapTipo = new ArrayAdapter<String>(this,
                    R.layout.xml_custom_simple_spinner_item, datos);
            adapTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbTipo.setAdapter(adapTipo);
        }
    }
    
    private void LlenarWidgets(String número, String banco)
    {
        EditText txtNúmero = (EditText)findViewById(R.id.txtNúmero);
        Spinner cmbBanco = (Spinner)findViewById(R.id.cmbBanco);
        ImageButton ibtnBanco = (ImageButton)findViewById(R.id.ibtnBanco);
        Spinner cmbTipo = (Spinner)findViewById(R.id.cmbTipo);
        ImageButton ibtnTipo = (ImageButton)findViewById(R.id.ibtnTipo);
        EditText txtSaldo = (EditText)findViewById(R.id.txtSaldo);
        TextView lblSaldoInicial2 = (TextView)findViewById(R.id.lblSaldoInicial2);
        
        // Consultar la bd por el resto de los datos
        SqlCliente sqlcte = new SqlCliente(CuentaActivity.this);
        String cláusulaW = "Número = ? AND Banco = ?";
        String[] argsW = new String[] { número, banco };
        Cursor c = sqlcte.Obtener("Cuentas", null, cláusulaW, argsW, null);
        
        if (c.moveToFirst())
        {
            
            TextView lblSaldoInicial = (TextView)findViewById(R.id.lblSaldoInicial);
            
            // Tipo
            @SuppressWarnings("unchecked")
            ArrayAdapter<String> adapTipos = (ArrayAdapter<String>)cmbTipo.getAdapter();
            cmbTipo.setSelection(adapTipos.getPosition(c.getString(2)));
            
            // Saldo
            txtSaldo.setText(String.format("%,.2f", c.getFloat(3)));
            
            // Saldo inicial
            lblSaldoInicial.setText(R.string.saldoInicial);
            lblSaldoInicial2.setText(String.format("%,.2f", c.getFloat(4)));
            
            c.close();
            sqlcte.Cerrar();
            
            txtNúmero.setEnabled(false);
            ibtnBanco.setEnabled(false);
            ibtnTipo.setEnabled(false);
            txtSaldo.setEnabled(false);
            
            if (númeroEdición != null)
            {
                cmbBanco.setEnabled(false);
            }
        }
        else
        {
            txtNúmero.setEnabled(true);
            cmbBanco.setEnabled(true);
            txtSaldo.setEnabled(true);
            lblSaldoInicial2.setText("");
        }
    }
    
    private void Salir()
    {
        Intent i = new Intent();
        i.putExtra("huboCambios", huboCambios);
        setResult(RESULT_OK, i);
        finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Salir();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CÓDIGO_NUEVO)
        {
            if (resultCode == RESULT_OK)
            {
                ActualizarContenidoSpinner();
            }
        }
    }
    
}
