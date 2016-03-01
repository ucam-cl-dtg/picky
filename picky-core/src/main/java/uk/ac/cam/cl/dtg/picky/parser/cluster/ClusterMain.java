package uk.ac.cam.cl.dtg.picky.parser.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import uk.ac.cam.cl.dtg.picky.parser.Entry;

import com.google.common.io.Files;

public class ClusterMain {
	public static void main(final String[] args) throws FileNotFoundException, IOException {

		final File folder = new File("/media/dhintze/Extern/clusterdata-demo");

		ClusterParser clusterParser = new ClusterParser();

		List<File> files = StreamSupport
				.stream(Files.fileTreeTraverser().preOrderTraversal(folder).spliterator(), true)
				.filter(File::isFile)
				.collect(Collectors.toList());

		files.stream()
				.filter(f -> f != null)
				.forEach(fileEntry -> {
					try {
						clusterParser.open(fileEntry);
						Optional<Entry> result;
						while ((result = clusterParser.readEntry()).isPresent()) {
							System.out.println(result.get());
						}
						System.out.println(clusterParser.getFileAttributes());
					} catch (Exception e) {
						e.printStackTrace();
					}

					clusterParser.close();

				});
	}
}
