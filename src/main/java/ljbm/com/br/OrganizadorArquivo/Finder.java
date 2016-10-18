package ljbm.com.br.OrganizadorArquivo;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A {@code FileVisitor} that finds all files that match the specified pattern.
 */
public class Finder extends SimpleFileVisitor<Path> {

	/**
	 * 
	 */
	private static final Logger LOG = Logger.getLogger(Finder.class);
	private final PathMatcher matcher;
	private int numMatches = 0;
	private Map<String, Path> mapa = new HashMap<String, Path>(0);
	private String padrao;

	public Map<String, Path> getMapa() {
		return mapa;
	}

	Finder(String pattern) {
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		this.padrao = pattern;
	}

	// Compares the glob pattern against
	// the file or directory name.
	void find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			numMatches++;
			LOG.debug(name.toString() + " - " + file.toString() + ","
					+ file.toFile().length());
			//mapa.put(name.toString(), file);
			
			mapa.put(file.toString(), file);

		}
	}

	// Prints the total number of
	// matches to standard out.
	void done() {
		// Collection<String> elementos = referencia.values();
		// for (String elemento : elementos) {
		// LOG.info(elemento);
		// }
		// Set<String> chaves = referencia.keySet();
		// for (String chave : chaves) {
		// LOG.info(chave);
		// }
		LOG.info(String.format("Encontrados %d arquivos %s.", numMatches,
				padrao));

	}

	// Invoke the pattern matching
	// method on each file.
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		find(file);
		return CONTINUE;
	}

	// Invoke the pattern matching
	// method on each directory.
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		find(dir);
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return CONTINUE;
	}
}