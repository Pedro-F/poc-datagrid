package dataGridSrv1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
	private RemoteCache<String, Object> cache;

	@RequestMapping("/")
	String homeMethod() {

		return "<br><h1><strong>dataGridSrv1</strong></h1></br>"
				+ "<br>Poner prendas en la caché: http://datagrid-srv1.accenture.cloud/put?parametro1=</h1></br>"
				+ "<br>Sacar prendas de la caché: http://datagrid-srv1.accenture.cloud/get?parametro1=</h1></br>";
	}

	/**
	 * Método que inserta valores en la caché
	 * 
	 * @param sParametro1
	 * @return
	 */
	@RequestMapping("/put")
	String putMethod(@RequestParam(value = "parametro1", defaultValue = "1") String sParametro1) {

		// variables
		int iParametro1;
		HashMap<String, Prenda> prendasMap;
		long lTimeBefore, lTimeAfter;
		String trazaPrendas = "";

		try {
			// Inicializamos la conexión al Datagrid
			init();

			// Inicializo el valor pasado por parámetro como int
			try {
				iParametro1 = Integer.parseInt(sParametro1);
			} catch (Exception e) {
				iParametro1 = 1;
			}

			prendasMap = (HashMap<String, Prenda>) cache.get(PRENDAS_KEY);

			// Si el HashMap no existe, ceamos uno nuevo
			if (prendasMap == null)
				prendasMap = new HashMap<String, Prenda>();

			// Generamos las prendas con el id secuencial del indice del for
			for (int i = 0; i < iParametro1; i++) {

				Prenda pPrenda = calculoPrenda("" + i);

				// Si ya existe, no la inserto
				if (cache.get(pPrenda.getPrendaName()) != null) {
					System.out.println(ID_TRAZA + "el elemento: " + i + " ya se encuentra en la caché");
					trazaPrendas += "la prenda: " + i + " ya se encuentra en la caché";
				}
				// Genero la prenda y la pongo en la caché
				else {

					prendasMap.put(pPrenda.getPrendaName(), pPrenda);

					// Pongo la prenda en la caché
					lTimeBefore = System.currentTimeMillis();
					cache.put(pPrenda.getPrendaName(), pPrenda);
					lTimeAfter = System.currentTimeMillis();

					// Traza de prenda enviada a la caché
					System.out.println(ID_TRAZA + "Se ha enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos "
							+ "la prenda " + pPrenda.toString());
					trazaPrendas += "<br>Se ha enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos "
							+ "la prenda " + pPrenda.toString() + "</br>";
				}

			}

			// Pongo el HashMap de prendas en la caché
			lTimeBefore = System.currentTimeMillis();
			cache.put(PRENDAS_KEY, prendasMap);
			lTimeAfter = System.currentTimeMillis();

			// Traza de prenda enviada a la caché
			System.out.println(ID_TRAZA + "Se ha enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos "
					+ "el mapa de prendas " + prendasMap.toString());

		} catch (Exception e) {
			e.printStackTrace();
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>PUT Finalizado con Error.</br>"
					+ "<br>error:\n " + e.getStackTrace().toString() + "\n\n...</br>";
		}

		return "<br><h1><strong>dataGridSrv1 Método PUT</strong></h1></br>" + trazaPrendas
				+ "<br>Se ha enviado la lista en " + (lTimeAfter - lTimeBefore) + " milisegundos </br>"
				+ "<br>La lista de prendas es  " + prendasMap.toString()
				+ "<br>Listo para enviar valores a la caché....</br>";
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
		String trazaPrendas = "";
		Prenda pPrenda;
		
		try {
			// Inicializamos la conexión al Datagrid
			init();
			// Obtenemos el HashMap de prendas
			lTimeBefore = System.currentTimeMillis();
			prendasMap = (HashMap<String, Prenda>) cache.get(PRENDAS_KEY);
			lTimeAfter = System.currentTimeMillis();
			
			trazaPrendas += "<br>Se ha recuperado el HashMap de prendas en " + (lTimeAfter - lTimeBefore)
					+ " milisegundos </br>";

			if (cache.get(sId) != null) {
				lTimeBefore = System.currentTimeMillis();
				pPrenda = (Prenda) cache.get(sId);
				lTimeAfter = System.currentTimeMillis();
				trazaPrendas += "<br>Se ha recuperado en " + (lTimeAfter - lTimeBefore) + " milisegundos "
						+ "la prenda " + pPrenda.toString() + "</br>";
				// Traza de prenda obtenida de la caché
				System.out.println(ID_TRAZA + "Se ha obtenido en " + (lTimeAfter - lTimeBefore) + " milisegundos "
						+ "la prenda " + pPrenda.toString());
			} else {
				trazaPrendas += "<br>La prenda " + sId + " no existe, se calculará e insertará en la caché</br>";
				lTimeBefore = System.currentTimeMillis();
				pPrenda = calculoPrenda(sId);
				// Pongo la prenda en la caché
				cache.put(sId, pPrenda);
				lTimeAfter = System.currentTimeMillis();
				trazaPrendas += "<br>Se ha calculado y enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos "
						+ "la prenda " + pPrenda.toString() + "</br>";

				// Traza de prenda calculada y enviada de la caché
				System.out.println(ID_TRAZA + "Se ha calculado y enviado en " + (lTimeAfter - lTimeBefore)
						+ " milisegundos " + "la prenda " + pPrenda.toString());

				// Actualizamos el HashMap de prendas y lo subimos a la
				// caché
				
				prendasMap.put(sId, pPrenda);
				lTimeBefore = System.currentTimeMillis();
				cache.put(PRENDAS_KEY, prendasMap);
				lTimeAfter = System.currentTimeMillis();

				// Traza de prenda calculada y enviada de la caché
				System.out.println(ID_TRAZA + "Se ha calculado y enviado en " + (lTimeAfter - lTimeBefore)
						+ " milisegundos " + "la prenda " + pPrenda.toString());
			}
			
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>Datos de la cache.</br>" + trazaPrendas
					+ "<br>Prenda: " + pPrenda.toString() + "</br>" + "<br>Lista Prendas: " + prendasMap.toString()
					+ "</br>";

		} catch (Exception e) {
			e.printStackTrace();
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>GET Finalizado con Error.</br>"
					+ "<br>error:\n " + e.getStackTrace().toString() + "\n\n...</br>";
		}

	}

	private void init() throws Exception {

		try {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.addServer().host(jdgProperty(JDG_HOST)).port(Integer.parseInt(jdgProperty(HOTROD_PORT)));
			System.out.println(
					"###===>>> Conectando a host : " + jdgProperty(JDG_HOST) + ", puerto: " + jdgProperty(HOTROD_PORT));
			cacheManager = new RemoteCacheManager(builder.build());
			cache = cacheManager.getCache("prendas");

			// Inicializo la caché con el mapa de prendas
			if (!cache.containsKey(PRENDAS_KEY)) {
				Map<String, Prenda> prendasMap = new HashMap<String, Prenda>();
				cache.put(PRENDAS_KEY, prendasMap);
			}
		} catch (Exception e) {
			System.out.println("Init Caught: " + e);
			e.printStackTrace();
			throw e;

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
