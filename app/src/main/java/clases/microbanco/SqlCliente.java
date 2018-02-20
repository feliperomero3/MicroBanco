package clases.microbanco;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqlCliente
{
    private static final String NOMBRE = "Banco";
    private static final String ETIQUETA = "SqlCliente";
    private static final int VERSIÓN = 7;

    private static final String ddlUsuarios = 
            "CREATE TABLE Usuarios(" +
            "   Contraseña TEXT PRIMARY KEY" +
            ")";
    
    private static final String ddlCuentas = 
            "CREATE TABLE Cuentas(" +
            "   Número TEXT," +
            "   Banco TEXT, " +
            "   Tipo TEXT, " +
            "   Saldo REAL," +
            "   SaldoInicial REAL," +
            "   Posición INT null," +
            "   PRIMARY KEY(Número, Banco)" +
            ")";
    
    private static final String ddlMovimientos = 
            "CREATE TABLE Movimientos(" +
            "   Número INTEGER PRIMARY KEY AUTOINCREMENT," +
            "   Fecha TEXT," +
            "   NúmeroCuenta TEXT," +
            "   NombreBanco TEXT," +
            "   Tipo TEXT," +
            "   Concepto TEXT null," +
            "   Importe REAL," +
            "   FOREIGN KEY(NúmeroCuenta, NombreBanco) REFERENCES Cuentas(Número, Banco)" +
            ")";
    
    // -- Actualizar el saldo de la cuenta en el caso de un retiro o un depósito
    private static final String ddlTrigger1 =
            "CREATE TRIGGER ActualizarSaldo AFTER INSERT ON Movimientos " + 
            "BEGIN" + 
            "    UPDATE Cuentas" + 
            "    SET Saldo = (" + 
            "        CASE" + 
            "            WHEN NEW.Tipo == 'Depósito' THEN  Saldo +  NEW.Importe" + 
            "            WHEN NEW.Tipo == 'Retiro' THEN Saldo - NEW.Importe" + 
            "        END " + 
            "    )" + 
            "    WHERE Número = NEW.NúmeroCuenta; " + 
            "END;";

    // -- Restaurar el saldo de  la cuenta cuando se elimine un movimiento
    private static final String ddlTrigger2 =
            "CREATE TRIGGER RestaurarSaldo AFTER DELETE ON Movimientos " + 
            "BEGIN" + 
            "    UPDATE Cuentas" + 
            "    SET Saldo = (" + 
            "        CASE" + 
            "            WHEN OLD.Tipo == 'Depósito' THEN  Saldo -  OLD.Importe" + 
            "            WHEN OLD.Tipo == 'Retiro' THEN Saldo + OLD.Importe" + 
            "        END " + 
            "    )" + 
            "    WHERE Número = OLD.NúmeroCuenta; " + 
            "END;";
    
    // Catálogos (a falta de no poder guardar arreglos en SharedPreference
    // ni encontrar otra manera de persistir arreglos.
    // No los relaciono con el resto de las tablas porque busco ofrecer
    // la posibilidad de eliminar a discreción.
    private static final String ddlBancos =
            "CREATE TABLE Bancos ( " +
            "   Nombre TEXT PRIMARY KEY" +
            ")";
    
    private static final String ddlTiposCuenta =
            "CREATE TABLE TiposCuenta ( " +
            "   Nombre TEXT PRIMARY KEY" +
            ")";
            
    private static final String ddlConceptos=
            "CREATE TABLE Conceptos ( " +
            "   Nombre TEXT PRIMARY KEY" +
            ")";

    private final Context contexto;

    private AuxiliarBD auxiliar;
    private SQLiteDatabase bd;

    public SqlCliente(Context ctx)
    {
        this.contexto = ctx;
        auxiliar = new AuxiliarBD(contexto);
    }

    // una Clase como miembro estático de otra Clase [Tipos Anidados ó 'Nested Types']
    private static class AuxiliarBD extends SQLiteOpenHelper
    {
        AuxiliarBD(Context contexto)
        {
            super(contexto, NOMBRE, null, VERSIÓN);
        }

        @Override
        public void onCreate(SQLiteDatabase bd)
        {
            try
            {
                bd.execSQL(ddlUsuarios);
                bd.execSQL(ddlCuentas);
                bd.execSQL(ddlMovimientos);
                bd.execSQL(ddlTrigger1);
                bd.execSQL(ddlTrigger2);
                bd.execSQL(ddlBancos);
                bd.execSQL(ddlTiposCuenta);
                bd.execSQL(ddlConceptos);

                bd.execSQL("INSERT INTO Bancos VALUES ('Banamex')");
                bd.execSQL("INSERT INTO Bancos VALUES ('Bancomer')");
                bd.execSQL("INSERT INTO Bancos VALUES ('Banorte')");
                bd.execSQL("INSERT INTO Bancos VALUES ('HSBC')");
                bd.execSQL("INSERT INTO Bancos VALUES ('Santander')");
                bd.execSQL("INSERT INTO Bancos VALUES ('Scotiabank')");
                bd.execSQL("INSERT INTO TiposCuenta VALUES ('Ahorro')");
                bd.execSQL("INSERT INTO TiposCuenta VALUES ('Cheques')");
                bd.execSQL("INSERT INTO TiposCuenta VALUES ('Inversión')");
                bd.execSQL("INSERT INTO Conceptos VALUES ('Sin concepto')");
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase bd, int versiónAnterior, int versiónPosterior)
        {
            Log.w(ETIQUETA, "Actualizando la base de datos de la versión " + versiónAnterior
                    + "a la versión " + versiónPosterior + " lo que destruirá los datos.");
            bd.execSQL("DROP TABLE IF EXISTS Usuarios");
            bd.execSQL("DROP TABLE IF EXISTS Cuentas");
            bd.execSQL("DROP TABLE IF EXISTS Movimientos");
            bd.execSQL("DROP TABLE IF EXISTS Bancos");
            bd.execSQL("DROP TABLE IF EXISTS TiposCuenta");
            bd.execSQL("DROP TABLE IF EXISTS Conceptos");
            bd.execSQL("DROP TRIGGER IF EXISTS ActualizarSaldo");
            bd.execSQL("DROP TRIGGER IF EXISTS RestaurarSaldo");
            onCreate(bd);
        }
    }

    // Abrir la base de datos
    public void Abrir()
    {
        try
        {
            bd = auxiliar.getWritableDatabase();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    // Cerrar la base de datos
    public void Cerrar()
    {
        auxiliar.close();
        bd.close();
    }

    public boolean Buscar(String tabla, String cláusulaW, String[] argsW)
    {
        try
        {
            Abrir();
            Cursor c = bd.query(tabla, null, cláusulaW, argsW, null, null, null);
            return c.moveToFirst();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return false;
        }
        finally
        {
            Cerrar();
        }
    }

    public int Contar(String tabla)
    {
        try
        {
            Abrir();
            return bd.query(tabla, null, null, null, null, null, null).getCount();
        }

        catch (SQLException ex)
        {
            ex.printStackTrace();
            return -1;
        }
        finally
        {
            Cerrar();
        }
    }

    // Insertar un registro
    public long Insertar(String tabla, ContentValues valores)
    {
        try
        {
            Abrir();
            return bd.insert(tabla, null, valores);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return -1;
        }
        finally
        {
            Cerrar();
        }
    }

    // Eliminar un registro en particular
    public boolean Eliminar(String tabla, String cláusulaW, String[] argsW)
    {
        try
        {
            Abrir();
            return bd.delete(tabla, cláusulaW, argsW) > 0;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return false;
        }
        finally
        {
            Cerrar();
        }
    }

    // Obtiene todos los registros, columnas y orden es opcional
    public Cursor ObtenerTodos(String tabla, String[] columnas, String orden)
    {
        try
        {
            Abrir();
                return bd.query(tabla, columnas, null, null, null, null, orden);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return null;
        }
        finally
        {
            //Cerrar(); // Cerar el cursor en el contexto donde se termine de utilizar
        }
    }

    // Obtiene ciertos registros
    public Cursor Obtener(String tabla, String[] columnas, String cláusulaW, String[] argsW, 
            String orden)
    {
        try
        {
            Abrir();
            return bd.query(tabla, columnas, cláusulaW, argsW, null, null, orden);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return null;
        }
        finally
        {
            //Cerrar(); // Cerrar el cursor en el contexto donde se termine de utilizar
        }
    }

    // Actualiza un registro (update)
    public boolean Actualizar(String tabla, ContentValues valores, String cláusulaW, String[] argsW)
    {
        try
        {
            Abrir();
            return bd.update(tabla, valores, cláusulaW, argsW) > 0;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return false;
        }
        finally
        {
            Cerrar();
        }
    }
}
