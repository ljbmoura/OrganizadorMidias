package ljbm.com.br.OrganizadorArquivo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ArvoreArquivos {

	private static final Logger LOG = Logger.getLogger(ArvoreArquivos.class);

	static void usage() {
		LOG.error("<dirOrigem> <glob_pattern> <dirDestino>");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException {
		
//		String[] argsAux = {"G:\\a\\copiasNuvem","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"G:\\a\\copiasNuvem","*.mp4", "H:\\degoo.luca\\videos"};
//		String[] argsAux = {"G:\\a - pre isadora\\fotos\\ace","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"G:\\a - pre isadora\\fotos\\ace","*.mp4", "H:\\degoo.luca\\videos"};
//		String[] argsAux = {"G:\\a - pre isadora\\fotos\\camera sony luc","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"G:\\a - pre isadora\\fotos\\N8","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"G:\\a - pre isadora\\fotos\\Album Databook","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"G:\\aOrganizadorMidias\\fotos","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"H:\\fotos e videos revisados\\s417-24nov","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"H:\\fotos e videos revisados\\s417-24nov","*.mp4", "H:\\degoo.luca\\videos"};
//		String[] argsAux = {"H:\\fotos e videos revisados\\s416nov2016","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"H:\\fotos e videos revisados\\s416nov2016","*.mp4", "H:\\degoo.luca\\videos"};
		
//		String[] argsAux = {"H:\\fotos e videos revisados\\xperia16nov2016\\100ANDRO","*.jpg", "H:\\degoo\\fotos"};
//		String[] argsAux = {"H:\\fotos e videos revisados\\xperia16nov2016\\100ANDRO","*.mp4", "H:\\degoo.luca\\videos"};
		
		Map <String,  String> tipoDest = new HashMap<String,  String>(0);
		tipoDest.put("jpg", "fotos");
		tipoDest.put("mp4", "videos");
		
//		String[] argsAux = {"G:\\aOrganizadorMidias\\" +tipoDest.get("jpg"),"*.jpg", "H:\\aOrganizadorMidias\\"+tipoDest.get("jpg")};
		
//		String[] argsAux = {"H:\\fotos e videos revisados\\" +tipoDest.get("jpg"),"*.jpg", "G:\\aMidiasOrganizadas\\"+tipoDest.get("jpg")};
//		String[] argsAux = {"H:\\fotos e videos revisados\\","*.mp4", "G:\\aMidiasOrganizadas\\"+tipoDest.get("mp4")};
		
		String[] argsAux = {"G:\\aOrganizadorMidias\\fotosC","*.jpg", "G:\\aMidiasOrganizadas\\"+tipoDest.get("jpg")};
		
		
		args = argsAux;
		
		if (args.length < 3) {//|| !args[1].equals("-name"))
			usage();
		}
		Path dirOrigem = Paths.get(args[0]);
		String pattern = args[1];
		Path dirDestino = Paths.get(args[2]);
		Path dirDestinoBck = Paths.get(args[2] + ".dup");

		LOG.info(String.format("Origem '%s', filtro '%s', destino '%s'",
				args[0], args[1], args[2]));
		LOG.info(String.format("Destino Duplicados '%s'", args[2] + ".dup"));
		Finder catalogador = new Finder(pattern);
		Files.walkFileTree(dirOrigem, catalogador);
		catalogador.done();
		Map<String, Path> catalogo = catalogador.getMapa();

//	    organizaPorAnoMes(catalogo, dirDestino.toFile(), dirDestinoBck.toFile());
		
		File[] pastasDestino = new File[1];
		//pastasDestino[0] = Paths.get("H://aOrganizadorMidias//videos").toFile();
		//pastasDestino[1] = Paths.get("H://aOrganizadorMidias//hfotos").toFile();
		pastasDestino[0] = dirDestino.toFile();
		verificaSincronizacao(catalogo, pastasDestino);
		
		/*
		 * Finder novo = new Finder(pattern); Files.walkFileTree(dirABuscar,
		 * novo); novo.done(); HashMap<String, String> mapaABuscar =
		 * novo.getMapa();
		 * 
		 * int i = 0; for (String chave : mapaABuscar.keySet()) { if
		 * (mapaOndeBuscar.containsKey(chave)) { LOG.info(chave + " - " +
		 * mapaABuscar.get(chave)); i++; } } LOG.info(i);
		 */
		System.exit(0);
	}

	/**
	 * @param catalogo
	 * @param pastaDestino
	 * @param pastaDestinoBck 
	 * @throws IOException
	 */
	private static void organizaPorAnoMes(Map<String, Path> catalogo,
			File pastaDestino, File pastaDestinoBck) throws IOException {	

		// padrão nokia: 2015-06-07-1105
		// padrão s4: 20150603_203140 ou 20150428_161032_Richtone(HDR) ou
		// 20150429_211018_LLS
		// padrão xperia: DSC_2099
		// ace: 2014-08-29 18.46.44
		// camera sony: DSC05071

		Pattern padraoN8 = Pattern
				.compile("(20)\\d\\d[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01])[-]\\d*");
		Pattern padraoS4 = Pattern
				.compile("(20)\\d\\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])_\\d{6}.*");
		Pattern padraoAce = Pattern
				.compile("(20)\\d\\d[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01]) .*");
		int cont = 0;
		int contJaExiste = 0;
		for (Entry<String, Path> elemento : catalogo.entrySet()) {
			String textoCasado = null;
			String subPastaAnoMes = null;

			File arquivoOrigem = elemento.getValue().toFile();
			String nomeArquivo = elemento.getValue().getFileName().toString();

			Matcher mN8 = padraoN8.matcher(nomeArquivo);
			if (mN8.find()) {
				textoCasado = mN8.group();
				LOG.debug(String.format("arquivo %s, padrao N8 %s ",
						nomeArquivo, textoCasado));
				subPastaAnoMes = textoCasado.substring(0, 7);
			} else {
				Matcher mS4 = padraoS4.matcher(nomeArquivo);
				if (mS4.find()) {
					textoCasado = mS4.group();
					LOG.debug(String.format("arquivo %s, padrao S4 %s ",
							nomeArquivo, textoCasado));
					subPastaAnoMes = textoCasado.substring(0, 4) + "-"
							+ textoCasado.substring(4, 6);

				} else {
					Matcher mAce = padraoAce.matcher(nomeArquivo);
					if (mAce.find()) {
						textoCasado = mAce.group();
						LOG.debug(String.format("arquivo %s, padrao Ace %s ",
								nomeArquivo, textoCasado));
						subPastaAnoMes = textoCasado.substring(0, 7);
					} else {

						Calendar modificadoEm = GregorianCalendar.getInstance();
						modificadoEm.setTime(new Date(arquivoOrigem
								.lastModified()));
						LOG.debug(String.format(
								"arquivo %s, sem padrao de nome ", nomeArquivo));
						subPastaAnoMes = String.format("\\%04d-%02d\\",
								modificadoEm.get(Calendar.YEAR),
								modificadoEm.get(Calendar.MONTH) + 1);

					}
				}
			}

			Path dirDestino = Paths.get(pastaDestino.getAbsolutePath() + "\\"
					+ subPastaAnoMes);
			if (!Files.exists(dirDestino)) {
				dirDestino = Files.createDirectory(dirDestino);
				LOG.info(String.format("pasta %s criada.",
						dirDestino.toString()));
			}

			File arquivoDestino = new File(dirDestino.toString() + "\\"
					+ nomeArquivo);
			cont++;
			try {

				Files.copy(arquivoOrigem.toPath(), arquivoDestino.toPath());
				LOG.debug(String.format("Arquivo %s criado",
						arquivoDestino.getAbsolutePath()));
			} catch (java.nio.file.FileAlreadyExistsException e) {
				
				LOG.warn(String.format("arquivo %s já existe",
						arquivoDestino.getAbsolutePath()));
				contJaExiste++;
				
				Path dirDestinoBck = Paths.get(pastaDestinoBck.getAbsolutePath() + "\\"
						+ subPastaAnoMes);
				if (!Files.exists(dirDestinoBck)) {
					dirDestinoBck = Files.createDirectory(dirDestinoBck);
					LOG.info(String.format("pasta %s criada.",
							dirDestinoBck.toString()));
				}
				File arquivoDestinoBck = new File(dirDestinoBck.toString() + "\\"
						+ nomeArquivo);
				try {
					Files.copy(arquivoOrigem.toPath(), arquivoDestinoBck.toPath());
				} catch (Exception oe) {
				LOG.error(String.format(
						"arquivo duplicado %s não copiado: " + oe.getMessage(),
						arquivoDestinoBck.getAbsolutePath()));
				}
			} catch (Exception oe) {
				LOG.error(String.format(
						"arquivo %s não copiado: " + oe.getMessage(),
						arquivoDestino.getAbsolutePath()));
			}
			if ((cont % 50) == 0) {
				LOG.info("posicao " + cont);
			}
		}
		LOG.info(String.format("%d arquivos avaliados", cont));
		LOG.info(String.format("%d arquivos em duplicidade", contJaExiste));
	}

	private static void verificaSincronizacao(Map<String, Path> catalogo,
			File[] pastasDestino) throws IOException {

		// padrão nokia: 2015-06-07-1105
		// padrão s4: 20150603_203140 ou 20150428_161032_Richtone(HDR) ou
		// 20150429_211018_LLS
		// padrão xperia: DSC_2099
		// ace: 2014-08-29 18.46.44
		// camera sony: DSC05071

		Pattern padraoN8 = Pattern
				.compile("(20)\\d\\d[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01])[-]\\d*");
		Pattern padraoS4 = Pattern
				.compile("(20)\\d\\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])_\\d{6}.*");
		Pattern padraoAce = Pattern
				.compile("(20)\\d\\d[-](0[1-9]|1[012])[-](0[1-9]|[12][0-9]|3[01]) .*");
		int cont = 0;
		int contNaoExiste = 0;
		for (Entry<String, Path> elemento : catalogo.entrySet()) {
			String textoCasado = null;
			String subPastaAnoMes = null;

			File arquivoOrigem = elemento.getValue().toFile();
			String nomeArquivo = elemento.getValue().getFileName().toString();

			Matcher mN8 = padraoN8.matcher(nomeArquivo);
			if (mN8.find()) {
				textoCasado = mN8.group();
				LOG.debug(String.format("arquivo %s, padrao N8 %s ",
						nomeArquivo, textoCasado));
				subPastaAnoMes = textoCasado.substring(0, 7);
			} else {
				Matcher mS4 = padraoS4.matcher(nomeArquivo);
				if (mS4.find()) {
					textoCasado = mS4.group();
					LOG.debug(String.format("arquivo %s, padrao S4 %s ",
							nomeArquivo, textoCasado));
					subPastaAnoMes = textoCasado.substring(0, 4) + "-"
							+ textoCasado.substring(4, 6);

				} else {
					Matcher mAce = padraoAce.matcher(nomeArquivo);
					if (mAce.find()) {
						textoCasado = mAce.group();
						LOG.debug(String.format("arquivo %s, padrao Ace %s ",
								nomeArquivo, textoCasado));
						subPastaAnoMes = textoCasado.substring(0, 7);
					} else {

						Calendar modificadoEm = GregorianCalendar.getInstance();
						modificadoEm.setTime(new Date(arquivoOrigem
								.lastModified()));
						LOG.debug(String.format(
								"arquivo %s, sem padrao de nome ", nomeArquivo));
						subPastaAnoMes = String.format("\\%04d-%02d\\",
								modificadoEm.get(Calendar.YEAR),
								modificadoEm.get(Calendar.MONTH) + 1);

					}
				}
			}
			cont++;
			boolean achou = false;
			for (File pastaDestino : pastasDestino) {

				Path dirDestino = Paths.get(pastaDestino.getAbsolutePath()
						+ "\\" + subPastaAnoMes);

				File arquivoDestino = new File(dirDestino.toString() + "\\"
						+ nomeArquivo);
				try {

					if (Files.exists(arquivoDestino.toPath())) {
						LOG.debug(String.format("Arquivo %s sincronizado em $s",
								arquivoOrigem.toPath(), arquivoDestino.toPath()));
												
						long sizeDestino = (Long) Files.getAttribute(
								arquivoDestino.toPath(), "basic:size",
								java.nio.file.LinkOption.NOFOLLOW_LINKS);
						long sizeOrigem = (Long) Files.getAttribute(
								arquivoOrigem.toPath(), "basic:size",
								java.nio.file.LinkOption.NOFOLLOW_LINKS);
						
						if (sizeDestino != sizeOrigem) {
							LOG.warn(String
									.format("destino %s(%d) diferente origem %s(%d)",
											 arquivoDestino.getAbsolutePath(), sizeDestino, 
											 arquivoOrigem.getAbsolutePath(), sizeOrigem));
						}
						achou = true;
						break;
					}
				} catch (Exception oe) {
					LOG.error(String.format(
							"arquivo %s não sincronizado: " + oe.getMessage(),
							arquivoOrigem.getAbsolutePath()));
				}

			}
			if ((cont % 50) == 0) {
				LOG.info("posicao " + cont);
			}
			if (!achou) {
				contNaoExiste++;
				LOG.info(String.format("Arquivo %s nao sincronizado",
						arquivoOrigem.getAbsolutePath()));

			}

		}
		LOG.info(String.format("%d arquivos avaliados", cont));
		LOG.info(String.format("%d arquivos não sincronizado", contNaoExiste));

	}
}
