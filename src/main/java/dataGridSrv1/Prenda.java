package dataGridSrv1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean para almacenar los datos de una prenda
 * @author Marcos García Pellitero
 */
public class Prenda implements Serializable {

    private static final long serialVersionUID = -181403229462007401L;

    private String prendaName;
    private List<String> colores;

    public Prenda(String prendaName) {
        this.prendaName = prendaName;
        colores = new ArrayList<String>();
    }

    public void addColor(String color) {
        colores.add(color);
    }

    public void removeColor(String color) {
        colores.remove(color);
    }

    public List<String> getColores() {
        return colores;
    }

    public String getPrendaName() {
        return prendaName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("# Prenda: " + prendaName + " =>");
        b.append("Colores:[");
        for (String color : colores) {
            b.append(" - " + color);
        }
        b.append("]. #");
        return b.toString();
    }
    
    /**
     * método equals propio para Prenda que compara el prendaName
     */
    @Override
    public boolean equals(Object other)
    {
    	System.out.println(DataGridWritter.ID_TRAZA + "Comparando: This.getPrendaName:" + this.getPrendaName() + 
    					   " <> other.getPrendaName:" + ((Prenda) other).getPrendaName());
    	return this.getPrendaName().equals(((Prenda) other).getPrendaName());
    }
}