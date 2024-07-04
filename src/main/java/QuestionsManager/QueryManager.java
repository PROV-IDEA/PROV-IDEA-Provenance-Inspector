package QuestionsManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.interop.InteropFramework.ProvFormat;
import org.openprovenance.prov.model.Document;

/**
 * Hello world!
 *
 */
public class QueryManager {

	private static Model model = null;

	public static void main(String[] args) {
		QueryManager qm = new QueryManager();
		String queryFileName1 = "src/main/resources/queries/1questionDescribeWhySurnameGeneration.txt";
		String query1 = null;
		String result1 = null;
		try {
			qm.createInmemoryModel("data");
			query1 = qm.getQuery(queryFileName1, null);
			result1 = qm.querySelect(query1); // queryDescribe
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getQuery(String queryFileName, String param) throws IOException {

		StringBuffer queryString = new StringBuffer();

		try (Stream<String> stream = Files.lines(Paths.get(queryFileName))) {
			stream.forEach(line -> {
				if (param != null) {
					line = line.replaceAll("<subject>", param);
				}
				queryString.append("\n" + line);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		return queryString.toString();
	}

	public void createInmemoryModel(String dataDirectory) {
		Model modelAux = null;
		try {
			String fullDirectory = "src/main/resources/" + dataDirectory;
			File directory = new File(fullDirectory);
			// Create an empty in-memory model and populate it from the graph
			modelAux = ModelFactory.createDefaultModel();
			if (directory.listFiles() != null) {
				File[] fList = directory.listFiles();
				// Each File corresponds to a PROV document file
				for (File file : fList) {
					InputStream in = new FileInputStream(file);
					modelAux.read(in, null, "TURTLE"); // null base URI, since model URIs are absolute
					in.close();

				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model = modelAux;
	}

	public String querySelect(String queryString) throws IOException {

		header("Query");
		System.out.println(queryString.length());
		// CONSTRUCTION
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		ResultSet resultSet = qe.execSelect();
		String result = ResultSetFormatter.asText(resultSet);
		qe.close();

		return result;
	}

	public String queryConstructImage(String queryString) throws IOException {

		header("Query");
		System.out.println(queryString);

		// CONSTRUCTION
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		Model modelResult = qe.execConstruct();

		header("Result TTL");
		modelResult.write(System.out, "TURTLE");

		modelResult.write(new FileOutputStream(new File("src/main/java/results/resultSet.ttl")), "TURTLE");
		doConversions("src/main/java/results/resultSet.ttl", "src/main/java/results/resultSet.svg");

		qe.close();

		return queryString.toString();
	}

	public String queryDescribe(String queryString) throws IOException {

		// CONSTRUCTION
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);

		Model modelResult = qe.execDescribe();

		header("Result TTL");
		modelResult.write(System.out, "TURTLE");

		modelResult.write(new FileOutputStream(new File("src/main/java/results/resultSet.ttl")), "TURTLE");

		doConversions("src/main/java/results/resultSet.ttl", "src/main/java/results/resultSet.svg");

		qe.close();

		return queryString.toString();
	}

	public String queryConstruct(String queryString) throws IOException {

		// CONSTRUCTION
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		Model modelResult = qe.execConstruct();

		header("Result TTL");
		modelResult.write(System.out, "TURTLE");

		modelResult.write(new FileOutputStream(new File("src/main/java/results/resultSet.ttl")), "TURTLE");

		doConversions("src/main/java/results/resultSet.ttl", "src/main/java/results/resultSet.svg");

		qe.close();

		return queryString.toString();
	}

	public void queryAsk(String queryString) throws IOException {
		// CONSTRUCTION
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		
		Boolean resultAsk = qe.execAsk();
		Model modelResult = ModelFactory.createDefaultModel();
		if (resultAsk) {
			modelResult.read("src/main/java/results/true.ttl");
		} else {
			modelResult.read("src/main/java/results/false.ttl");
		}

		header("Result TTL");
		modelResult.write(System.out, "TURTLE");

		modelResult.write(new FileOutputStream(new File("src/main/java/results/resultSet.ttl")), "TURTLE");

		doConversions("src/main/java/results/resultSet.ttl", "src/main/java/results/resultSet.svg");

		qe.close();
	}

	private void header(String msg) {
		System.out.println("\n\n");
		System.out.println("#########################################################################");
		System.out.println("#########################################################################");
		System.out.println("############################ " + msg + " ######################################");
		System.out.println("#########################################################################");
		System.out.println("#########################################################################");
	}

	private void doConversions(String filein, String fileout) {
		InteropFramework intF = new InteropFramework();
		Document document = intF.readDocumentFromFile(filein);
		intF.writeDocument(fileout, ProvFormat.SVG, document);
	}

}
