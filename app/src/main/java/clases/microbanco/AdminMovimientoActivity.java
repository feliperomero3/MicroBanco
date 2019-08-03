package clases.microbanco;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AdminMovimientoActivity extends Activity
{
    private String filtroMovimiento, filtroMovimientoPer, cuenta, banco;
    private int año, mes, día;
    private SharedPreferences prefs;
    private TextView lblSaldoCuenta;
    private int huboCambios = 0;
    private ListView lstMovimientos;
    private SimpleAdapter adapter;
    private LinearLayout llAviso;
    private TextView aviso;
    
    private final int CÓDIGO_CREAR = 0;
    private final int CÓDIGO_FILTRO = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminmovimiento);

        final LinearLayout llCrearMovimiento = (LinearLayout)findViewById(R.id.adminmovimientoCabecera);
        llAviso = (LinearLayout)findViewById(R.id.adminmovimientoSubCabecera);
        
        // Etiquetas de encabezado
        final TextView lblNúmeroCuenta = (TextView)findViewById(R.id.lblCuentaElegidaNúmero);
        final TextView lblBancoCuenta = (TextView)findViewById(R.id.lblCuentaElegidaBanco);
        lblSaldoCuenta = (TextView)findViewById(R.id.lblCuentaElegidaSaldo);
        
        // Colocar datos de la cuenta
        cuenta = getIntent().getStringExtra("Número");
        banco = getIntent().getStringExtra("Banco");
        lblNúmeroCuenta.setText(cuenta);
        lblBancoCuenta.setText(banco);
        lblSaldoCuenta.setText(getIntent().getStringExtra("Saldo"));
        
        // Nombre de la preferencia de filtros en SharedPreferences
        // Se guarda el filtro seleccionado: ej: 0, 1, 2, 3, etc
        filtroMovimiento = "filtroMov".concat(cuenta).concat(banco);
        
        // Se guarda el intervalo de fechas como "fecha1,fecha2" como una sola cadena
        filtroMovimientoPer = "filtroMovPer".concat(cuenta).concat(banco);
        
        // Datos usados en el diálogo mostrado del filtro "Personalizado..."
        final Calendar c = Calendar.getInstance();
        año = c.get(Calendar.YEAR);
        mes = c.get(Calendar.MONTH);
        día = c.get(Calendar.DAY_OF_MONTH);
        
        MostrarMovimientos();
        
        llCrearMovimiento.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent("clases.microbanco.MovimientoActivity");
                i.putExtra("Número", getIntent().getStringExtra("Número"));
                i.putExtra("Banco", getIntent().getStringExtra("Banco"));
                i.putExtra("Saldo", (String)lblSaldoCuenta.getText());
                startActivityForResult(i, CÓDIGO_CREAR);
            }
        });
        
        llCrearMovimiento.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                if (v.getId() == R.id.adminmovimientoCabecera)
                {
                    switch (e.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            llCrearMovimiento.setBackgroundColor(Color.parseColor("#0000FF"));
                            break;
                        
                        case MotionEvent.ACTION_UP:
                            llCrearMovimiento.setBackgroundColor(Color.parseColor("#4682B4"));
                            break;
                    }
                }
                return false;
            }
        });
    }
    
    public void onResume()
    {
        super.onResume();
        
    }
    
    // Muestra avisos al usuario como la inexistencia de movimientos
    // o el filtro activo (de ser el caso)
    private void ActualizarAviso(String texto)
    {
        if (llAviso.indexOfChild(aviso) != -1)
        {
            // Sólo cambiar el texto
            aviso.setText(texto);

        }
        else
        {
            // No existen movimientos. Mostrar aviso.
            aviso = new TextView(this);
            aviso.setText(texto);
            aviso.setTextSize(14);
            aviso.setTypeface(Typeface.DEFAULT_BOLD);
            aviso.setBackgroundColor(Color.parseColor("#808080"));
            aviso.setTextColor(Color.WHITE);
            aviso.setPadding(0, 5, 0, 5);
            aviso.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            aviso.setGravity(Gravity.CENTER);
            
            llAviso.addView(aviso);
        }
    }
    
    private void RemoverAviso()
    {
        llAviso.removeView(aviso);
    }
    
    private String ObtenerFechaActual()
    {
        return (String)DateFormat.format("yyyy-MM-dd", ObtenerCalFechaActual());
    }
    
    private GregorianCalendar ObtenerCalFechaActual()
    {
        // Obtener la fecha actual
        Calendar cal = Calendar.getInstance();
        int año = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int día = cal.get(Calendar.DAY_OF_MONTH);
        return new GregorianCalendar(año, mes, día);
    }
    
    // Hace los cálculos de la fecha según el filtro seleccionado
    private String ObtenerFechaFiltro(int filtro)
    {
        // Obtener la fecha actual
        GregorianCalendar greCal = ObtenerCalFechaActual();
        
        // REF: ver R.array.filtros
        switch (filtro)
        {
            case 0:
                // "Todos"
                break;
            case 1:
                // "Hoy"
                break;
            case 2:
                // "Últimos 7 días"
                greCal.add(Calendar.DAY_OF_MONTH, -7);
                break;
            case 3:
                // "Últimos 30 días
                greCal.add(Calendar.DAY_OF_MONTH, -30);
                break;
            case 4:
                // CASO ESPECIAL: TODO: "Intervalo de fechas..."
                break;
        }
        
        return (String)DateFormat.format("yyyy-MM-dd", greCal);
    }
    
    private boolean MostrarMovimientos()
    {
        // Cambiar nombres para mantener la semántica con la bd
        String númeroCuenta = cuenta;
        String nombreBanco = banco;
        
        // Obtener el filtro seleccionado
        prefs = getSharedPreferences("clases.microbanco.MicroBanco", MODE_PRIVATE);
        int filtroElegido = prefs.getInt(filtroMovimiento, 0);
        
        // Obtener movimientos y cargar los movimientos acordes al filtro
        SqlCliente sqlcte = new SqlCliente(AdminMovimientoActivity.this);
        String cláusulaW = "NúmeroCuenta = ? AND NombreBanco = ?";
        String[] argsW = new String[] { númeroCuenta, nombreBanco };
        String orden = "Fecha DESC, Número DESC";
        String fechaFiltro = ObtenerFechaFiltro(filtroElegido);
        
        if (filtroElegido == 0)
        {
            // "Todos"
            RemoverAviso();
        }
        else if (filtroElegido == 1)
        {
            // "Hoy"
            cláusulaW = cláusulaW.concat(" AND Fecha = ?");
            argsW = new String[] { númeroCuenta, nombreBanco, fechaFiltro };
            ActualizarAviso(getResources().getString(R.string.filtro1));
            
        }
        else if (filtroElegido > 1 && filtroElegido < 4)
        {
            // "Últimos 7 días" ó "Últimos 30 días"
            cláusulaW = cláusulaW.concat(" AND Fecha BETWEEN ? AND ?");
            String fechaActual = ObtenerFechaActual();
            argsW = new String[] { númeroCuenta, nombreBanco, fechaFiltro, fechaActual };
            
            if (filtroElegido == 2)
                ActualizarAviso(getResources().getString(R.string.filtro2));
            else
                ActualizarAviso(getResources().getString(R.string.filtro3));
        }
        else if (filtroElegido == 4)
        {
            // Personalizado: intervalo de fechas
            prefs = getSharedPreferences("clases.microbanco.MicroBanco", MODE_PRIVATE);
            
            // Recuperar las fechas usadas la última vez en este filtro
            String fechas[] = prefs.getString(filtroMovimientoPer, "").split(",");
            String fecha1 = fechas[0];
            String fecha2 = fechas[1];
            
            // Mostrar fecha en el aviso en formato local del sistema
            ActualizarAviso("Ver: ".concat(String.format("desde %s hasta %s", fecha1, fecha2)));
            
            // Aplicar el formato correcto a la fecha para hacer la búsqueda en la bd
            fecha1 = Formato.ObtenerFechaSqlite(AdminMovimientoActivity.this, fecha1);
            fecha2 = Formato.ObtenerFechaSqlite(AdminMovimientoActivity.this, fecha2);
            
            cláusulaW = cláusulaW.concat(" AND Fecha BETWEEN ? AND ?");
            argsW = new String[] { númeroCuenta, nombreBanco, fecha1, fecha2 };
        }
        
        Cursor c = sqlcte.Obtener("Movimientos", null, cláusulaW, argsW, orden);
        
        if (c.moveToFirst())
        {
            // Create the grid item mapping
            String[] from = new String[] { "Fecha", "Importe", "Concepto" };
            int[] to = new int[] { R.id.itemFecha, R.id.itemImporte, R.id.itemConcepto };
            
            // Prepare the list of all records
            List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
            
            // Pasar todos los movimientos
            for (int i = 0; i < c.getCount(); i++)
            {
                String número = c.getString(0);
                
                // Convertir la fecha almacenada en la bd "yyyy-MM-dd"
                // al formato local del sistema
                
                String fecha = Formato.ObtenerFechaLocal(AdminMovimientoActivity.this,
                        c.getString(1));
                
                String cuenta = c.getString(2);
                String banco = c.getString(3);
                String tipo = c.getString(4);
                String concepto = c.getString(5);
                String importe = "$".concat(String.format("%,.2f", c.getFloat(6)));
                
                if (tipo.equals("Retiro"))
                {
                    importe = "(-)".concat(importe);
                }
                
                HashMap<String, String> mapa = new HashMap<String, String>();
                
                mapa.put("Número", número);
                mapa.put("Fecha", fecha);
                mapa.put("Cuenta", cuenta);
                mapa.put("Banco", banco);
                mapa.put("Tipo", tipo);
                mapa.put("Concepto", concepto);
                mapa.put("Importe", importe);
                
                fillMaps.add(mapa);
                
                c.moveToNext();
            }
            
            c.close();
            sqlcte.Cerrar();
            
            // Cargar la lista
            adapter = new SimpleAdapter(this, fillMaps, R.layout.xml_casilla_movimiento, from, to);
            lstMovimientos = (ListView)findViewById(R.id.lstMovimientos);
            lstMovimientos.setAdapter(adapter);
            
            // Registers a context menu to be shown for the given view
            registerForContextMenu(lstMovimientos);
            return true;
        }
        else
        {
            // Actualizr listado de movimientos (sólo cuando se eliminó el último movimiento)
            if (lstMovimientos != null)
            {
                lstMovimientos.setAdapter(null);
            }
            
            if (aviso == null)
            {
                ActualizarAviso(getResources().getString(R.string.noHayMovimientos));
            }
            return false;
        }
    }
    
    private String[] ObtenerFiltroPersonalizado()
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.descFiltro);
        
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View layout = inflater.inflate(R.layout.xml_filtro_personalizado, null);
        builder.setView(layout);
        
        // Preparar los eventos de clic, diálogo de fecha, etc.
        final Button btnFecha1 = (Button)layout.findViewById(R.id.btnFiltroFecha1);
        final Button btnFecha2 = (Button)layout.findViewById(R.id.btnFiltroFecha2);
        
        // Obtener instancia de preferencias
        prefs = getSharedPreferences("clases.microbanco.MicroBanco", MODE_PRIVATE);
        
        // Default button values: previously set dates or current date
        final GregorianCalendar greCal = new GregorianCalendar(año, mes, día);
        final java.text.DateFormat formatoFecha = DateFormat
                .getDateFormat(AdminMovimientoActivity.this);
        String fechaActual = formatoFecha.format(greCal.getTime());
        String fechasAnteriores = prefs.getString(filtroMovimientoPer, "");
        
        if (!("").equals(fechasAnteriores))
        {
            String[] fechas = fechasAnteriores.split(",");
            btnFecha1.setText(fechas[0]);
            btnFecha2.setText(fechas[1]);
        }
        else
        {
            btnFecha1.setText(prefs.getString(filtroMovimientoPer, fechaActual));
            btnFecha2.setText(prefs.getString(filtroMovimientoPer, fechaActual));
        }
        
        // Set up the Click Listener for both buttons
        OnClickListener l = new OnClickListener()
        {
            public void onClick(final View v)
            {
                // Create a new instance of DatePickerDialog & show it // TODO: separar
                // DataSetListener
                DatePickerDialog dpFecha = new DatePickerDialog(AdminMovimientoActivity.this,
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                    int dayOfMonth)
                            {
                                año = year;
                                mes = monthOfYear;
                                día = dayOfMonth;
                                GregorianCalendar cal = new GregorianCalendar(año, mes, día);
                                
                                // Actualizar texto del botón correspondiente
                                if (v.getId() == btnFecha1.getId())
                                    btnFecha1.setText(formatoFecha.format(cal.getTime()));
                                else
                                    btnFecha2.setText(formatoFecha.format(cal.getTime()));
                                
                            }
                        }, año, mes, día);
                dpFecha.show();
            }
        };
        
        btnFecha1.setOnClickListener(l);
        btnFecha2.setOnClickListener(l);
        
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                // Almacenar fechas en SharedPreferences
                String fechas = String.format("%s,%s", btnFecha1.getText(), btnFecha2.getText());
                prefs.edit().putString(filtroMovimientoPer, fechas).commit();
                
                // Aplicar filtro
                MostrarMovimientos();
                
                // Cerrar
                dialog.dismiss();
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
        
        return new String[] { (String)btnFecha1.getText(), (String)btnFecha2.getText() };
    }
    
    private void MostrarListaFiltros()
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.verMovimientos);
        
        // Obtener el filtro seleccionado anteriormente
        prefs = getSharedPreferences("clases.microbanco.MicroBanco", MODE_PRIVATE);
        int filtroElegido = prefs.getInt(filtroMovimiento, 0);
        
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.filtros),
                filtroElegido, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichItem)
                    {
                        // Guardar el filtro seleccionado
                        prefs.edit().putInt(filtroMovimiento, whichItem).commit();
                        Toast.makeText(AdminMovimientoActivity.this,
                                "Filtro ".concat(Integer.toString(whichItem)), Toast.LENGTH_SHORT)
                                .show();
                        if (whichItem < 4)
                        {
                            // Aplicar filtro
                            MostrarMovimientos();
                        }
                        else
                        {
                            // Caso especial: intervalo de fechas
                            ObtenerFiltroPersonalizado();
                        }
                        
                        // Cerrar después de elegir
                        dialog.dismiss();
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
    
    public void onCreateContextMenu(ContextMenu menú, View v, ContextMenuInfo infoMenú)
    {
        if (v.getId() == R.id.lstMovimientos)
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
        
        // Fila seleccionada
        @SuppressWarnings("unchecked")
        HashMap<String, String> rowItem = (HashMap<String, String>)adapter.getItem(info.position);
        String número = rowItem.get("Número");
        String cuenta = rowItem.get("Cuenta");
        String banco = rowItem.get("Banco");
        
        // Eliminar
        if (opciónElegida == 0)
        {
            SqlCliente sqlcte = new SqlCliente(this);
            String cláusulaW = "Número = ?";
            String[] argsW = new String[] { número };
            
            if (sqlcte.Eliminar("Movimientos", cláusulaW, argsW))
            {
                Toast.makeText(AdminMovimientoActivity.this, "Movimiento eliminado con éxito.",
                        Toast.LENGTH_SHORT).show();
                huboCambios = 1;
                
                // Actualizar lista
                MostrarMovimientos();
                
                // Actualizar saldo del encabezado
                TextView lblSaldoCuenta = (TextView)findViewById(R.id.lblCuentaElegidaSaldo);
                cláusulaW = "Número = ? AND Banco = ?";
                argsW = new String[] { cuenta, banco };
                sqlcte = new SqlCliente(AdminMovimientoActivity.this);
                Cursor c = sqlcte.Obtener("Cuentas", new String[] { "Saldo" }, cláusulaW, argsW,
                        null);
                
                if (c.moveToFirst())
                {
                    lblSaldoCuenta.setText("$".concat(String.format("%,.2f", c.getFloat(0))));
                }
            }
            else
            {
                Toast.makeText(AdminMovimientoActivity.this,
                        "Hubo un error al intentar eliminar el movimiento.", Toast.LENGTH_SHORT)
                        .show();
            }
            
        }
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        CreateMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return MenuChoice(item);
    }
    
    // Creates a list of items to show inside a menu
    private void CreateMenu(Menu menu)
    {
        MenuItem mnu0 = menu.add(0, 0, 0, R.string.menúCrear);
        {
            mnu0.setIcon(android.R.drawable.ic_menu_add);
        }
        MenuItem mnu3 = menu.add(0, 3, 3, R.string.menúVer);
        {
            mnu3.setIcon(android.R.drawable.ic_menu_view);
        }
        MenuItem mnu5 = menu.add(0, 5, 5, R.string.acerca);
        {
            mnu5.setIcon(android.R.drawable.ic_menu_info_details);
        }
    }
    
    // Handles the event that is fired when the user selects an item inside the menu.
    private boolean MenuChoice(MenuItem item)
    {
        if (item.getItemId() == 0)
        {
            Intent i = new Intent("clases.microbanco.MovimientoActivity");
            i.putExtra("Número", getIntent().getStringExtra("Número"));
            i.putExtra("Banco", getIntent().getStringExtra("Banco"));
            i.putExtra("Saldo", (String)lblSaldoCuenta.getText());
            startActivityForResult(i, CÓDIGO_CREAR);
        }
        else if (item.getItemId() == 3)
        {
            MostrarListaFiltros();
        }
        else if(item.getItemId() == 5)
        {
            startActivity(new Intent("clases.microbanco.AcercaActivity"));
        }
        return false;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        huboCambios = data.getIntExtra("huboCambios", 0);
        switch (requestCode)
        {
            case CÓDIGO_CREAR:
                if (huboCambios == 1)
                {
                    Toast.makeText(this, "Hubo modificaciones", Toast.LENGTH_SHORT).show();
                    
                    // Actualizar la lista de cuentas
                    MostrarMovimientos();
                    
                    // Actualizar el saldo del encabezado
                    lblSaldoCuenta.setText(data.getStringExtra("Saldo"));
                }
                else
                {
                    Toast.makeText(this, "No modificaciones", Toast.LENGTH_SHORT).show();
                }
                break;
            case CÓDIGO_FILTRO:
                // TODO: leer Intent y aplicar el filtro
                break;
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent i = new Intent();
            i.putExtra("huboCambios", huboCambios);
            setResult(RESULT_OK, i);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
