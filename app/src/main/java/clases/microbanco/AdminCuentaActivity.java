package clases.microbanco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AdminCuentaActivity extends Activity
{
    private ListView lstCuentas;
    private SimpleAdapter adapter;
    private LinearLayout llAviso;
    private TextView aviso;
    
    private static final int CÓDIGO_CREAR = 1;
    private static final int CÓDIGO_VERMOV = 2;
    private static final int CÓDIGO_CREARMOV = 3;
    private static final int CÓDIGO_EDITAR = 4;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admincuenta);
//        Toast.makeText(this, "onCreate()", Toast.LENGTH_SHORT).show();
        
        final LinearLayout llCrearMovimiento = (LinearLayout)findViewById(R.id.admincuentaCabecera);
        
        MostrarCuentas();
        
        llCrearMovimiento.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivityForResult(new Intent("clases.microbanco.CuentaActivity"),
                        CÓDIGO_CREAR);
            }
        });
        
        llCrearMovimiento.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                if (v.getId() == R.id.admincuentaCabecera)
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
//        Toast.makeText(this, "onResume()", Toast.LENGTH_SHORT).show();
    }
    
    private boolean MostrarCuentas()
    {
        // Obtener cuentas
        SqlCliente sqlcte = new SqlCliente(AdminCuentaActivity.this);
        Cursor c = sqlcte.ObtenerTodos("Cuentas", null, null);
        
        if (c.moveToFirst())
        {
            // Create the grid item mapping
            String[] from = new String[] { "Número", "Saldo", "Banco", "Tipo", "SaldoInicial" };
            int[] to = new int[] { R.id.itemNúmero, R.id.itemSaldo, R.id.itemBanco,
                    R.id.itemTipoCuenta, R.id.itemSaldoInicial };
            
            // Prepare the list of all records
            List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
            
            // Pasar todas las cuentas
            for (int i = 0; i < c.getCount(); i++)
            {
                String número = c.getString(0);
                String banco = c.getString(1);
                String tipo = c.getString(2);
                String saldo = "$".concat(String.format("%,.2f", c.getFloat(3)));
                String saldoInicial = "$".concat(String.format("%,.2f", c.getFloat(4)));
                
                HashMap<String, String> mapa = new HashMap<String, String>();
                
                mapa.put("Número", número);
                mapa.put("Banco", banco);
                mapa.put("Tipo", tipo);
                mapa.put("Saldo", saldo);
                mapa.put("SaldoInicial", saldoInicial);
                
                fillMaps.add(mapa);
                
                c.moveToNext();
            }
            
            c.close();
            sqlcte.Cerrar();
            
            // Cargar la lista
            adapter = new SimpleAdapter(this, fillMaps, R.layout.xml_casilla_cuenta, from, to);
            lstCuentas = (ListView)findViewById(R.id.lstCuentas);
            lstCuentas.setAdapter(adapter);
            
            // Registers a context menu to be shown for the given view
            registerForContextMenu(lstCuentas);
            
            // Remover aviso de no cuentas
            if (aviso != null)
            {
                llAviso.removeView(aviso);
            }
            return true;
        }
        else
        {
            // Actualizr listado de cuentas (sólo cuando se eliminó la última cuenta)
            if (lstCuentas != null)
            {
                lstCuentas.setAdapter(null);
            }
            
            if (aviso == null)
            {
                // No existen cuentas. Mostrar aviso.
                aviso = new TextView(this);
                aviso.setText(R.string.noHayCuentas);
                aviso.setTextSize(18);
                aviso.setTypeface(Typeface.DEFAULT_BOLD);
                aviso.setBackgroundColor(Color.parseColor("#808080"));
                aviso.setTextColor(Color.WHITE);
                aviso.setPadding(0, 5, 0, 5);
                // aviso.setGravity(17);
                aviso.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));
                aviso.setGravity(Gravity.CENTER);
                
                llAviso = (LinearLayout)findViewById(R.id.admincuentaSubCabecera);
                llAviso.addView(aviso);
                
            }
            return false;
        }
    }
    
    public void onClick(View v)
    {
        
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case 0:
                
                Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("Mensaje");
                
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Toast.makeText(getBaseContext(), "OK clicked!", Toast.LENGTH_SHORT).show();
                    }
                });
                
                builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Toast.makeText(getBaseContext(), "Cancel clicked!", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                
                return builder.create();
                
        }
        
        return null;
    }
    
    public void onCreateContextMenu(ContextMenu menú, View v, ContextMenuInfo infoMenú)
    {
        if (v.getId() == R.id.lstCuentas)
        {
            menú.setHeaderTitle(R.string.eligaAcción);
            String[] opcionesMenú = getResources().getStringArray(R.array.accionesCuenta);
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
        String banco = rowItem.get("Banco");
        
        // Nuevo movimiento
        if (opciónElegida == 0)
        {
            String saldo = rowItem.get("Saldo");
            Intent i = new Intent("clases.microbanco.MovimientoActivity");
            i.putExtra("Número", número);
            i.putExtra("Banco", banco);
            i.putExtra("Saldo", saldo);
            startActivityForResult(i, CÓDIGO_CREARMOV);
        }
        // Ver movimientos
        else if (opciónElegida == 1)
        {
            String saldo = rowItem.get("Saldo");
            Intent i = new Intent("clases.microbanco.AdminMovimientoActivity");
            i.putExtra("Número", número);
            i.putExtra("Banco", banco);
            i.putExtra("Saldo", saldo);
            startActivityForResult(i, CÓDIGO_VERMOV);
        }
        // Editar
        else if (opciónElegida == 2)
        {
            // Pasar el número y el banco
            Intent i = new Intent("clases.microbanco.CuentaActivity");
            i.putExtra("Número", número);
            i.putExtra("Banco", banco);
            startActivityForResult(i, CÓDIGO_EDITAR);
        }
        // Eliminar
        else if (opciónElegida == 3)
        {
            EliminarCuenta(número, banco);
        }
        return true;
    }
    
    private void EliminarCuenta(final String número, final String banco)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pEliminarCuenta);
        builder.setMessage(R.string.mEliminarCuenta);
        
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                SqlCliente sqlcte = new SqlCliente(AdminCuentaActivity.this);
                String cláusulaW = "Número = ? AND Banco = ?";
                String[] argsW = new String[] { número, banco };
                
                if (sqlcte.Eliminar("Cuentas", cláusulaW, argsW))
                {
                    Toast.makeText(AdminCuentaActivity.this, "Cuenta eliminada con éxito.",
                            Toast.LENGTH_SHORT).show();
                    
                    // Actualizar lista
                    MostrarCuentas();
                    
                    // Eliminar los movimientos asociados
                    cláusulaW = "NúmeroCuenta = ? AND NombreBanco = ?";
                    sqlcte.Eliminar("Movimientos", cláusulaW, argsW);
                }
                else
                {
                    Toast.makeText(AdminCuentaActivity.this,
                            "Hubo un error al intentar eliminar la cuenta.", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                
            }
        });
        
        // Create the AlertDialog object & show it
        builder.create().show();
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
//        MenuItem mnu1 = menu.add(0, 1, 1, R.string.menúEliminar);
//        {
//            mnu1.setIcon(android.R.drawable.ic_menu_delete);
//        }
//        MenuItem mnu2 = menu.add(0, 2, 2, R.string.menúOrdenar);
//        {
//            mnu2.setIcon(android.R.drawable.ic_menu_sort_by_size);
//        }
//        MenuItem mnu3 = menu.add(0, 3, 3, R.string.menúVer);
//        {
//            mnu3.setIcon(android.R.drawable.ic_menu_view);
//        }
//        MenuItem mnu4 = menu.add(0, 4, 4, R.string.menúPrefs);
//        {
//            mnu4.setIcon(android.R.drawable.ic_menu_preferences);
//        }
        MenuItem mnu5 = menu.add(0, 5, 5, R.string.acerca);
        {
            mnu5.setIcon(android.R.drawable.ic_menu_info_details);
        }
    }
    
    // Handles the event that is fired when the user selects an item inside the menu.
    private boolean MenuChoice(MenuItem item)
    {
        // Crear
        if (item.getItemId() == 0)
        {
            startActivityForResult(new Intent("clases.microbanco.CuentaActivity"), CÓDIGO_CREAR);
            return true;
        }
//        // Eliminar
//        else if (item.getItemId() == 1)
//        {
//            
//            return true;
//        }
//        else if (item.getItemId() == 2)
//        {
//            
//            return true;
//        }
        else if(item.getItemId() == 5)
        {
            startActivity(new Intent("clases.microbanco.AcercaActivity"));
        }
        return false;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // Actualizar la lista de cuentas
        MostrarCuentas();
    }
}
