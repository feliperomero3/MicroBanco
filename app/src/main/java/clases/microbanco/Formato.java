package clases.microbanco;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import android.content.Context;
import android.text.format.DateFormat;

public class Formato
{
    // Convertir la fecha almacenada en la bd "yyyy-MM-dd"
    // al formato local del sistema
    protected static String ObtenerFechaLocal(Context contexto, String fecha)
    {
        // Obtener año, mes y día de la cadena
        String[] datos = fecha.split("-");
        int año = Integer.parseInt(datos[0]);
        int mes = Integer.parseInt(datos[1]) - 1;
        int día = Integer.parseInt(datos[2]);
        
        // Construir calendario con los datos del parámetro fecha
        GregorianCalendar greCal = new GregorianCalendar(año, mes, día);
        
        // Objeto que aplicará el formato de fecha local del sistema
        java.text.DateFormat formatoFecha = DateFormat.getDateFormat(contexto);
        
        // Actualizar la cadena obtenida de la bd
        return formatoFecha.format(greCal.getTime());
    }
    
    // Convertir la fecha con formato actual del sistema
    // al formato requerido por sqlite
    // @contexto: el contexto de la aplicación
    // @fecha: cadena con la fecha en el formato del sistema
    protected static String ObtenerFechaSqlite(Context contexto, String fecha)
    {
        // Objeto que obtendrá el formato de fecha local del sistema
        java.text.DateFormat formatoFecha = DateFormat.getDateFormat(contexto);
        
        Calendar cal = Calendar.getInstance();
        try
        {
            // Construir calendario con los datos del parámetro fecha
            Date dt = formatoFecha.parse(fecha); // Convertir la cadena en objeto Date
            cal.setTime(dt);
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (String)DateFormat.format("yyyy-MM-dd", cal);
    }
    
    // Convertir la fecha con formato "dd/MM/yyyy"
    // al formato requerido por sqlite
    // TODO: generalizar para que acepte cualquier formato de entrada
    protected static String ObtenerFechaSqlite(String fecha)
    {
        // Obtener año, mes y día de la cadena
        String[] datos = fecha.split("/");
        int día = Integer.parseInt(datos[0]);
        int mes = Integer.parseInt(datos[1]) - 1;
        int año = Integer.parseInt(datos[2]);
        
        // Construir calendario con los datos del parámetro fecha
        GregorianCalendar greCal = new GregorianCalendar(año, mes, día);
        
        return (String)DateFormat.format("yyyy-MM-dd", greCal);
    }
}
