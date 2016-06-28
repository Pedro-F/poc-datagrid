package dataGridSrv1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration

/**
 * Clase que obtiene datos de la caché JBoss. En caso de que no encuentre el
 * dato, genera uno nuevo y lo pone en la c caché para la siguiente petición
 * 
 * @author pedro.alonso.garcia
 *
 */
public class DataGridWritter {

	
	
	private static final String JDG_HOST = "jdg.host";
	private static final String HOTROD_PORT = "jdg.hotrod.port";
	private static final String PROPERTIES_FILE = "jdg.properties";
	// private static final String JDG_USER = "jdg.user";
	// private static final String JDG_PASSWORD = "jdg.password";
	// private static final String msgTeamMissing = "The specified team \"%s\"
	// does not exist, choose next operation\n";
	// private static final String msgEnterTeamName = "Enter team name: ";
	// private static final String initialPrompt = "Choose action:\n" +
	// "============= \n" + "at - add a team\n"
	// + "ap - add a player to a team\n" + "rt - remove a team\n" + "rp - remove
	// a player from a team\n"
	// + "p - print all teams and players\n" + "q - quit\n";
	private static final String prendasKey = "prendas";

	// private Console con;
	private RemoteCacheManager cacheManager;
	private RemoteCache<String, Object> cache;

	/**
	 *  PUT
	 * @param sParametro1
	 * @return
	 */
	@RequestMapping("/put")
	String putMethod(@RequestParam(value = "parametro1", defaultValue = "1") String sParametro1) {
		// String home(@RequestParam(value="parametro1", defaultValue="1")
		// String sId,
		// @RequestParam(value="parametro2", defaultValue="1") String
		// sParametro2) {
		try {
			// TODO inicializar la Cache ¿es necesario?
			init();
			// Inicializo el valor pasado por parámetro como int
			int iParametro1;
			List<Prenda> prendas;
			try {
				iParametro1 = Integer.parseInt(sParametro1);
			} catch (Exception e) {
				iParametro1 = 1;
			}

			long lTimeBefore, lTimeAfter;

			prendas = (List<Prenda>) cache.get(prendasKey);
			if (prendas ==null)
			{
				prendas = new ArrayList<Prenda>();
				cache.put(prendasKey, prendas);
			}
			
			for (int i = 0; i < iParametro1; i++) {

				Prenda pPrenda = calculoPrenda("" + i);


				if (!prendas.contains(pPrenda)) {
					prendas.add(pPrenda);
				}
				// Pongo la prenda en la caché
				lTimeBefore = System.currentTimeMillis();
				cache.put(pPrenda.getPrendaName(), pPrenda);
				lTimeAfter = System.currentTimeMillis();

				// Traza de prenda enviada a la caché
				System.out.println("Se ha enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos " + "la prenda "
						+ pPrenda.toString());

			}
			
			// Actualizar la lista de prendas en la caché
			lTimeBefore = System.currentTimeMillis();
			cache.put(prendasKey, prendas);
			lTimeAfter = System.currentTimeMillis();

			// Traza de prenda enviada a la caché
			System.out.println("Se ha enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos "
					+ "la lista de prendas " + prendas.toString());

		} catch (Exception e) {
			e.printStackTrace();
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>Finalizado con Error.</br>"
					+ "<br>error:\n " + e.getStackTrace().toString() + "\n\n...</br>";
		}

		return "<br><h1><strong>dataGridSrv1 Método PUT</strong></h1></br>"
				+ "<br>Listo para obtener enviar valores a la caché.</br>" + "<br>Runing...</br>";
	}

	
	
	/**
	 *   Metodo retorna valores en la cache
	 * @param sId
	 * @return
	 */
	@RequestMapping("/get")
	String getMethod(@RequestParam(value = "parametro1", defaultValue = "1") String sId) {

		try {
			init();

			long lTimeBefore, lTimeAfter;

			lTimeBefore = System.currentTimeMillis();
			Prenda pPrenda1 = (Prenda) cache.get(sId);
			List<Prenda> listaPrendas = (List<Prenda>) cache.get(prendasKey);
			
			lTimeAfter = System.currentTimeMillis();
			if (pPrenda1 != null) {
				// Traza de prenda obtenida de la caché
				System.out.println("Se ha obtenido en " + (lTimeAfter - lTimeBefore) + " milisegundos " + "la prenda "
						+ pPrenda1.toString());
			} else {
				pPrenda1 = calculoPrenda(sId);

				// Pongo la prenda en la caché
				lTimeBefore = System.currentTimeMillis();
				cache.put(sId, pPrenda1);
				lTimeAfter = System.currentTimeMillis();

				// Traza de prenda enviada a la caché
				System.out.println("Se ha enviado en " + (lTimeAfter - lTimeBefore) + " milisegundos " + "la prenda "
						+ pPrenda1.toString());

			}
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>"
			+ "<br>Datos de la cache.</br>"
			+ "<br>Prenda: " + pPrenda1.toString() + "</br>"
			+ "<br>Lista Prendas: " + listaPrendas.toString() + "</br>";

		} catch (Exception e) {
			e.printStackTrace();
			return "<br><h1><strong>dataGridSrv1</strong></h1></br>" + "<br>Finalizado con Error.</br>"
					+ "<br>error:\n " + e.getStackTrace().toString() + "\n\n...</br>";
		}

	}

	private void init() throws Exception {

		try {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.addServer().host(jdgProperty(JDG_HOST)).port(Integer.parseInt(jdgProperty(HOTROD_PORT)));
			System.out.println(
					"\n\n\n\n\nConectando a host : " + jdgProperty(JDG_HOST) + ", puerto: " + jdgProperty(HOTROD_PORT));
			cacheManager = new RemoteCacheManager(builder.build());
			System.out.println("cacheManager : " + cacheManager.toString());
			cache = cacheManager.getCache("prendas");
			System.out.println("Obtenida Cache");

			// TODO Inicializo la caché con datos ===> ¿es necesario?
			if (!cache.containsKey(prendasKey)) {
				List<String> prendas = new ArrayList<String>();
				Prenda p = new Prenda("Pantalón");
				p.addColor("Blanco");
				p.addColor("Negro");
				p.addColor("Azul");
				cache.put(p.getPrendaName(), p);
				prendas.add(p.getPrendaName());
				cache.put(prendasKey, prendas);
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
			Thread.sleep(100);
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
