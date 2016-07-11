package dataGridSrv1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase que pone y obtiene datos de la caché JBoss Datagrid 6.5.
 * 
 * @author pedro.alonso.garcia
 *
 */
@RestController
@EnableAutoConfiguration
public class DataGridWritter {

	// Constantes de la clase
	private static final String JDG_HOST = "jdg.host";
	private static final String HOTROD_PORT = "jdg.hotrod.port";
	private static final String PROPERTIES_FILE = "jdg.properties";
	private static final String PRENDAS_KEY = "prendas";
	public static final String ID_TRAZA = "###===>>>";

	// Variables globales
	private RemoteCacheManager cacheManager;
	private RemoteCache<String, Object> cache = null;
	private final static DatagridListener listener = new DatagridListener();

	@RequestMapping("/")
	String homeMethod() {

		return "<br><h1><strong>dataGridSrv1</strong></h1></br>"
				+ "<br>Poner prendas en la caché: datagrid-srv1.accenture.cloud/put?parametro1=&parametro2=</h1></br>"
				+ "<br>Sacar prendas de la caché: datagrid-srv1.accenture.cloud/get?parametro1=</h1></br>";
	}

	/**
	 * Método que inserta valores en la caché
	 * 
	 * @param sParametro1
	 * @return
	 */
	@RequestMapping("/put")
	String putMethod(@RequestParam(value = "parametro1", defaultValue = "1") String sParametro1,
					 @RequestParam(value = "parametro2", defaultValue = "1") String sParametro2) {

		// variables
		int iParametro1,iParametro2;
		HashMap<String, Prenda> prendasMap;
		long lTimeBefore, lTimeAfter;
		long timeGetCacheData = 0;
		long timePutCacheData = 0;
		long timeGetCacheListas = 0;
		long contadorPuts = 0;

		try {
			// Inicializamos la conexión al Datagrid
			init();
			
			// Inicializo los valores pasados por parámetro como int
			try {
				iParametro1 = Integer.parseInt(sParametro1);
			} catch (Exception e) {
				iParametro1 = 1;
			}
			try {
				iParametro2 = Integer.parseInt(sParametro2);
			} catch (Exception e) {
				iParametro2 = 1;
			}
			lTimeBefore = System.currentTimeMillis();
			prendasMap = (HashMap<String, Prenda>) cache.get(PRENDAS_KEY);
			lTimeAfter = System.currentTimeMillis();
			timeGetCacheListas =  lTimeAfter - lTimeBefore;
			
			// Si el HashMap no existe, ceamos uno nuevo
			if (prendasMap == null)
				prendasMap = new HashMap<String, Prenda>();

			// Generamos las prendas con el id secuencial del indice del for
			for (int i = 0; i < iParametro1; i++) {
				int idPrendaTratar = iParametro2 + i;
				// Si ya existe, no la inserto
				lTimeBefore = System.currentTimeMillis();
				Prenda prendaEnCache = (Prenda) cache.get(String.valueOf(idPrendaTratar));
				lTimeAfter = System.currentTimeMillis();
				// acumulo el tiempo del get 
				timeGetCacheData += (lTimeAfter - lTimeBefore);
				
				if (cache.get(String.valueOf(idPrendaTratar)) == null) {

					Prenda pPrenda = calculoPrenda(String.valueOf(idPrendaTratar));
					
					prendasMap.put(pPrenda.getPrendaName(), pPrenda);

					// Pongo la prenda en la caché
					lTimeBefore = System.currentTimeMillis();
					cache.put(pPrenda.getPrendaName(), pPrenda);
					lTimeAfter = System.currentTimeMillis();
					
					// acumulo el tiempo del put y contabilizo la insercion
					timePutCacheData += (lTimeAfter - lTimeBefore);
					contadorPuts++;
				}
			}

			if (contadorPuts > 0){
				// Pongo el HashMap de prendas en la caché
				lTimeBefore = System.currentTimeMillis();
				cache.put(PRENDAS_KEY, prendasMap);
				lTimeAfter = System.currentTimeMillis();
			}
			
			// Traza 
			System.out.println(ID_TRAZA + "GET del HashMap en " + timeGetCacheListas + " milisegundos");
			System.out.println(ID_TRAZA + "Media de GET de prenda " + (timeGetCacheData/iParametro1) + " milisegundos (" + sParametro1 + " GETs realizados en " + timeGetCacheData + " milisegundos)");
			if (contadorPuts > 0){
				System.out.println(ID_TRAZA + "Media de PUT de prenda " + (timePutCacheData/contadorPuts) + " milisegundos (" + contadorPuts + " PUTs realizados en " + timePutCacheData + " milisegundos)");
				System.out.println(ID_TRAZA + "PUT del HashMap en " + (lTimeAfter - lTimeBefore) + " milisegundos");
			}
			else{
				System.out.println(ID_TRAZA + "Se produjeron 0 inserciones y no se modificará el HashMap");
			}
			System.out.println(ID_TRAZA + "La lista de prendas tiene  " + prendasMap.size());

		} catch (Exception e) {
			e.printStackTrace();
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>PUT Finalizado con Error.</br>"
					+ "<br>error:\n " + e.getStackTrace().toString() + "\n\n...</br>";
		}

		
		String sRetorno = "<br><h1><strong>dataGridSrv1 Método PUT</strong></h1></br>";
		sRetorno       += "<br>GET del HashMap en " + timeGetCacheListas + " milisegundos</br>";
		sRetorno       += "<br>Media de GET de prenda " + (timeGetCacheData/iParametro1) + " milisegundos (" + sParametro1 + " GETs realizados en " + timeGetCacheData + " milisegundos)</br>";
		if (contadorPuts > 0){
			sRetorno += "<br>Media de PUT de prenda " + (timePutCacheData/contadorPuts) + " milisegundos (" + contadorPuts + " PUTs realizados en " + timePutCacheData + " milisegundos)</br>";
			sRetorno       += "<br>PUT del HashMap en " + (lTimeAfter - lTimeBefore) + " milisegundos</br>";
	    }
		else{
			sRetorno       += "<br>Se produjeron 0 inserciones y no se modificará el HashMap</br>";
		}
		sRetorno       += "<br>La lista de prendas tiene  " + prendasMap.size()+ "</br>";
		return sRetorno;
	}

	/**
	 * Metodo retorna valores de la cache
	 * 
	 * @param sId
	 * @return
	 */
	@RequestMapping("/get")
	String getMethod(@RequestParam(value = "parametro1", defaultValue = "1") String sId) {

		// variables
		HashMap<String, Prenda> prendasMap;
		long lTimeBefore, lTimeAfter;
		Prenda pPrenda;
		long timeGetCacheData = 0;
		long timePutCacheData = 0;
		long timeGetCacheListas = 0;
		long contadorPuts = 0;
		boolean bSoloGet = false;
		
		try {
			// Inicializamos la conexión al Datagrid
			init();

			// Obtenemos el HashMap de prendas
			lTimeBefore = System.currentTimeMillis();
			prendasMap = (HashMap<String, Prenda>) cache.get(PRENDAS_KEY);
			lTimeAfter = System.currentTimeMillis();
			timeGetCacheListas =  lTimeAfter - lTimeBefore;
			
			if (cache.get(sId) != null) {
				
				lTimeBefore = System.currentTimeMillis();
				pPrenda = (Prenda) cache.get(sId);
				lTimeAfter = System.currentTimeMillis();
				// acumulo el tiempo del get 
				timeGetCacheData += (lTimeAfter - lTimeBefore);
				
				// Traza 
				System.out.println(ID_TRAZA + "GET del HashMap en " + timeGetCacheListas + " milisegundos");
				System.out.println(ID_TRAZA + "Media de GET de prenda " + timeGetCacheData + " milisegundos");
				bSoloGet = true;
				
			} else {
				
				pPrenda = calculoPrenda(sId);
				// Pongo la prenda en la caché
				lTimeBefore = System.currentTimeMillis();
				//cache.put(sId, pPrenda);
				// Ponemos lifespan y max idle
				cache.put(sId, pPrenda, 2, TimeUnit.MINUTES, 1, TimeUnit.MINUTES);
				
				lTimeAfter = System.currentTimeMillis();
				// acumulo el tiempo del put y contabilizo la insercion
				timePutCacheData += (lTimeAfter - lTimeBefore);
				
				// Actualizamos el HashMap de prendas y lo subimos a la
				// caché
				prendasMap.put(sId, pPrenda);
				lTimeBefore = System.currentTimeMillis();
				cache.put(PRENDAS_KEY, prendasMap);
				lTimeAfter = System.currentTimeMillis();

				
				// Traza 
				System.out.println(ID_TRAZA + "GET del HashMap en " + timeGetCacheListas + " milisegundos");
				System.out.println(ID_TRAZA + "GET de prenda " + timeGetCacheData + " milisegundos");
				System.out.println(ID_TRAZA + "PUT de prenda " + timePutCacheData + " milisegundos");
				System.out.println(ID_TRAZA + "PUT del HashMap en " + (lTimeAfter - lTimeBefore) + " milisegundos");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>GET Finalizado con Error.</br>"
					+ "<br>error:\n " + e.getStackTrace().toString() + "\n\n...</br>";
		}
		
		String sRetorno = "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>Datos de la cache.</br>";
		sRetorno += "<br>GET del HashMap en " + (lTimeAfter - lTimeBefore) + " milisegundos</br>";
		sRetorno += "<br>GET de prenda " + (lTimeAfter - lTimeBefore) + " milisegundos</br>";
		if (!bSoloGet){sRetorno += "<br>PUT de prenda " + (lTimeAfter - lTimeBefore) + " milisegundos</br>";} 
		if (!bSoloGet){sRetorno += "<br>PUT del HashMap en " + (lTimeAfter - lTimeBefore) + " milisegundos</br>";} 
		sRetorno += "<br>La lista de prendas tiene  " + prendasMap.size();
		
		return sRetorno;
	}

	private void init() throws Exception {
		if (cache==null){
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.addServer().host(jdgProperty(JDG_HOST)).port(Integer.parseInt(jdgProperty(HOTROD_PORT)));
				System.out.println(
						"###===>>> Conectando a host : " + jdgProperty(JDG_HOST) + ", puerto: " + jdgProperty(HOTROD_PORT));
				cacheManager = new RemoteCacheManager(builder.build());
				cache = cacheManager.getCache("prendas");
				
				//Añadimos el listener
				
				cache.addClientListener(listener);
				
				// Inicializo la caché con el mapa de prendas
				if (!cache.containsKey(PRENDAS_KEY)) {
					Map<String, Prenda> prendasMap = new HashMap<String, Prenda>();
					cache.put(PRENDAS_KEY, prendasMap);
				}
			}
			catch (Exception e) {
				System.out.println("Init Caught: " + e);
				e.printStackTrace();
				throw e;
	
			}
		}
	}

	/**
	 * Método que calcula una prenda que no está en la caché. Tiene un sleep de
	 * 100 milisegundos para simular un retardo en la petición a sistema
	 * persistente
	 * 
	 * @param sNombrePrenda
	 * @return
	 * 
	 */
	private Prenda calculoPrenda(String sNombrePrenda) {
		// Creo la prenda
		Prenda pNuevaPrenda = new Prenda(sNombrePrenda);
		// Pongo colores por defecto
		pNuevaPrenda.addColor("Negro");
		pNuevaPrenda.addColor("Blanco");
		pNuevaPrenda.addColor("Azul");

		// Retardo de 100 Milisegundos
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return pNuevaPrenda;
	}

	/**
	 * Método que obtiene una propiedad del fichero
	 * src\main\resources\jdg.properties
	 * 
	 * @param name
	 * @return
	 * 
	 */
	public static String jdgProperty(String name) {
		Properties props = new Properties();
		try {
			props.load(DataGridWritter.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return props.getProperty(name);
	}

	/*******************************************
	 * MAIN *
	 * 
	 * @param args
	 *            *
	 * @throws Exception
	 *             *
	 ******************************************/
	public static void main(String[] args) throws Exception {
		SpringApplication.run(DataGridWritter.class, args);
	}

}
