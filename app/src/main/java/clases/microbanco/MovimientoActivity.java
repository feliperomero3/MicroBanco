package clases.microbanco;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MovimientoActivity extends Activity
{
    private final int CÓDIGO_NUEVO = 0;
    private String saldoActual;
    private int huboCambios = 0;
    private int año, mes, día;
    private DatePickerDialog dpFecha;
    private Spinner cmbConcepto;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);
        
        // Etiquetas de encabezado
        final TextView lblNúmeroCuenta = (TextView)findViewById(R.id.lblCuentaElegidaNúmero);
        final TextView lblBancoCuenta = (TextView)findViewById(R.id.lblCuentaElegidaBanco);
        final TextView lblSaldoCuenta = (TextView)findViewById(R.id.lblCuentaElegidaSaldo);
        
        final Button btnFecha = (Button)findViewById(R.id.btnFecha);
        final Spinner cmbTipo = (Spinner)findViewById(R.id.cmbTipo);
        cmbConcepto = (Spinner)findViewById(R.id.cmbConcepto);
        final ImageButton ibtnConcepto = (ImageButton)findViewById(R.id.ibtnConcepto);
        final EditText txtImporte = (EditText)findViewById(R.id.txtImporte);
        
        Button btnGuardar = (Button)findViewById(R.id.btnGuardar);
        Button btnCancelar = (Button)findViewById(R.id.btnCancelar);
        
        // Colocar datos de la cuenta
        lblNúmeroCuenta.setText(getIntent().getStringExtra("Número"));
        lblBancoCuenta.setText(getIntent().getStringExtra("Banco"));
        lblSaldoCuenta.setText(getIntent().getStringExtra("Saldo"));
        saldoActual = getIntent().getStringExtra("Saldo");
        
        // Obtener la fecha actual
        Calendar cal = Calendar.getInstance();
        año = cal.get(Calendar.YEAR);
        mes = cal.get(Calendar.MONTH);
        día = cal.get(Calendar.DAY_OF_MONTH);
        
        GregorianCalendar greCal = new GregorianCalendar(año, mes, día);
        
        // Objeto que aplicará el formato de fecha local del sistema
        java.text.DateFormat formatoFecha = DateFormat.getDateFormat(this);
        
        // Actualizar texto del btnFecha
        btnFecha.setText(formatoFecha.format(greCal.getTime()));
        
        // Cargar tipos
        String[] tipos = getResources().getStringArray(R.array.tiposMovimiento);
        ArrayAdapter<String> adapTipo = new ArrayAdapter<String>(this,
                R.layout.xml_custom_simple_spinner_item, tipos);
        adapTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbTipo.setAdapter(adapTipo);
        cmbTipo.setSelection(1);
        
        // Cargar conceptos
        ActualizarSpinner();
        
        btnFecha.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                // Create a new instance of DatePickerDialog & show it
                dpFecha = new DatePickerDialog(MovimientoActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                    int dayOfMonth)
                            {
                                año = year;
                                mes = monthOfYear;
                                día = dayOfMonth;
                                
                                GregorianCalendar greCal = new GregorianCalendar(año, mes, día);
                                java.text.DateFormat formatoFecha = DateFormat
                                        .getDateFormat(MovimientoActivity.this);
                                
                                // Actualizar texto del btnFecha
                                btnFecha.setText(formatoFecha.format(greCal.getTime()));
                            }
                        }, año, mes, día);
                dpFecha.show();
            }
        });
        
        ibtnConcepto.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent("clases.microbanco.AddActivity");
                i.putExtra("BotónOrigen", "ibtnConcepto");
                startActivityForResult(i, CÓDIGO_NUEVO);
            }
        });
        
        btnGuardar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                GregorianCalendar fechaElegida = new GregorianCalendar(año, mes, día);
                
                // Guardar fecha en sqlite como "yyyy-MM-dd" para poder ordenar
                String fecha = (String)DateFormat.format("yyyy-MM-dd", fechaElegida);
                
                float importe = 0.0F;
                
                try
                {
                    importe = Float.parseFloat(txtImporte.getText().toString());
                }
                catch (NumberFormatException ex)
                {
                    Toast.makeText(MovimientoActivity.this, "Falta el importe", Toast.LENGTH_SHORT)
                            .show();
                    ex.printStackTrace();
                    return;
                }
                
                String tipo = (String)cmbTipo.getSelectedItem();
                String cuenta = getIntent().getStringExtra("Número");
                String banco = getIntent().getStringExtra("Banco");
                String concepto = (String)cmbConcepto.getSelectedItem();
                
                ContentValues valores = new ContentValues();
                valores.put("Fecha", fecha);
                valores.put("NúmeroCuenta", cuenta);
                valores.put("NombreBanco", banco);
                valores.put("Tipo", tipo);
                valores.put("Concepto", concepto);
                valores.put("Importe", importe);
                
                SqlCliente sqlcte = new SqlCliente(MovimientoActivity.this);
                
                if (sqlcte.Insertar("Movimientos", valores) != -1)
                {
                    Toast.makeText(MovimientoActivity.this, "Movimiento almacenado con éxito",
                            Toast.LENGTH_SHORT).show();
                    
                    // El saldo de la cuenta se actualiza automáticamente con un Trigger
                    
                    TextView lblSaldoCuenta = (TextView)findViewById(R.id.lblCuentaElegidaSaldo);
                    String cláusulaW = "Número = ? AND Banco = ?";
                    String[] argsW = new String[] { cuenta, banco };
                    sqlcte = new SqlCliente(MovimientoActivity.this);
                    Cursor c = sqlcte.Obtener("Cuentas", new String[] { "Saldo" }, cláusulaW,
                            argsW, null);
                    
                    if (c.moveToFirst())
                    {
                        // Actualizar saldo del encabezado
                        lblSaldoCuenta.setText("$".concat(String.format("%,.2f", c.getFloat(0))));
                        saldoActual = "$".concat(String.format("%,.2f", c.getFloat(0)));
                    }
                    
                    txtImporte.setText("");
                    huboCambios = 1;
                    c.close();
                    sqlcte.Cerrar();
                }
                else
                {
                    Toast.makeText(MovimientoActivity.this,
                            "Error al intentar almacenar el movimiento", Toast.LENGTH_SHORT)
                            .show();
                    sqlcte.Cerrar();
                }
                
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
    
    private void ActualizarSpinner()
    {
        SqlCliente sqlcte = new SqlCliente(this);
        Cursor c = sqlcte.ObtenerTodos("Conceptos", null, "Nombre ASC");
        
        if (c.moveToFirst())
        {
            String[] conceptos = new String[c.getCount()];
            
            for (int i = 0; i < c.getCount(); i++)
            {
                conceptos[i] = c.getString(0);
                c.moveToNext();
            }
            
            // Cargar conceptos
            ArrayAdapter<String> adapConceptos = new ArrayAdapter<String>(this,
                    R.layout.xml_custom_simple_spinner_item, conceptos);
            adapConceptos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbConcepto.setAdapter(adapConceptos);
        }
    }
    
    private void Salir()
    {
        Intent i = new Intent();
        i.putExtra("huboCambios", huboCambios);
        i.putExtra("Saldo", saldoActual);
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
                ActualizarSpinner();
            }
        }
    }
}
